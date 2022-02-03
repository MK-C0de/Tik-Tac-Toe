import java.util.ArrayList;
import java.lang.Thread;
import java.net.Socket;

import java.util.Scanner;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;

public class Game {

    private int[][] remoteBoard;
    private final int MAXSIZE = 3;

    private ArrayList<Service> players;
    private int playersHi;

    private PrintWriter tempOut;
    private int totalTurns;
    private int winStatus;

    public Game (){
        this.players = new ArrayList<Service>();
        this.playersHi = 0;
        this.totalTurns = 0;
        this.winStatus = -1;

        this.remoteBoard = new int[MAXSIZE][MAXSIZE];
        for (int row = 0; row < MAXSIZE; ++row){
            for (int col = 0; col < MAXSIZE; ++col){
                this.remoteBoard[row][col] = 0;
            }
        }
    }

    public void addPlayer (Service newService) {
        this.players.add(newService);
    }

    public String getRemoteString() {
        String remote = "";
        for (int row = 0; row < this.MAXSIZE; ++row) {
            for (int col = 0; col < this.MAXSIZE; ++col) {
                remote += "" + this.remoteBoard[row][col];

                if (row == this.MAXSIZE-1 && col == this.MAXSIZE-1)
                    remote += "";
                else
                    remote += ",";
            }
        }
        return remote;
    }

    public void updateBoard (String newArry, int player){
        String[] split = newArry.split(",");

        int count = 0;
        for (int row = 0; row < this.MAXSIZE; ++row) {
            for (int col = 0; col < this.MAXSIZE; ++col) {
                this.remoteBoard[row][col] = Integer.parseInt(split[count]);
                count++;
            }
        }
        notifyNextTurn(player, false);
    }

    public void notifyNextTurn (int player, boolean isWon) {
        int num = 0;
        if(player == 1){
            for (int i = 0; i < players.size(); ++i){
                if (this.players.get(i).getPlayerNumber() == 2)
                    num = i;   
            }
        } else {
            for (int i = 0; i < players.size(); ++i){
                if (this.players.get(i).getPlayerNumber() == 1)
                    num = i;   
            }
        }

        try{
            Socket tempSocket = this.players.get(num).getSocket();
            this.tempOut = new PrintWriter(tempSocket.getOutputStream());

            if(!isWon){
                tempOut.println("Player " + player + " has made a move");
                this.totalTurns++;
                tempOut.flush();
            } else {
                tempOut.println("Player " + player + " has won");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean maxLobby () {
        if(this.players.size() == 2)
            return true;
        return false;
    }

    public void incrementHi () {
        this.playersHi++;
    }

    public int getHiCount () {
        return this.playersHi;
    }

    public int getTotalTurns () {
        return this.totalTurns;
    }

    public void setWinStatus (int status) {
        this.winStatus = status;
    }

    public int getWinStatus () {
        return this.winStatus;
    }
}
