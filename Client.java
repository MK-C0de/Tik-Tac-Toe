import java.io.InputStream;
import java.io.IOException;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import java.util.Scanner;
import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Client implements Runnable {

    private final int SBAP_PORT = 8888;
    private Socket socket;

    private InputStream instream;
    private OutputStream outstream;

    private Scanner in;
    private PrintWriter out;

    private int[][] localBoard;
    private ClientGUI gui;

    private int playerNumber;
    private Thread currentThread;

    private final int SLEEPTIME = 100;
    private final int DELAYTIME = 110;
    private final int MAXSIZE = 3;
    private final int MAXTURNS = 9;

    public Client (ClientGUI gui, int[][] local) {
        try {
            this.socket = new Socket("localhost", SBAP_PORT);

            this.instream = socket.getInputStream();
            this.outstream = socket.getOutputStream();

            this.in = new Scanner(this.instream);
            this.out = new PrintWriter(this.outstream);

            this.localBoard = local;
            this.gui = gui;
        } catch (IOException ee){
            ee.printStackTrace();
        }
    }

    public void setThread (Thread t) {
        this.currentThread = t;
    }

    public void run (){

        String command = "SayHi";
        this.out.println(command);
        this.out.flush();

        String response = this.in.nextLine();
        System.out.println(response);

        response = this.in.nextLine();
        this.playerNumber = Integer.parseInt(response);
        checkPlayer(response);

        if (this.playerNumber == 1)
            System.out.println("You go first, pick a tile");
        else
            System.out.println("You go second, waiting for player 1");

        firstTurn();
        turnAction();
        closeResources();
    }

    public void checkPlayer (String response) {
        String playerColor;
        if(this.playerNumber == 1)
            playerColor = "BLUE";
        else
            playerColor = "RED";

        System.out.println("You are Player " + response + 
                           " (" + playerColor + ")");
    }

    public void firstTurn () {
        if(this.playerNumber == 1)
            makeAMove();
    }

    public void turnAction () {
        while(true) {
            if (!in.hasNext())
                continue;

            String response = this.in.nextLine();
            String opponent = getOpponent();
            System.out.println("Sever: " + response);

            if (!statusIsSet() && response.equals("Player " + opponent + " has made a move")){
                requestNewArray();

                if (maxTurns() && !statusIsSet()){
                    gameDraw();
                    this.currentThread.interrupt();
                }

                makeAMove();
            }

            if (response.equals("Player " + opponent + " has won")){
                System.out.println("Better luck next time...");

                this.currentThread.interrupt();
            }
        }
    }

    public void requestNewArray (){
        String command = "GetArr";
        this.out.println(command);
        this.out.flush();
        
        String response = this.in.nextLine();
        String[] split = response.split(",");

        int count = 0;
        for (int row = 0; row < this.MAXSIZE; ++row) {
            for (int col = 0; col < this.MAXSIZE; ++col) {
                this.localBoard[row][col] = Integer.parseInt(split[count]);
                count++;
            }
        }
        updateGUI();
    }

    public boolean maxTurns () {
        String command = "GetTurns";
        this.out.println(command);
        this.out.flush();

        String response = this.in.nextLine();
        int totalTurns = Integer.parseInt(response);
        System.out.println("      turn " + totalTurns);

        if (totalTurns == this.MAXTURNS)
            return true;

        return false;
    }

    public void updateGUI () {
        System.out.println("Updating the GUI");
        Rectangle[][] squares = this.gui.getSquares();

        for (int row = 0; row < this.MAXSIZE; ++row) {
            for (int col = 0; col < this.MAXSIZE; ++col) {
                if (localBoard[row][col] == 0)
                    squares[row][col].setFill(Color.WHITE);

                if (localBoard[row][col] == 1)
                    squares[row][col].setFill(Color.BLUE);

                if (localBoard[row][col] == 2)
                    squares[row][col].setFill(Color.RED);
            }
        }
    }

    public void makeAMove (){
        ArrayList<Integer> legalMoves = findLegalMoves();
        if (legalMoves.size() == 0)
            return;

        int random = randomInt (0, legalMoves.size()-1);
        int pickTile = legalMoves.get(random);

        int count = 0;
        for (int row = 0; row < this.MAXSIZE; ++row) {
            for (int col = 0; col < this.MAXSIZE; ++col) {
                if (pickTile == count)
                    this.localBoard[row][col] = this.playerNumber;
                count++;
            }
        }

        updateGUI();
        if(!statusIsSet() && checkWin()){
            System.out.println("You got three in a row!");
            String command = "CallWin";
            this.out.println(command);
            this.out.flush();

            this.currentThread.interrupt();
        }

        String localArr = getLocalString();
        String command = "UpdateArr " + localArr;
        this.out.println(command);
        this.out.flush();

        try {
            Thread.sleep(this.DELAYTIME);
        } catch (InterruptedException e){
            System.out.println("");
        }
    }

    public ArrayList<Integer> findLegalMoves () {
        ArrayList<Integer> legalMoves = new ArrayList<Integer>();
        Rectangle[][] squares = gui.getSquares();

        int count = 0;
        for (int row = 0; row < this.MAXSIZE; ++row) {
            for (int col = 0; col < this.MAXSIZE; ++col) {
                if(squares[row][col].getFill() == Color.WHITE){
                    legalMoves.add(count);
                }
                count++;
            }
        }
        return legalMoves;
    }

    public boolean statusIsSet() {
        String command = "GetWinStatus ";
        this.out.println(command);
        this.out.flush();

        String response = this.in.nextLine();
        int status = Integer.parseInt(response);
        if(status >= 0)
            return true;
        return false;
    }

    public String getOpponent () {
        if(this.playerNumber == 1)
            return "2";
        return "1";
    }

    public String getLocalString() {
        String local = "";
        for (int row = 0; row < this.MAXSIZE; ++row) {
            for (int col = 0; col < this.MAXSIZE; ++col) {
                local += "" + this.localBoard[row][col];

                if (row == this.MAXSIZE-1 && col == this.MAXSIZE-1)
                    local += "";
                else
                    local += ",";
            }
        }
        return local;
    }

    public int randomInt (int min, int max) {
        return (int)(Math.random() * (max - min + 1) + min);  
    }

    public boolean checkWin () {
        Rectangle[][] squares = this.gui.getSquares();
        Color playerColor;
        if (playerNumber == 1)
            playerColor = Color.BLUE;
        else
            playerColor = Color.RED;

        //check horizontal
        for (int row = 0; row < this.MAXSIZE; ++row) {
            int horizontalCount = 0;
            for (int col = 0; col < this.MAXSIZE; ++col) {
                if (squares[row][col].getFill() == playerColor)
                    horizontalCount++;
            }
            if(horizontalCount == 3)
                return true;
        }

        //check vertical
        for (int col = 0; col < this.MAXSIZE; ++col) {
            int verticalCount = 0;
            for (int row = 0; row < this.MAXSIZE; ++row) {
                if (squares[row][col].getFill() == playerColor)
                    verticalCount++;
            }
            if(verticalCount == 3)
                return true;
        }

        //check left diagonal
        int diagonalLeft = 0;
        for (int row = 0; row < this.MAXSIZE; ++row) {
            for (int col = 0; col < this.MAXSIZE; ++col) {
                if (row == 0 && col == 0 && squares[row][col].getFill() == playerColor)
                    diagonalLeft++;
                if (row == 1 && col == 1 && squares[row][col].getFill() == playerColor)
                    diagonalLeft++;
                if (row == 2 && col == 2 && squares[row][col].getFill() == playerColor)
                    diagonalLeft++;
            }
        }
        if(diagonalLeft == 3)
                return true;

        //check right diagonal
        int diagonalRight = 0;
        for (int row = 0; row < this.MAXSIZE; ++row) {
            for (int col = 0; col < this.MAXSIZE; ++col) {
                if (row == 0 && col == 2 && squares[row][col].getFill() == playerColor)
                    diagonalRight++;
                if (row == 1 && col == 1 && squares[row][col].getFill() == playerColor)
                    diagonalRight++;
                if (row == 2 && col == 0 && squares[row][col].getFill() == playerColor)
                    diagonalRight++;
            }
        }
        if(diagonalRight == 3)
                return true;

        return false;
    }

    public void gameDraw() {
        String command = "CallDraw";
        this.out.println(command);
        this.out.flush();
    }

    public void closeResources() {
        try {
            this.in.close();
            this.out.close();
            this.socket.close();
        } catch (IOException e){
            e.printStackTrace();
        } 
    }
}