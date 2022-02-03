import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import java.io.BufferedReader;

public class Service implements Runnable {

    private Game game;
    private Socket socket;
    private int playerNumber;

    private Scanner in;
    private PrintWriter out;

    public Service (Socket aSocket, int number, Game newGame) {
        this.socket = aSocket;
        this.playerNumber = number;
        this.game = newGame;

    }

    public void run () {
        try {
            try {
                this.in = new Scanner(socket.getInputStream());
                this.out = new PrintWriter(socket.getOutputStream());
                doService();
            } finally {
                this.in.close();
                this.out.close();

                try {
                    this.socket.close();
                } catch (IOException e){
                    e.printStackTrace();}  
            } 
        } catch (IOException ioe) {ioe.printStackTrace();}
    }

    public void doService() {
        while(true){
            if(!this.in.hasNext())
                return;

            String command = this.in.nextLine();
            if (command.equals("QUIT"))
                return;
            else
                executeCommand(command);
        }
    }

    public void executeCommand(String commandString){

        //System.out.println("Player " + this.getPlayerNumber() + " sent " + commandString);
        String[] split = commandString.split(" ");
        String command = "";
        String board = "";

        if(split.length == 2){
            command = split[0];
            board = split[1];
        } else {
            command = split[0];
        }

        if (command.equals("SayHi")){
            System.out.println("A player said hi");
            out.println("Hello!");

            this.game.incrementHi();
            System.out.println("HiCount: " + this.game.getHiCount());
            if(this.game.getHiCount() == 2){
                System.out.println("Two players have met, starting game");
                out.println("" + this.playerNumber);
            }
            out.flush();
        }

        if (command.equals("GetArr")){
            System.out.println("Player " + this.playerNumber + " is requesting for the remoteBoard");
            out.println(this.game.getRemoteString());
            out.flush();
        }

        if (command.equals("UpdateArr")){
            System.out.println("Player " + this.playerNumber + " is updating the remoteBoard\n");
            this.game.updateBoard(board, this.playerNumber);
        }

        if (command.equals("GetTurns")) {
            System.out.println("Player " + this.playerNumber + " is requesting for the total turns");
            out.println("" + this.game.getTotalTurns());
            out.flush();
        }

        if(command.equals("CallDraw")) {
            System.out.println("Game has ended in a draw, better luck next time!");
            this.game.setWinStatus(0);
        }

        if(command.equals("CallWin")) {
            System.out.println("Player " + this.playerNumber + " got 3 in a row and wins!");
            this.game.setWinStatus(this.playerNumber);
            this.game.notifyNextTurn(this.playerNumber, true);
        }

        if(command.equals("GetWinStatus")){
            out.println("" + this.game.getWinStatus());
            out.flush();
        }
    }

    public int getPlayerNumber () {
        return this.playerNumber;
    }

    public Socket getSocket () {
        return this.socket;
    }
}