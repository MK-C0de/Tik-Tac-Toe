import java.io.InputStream;
import java.io.IOException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.lang.Thread;

public class Server {

    public static void main (String[] args) {
        try {
            Game newGame =  new Game();

            final int SBAP_PORT = 8888;
            ServerSocket server = new ServerSocket(SBAP_PORT);
            System.out.println("Waiting for players to connect...");

            int playerNumber = 0;
            boolean check = true;
            while (true){ 
                
                if (newGame.maxLobby()){
                    if(check) 
                        System.out.println("Max number of players reached [2/2]");
           
                    check = false;
                    continue;
                }

                Socket playerSocket = server.accept();
                playerNumber++;

                Service service = new Service(playerSocket, playerNumber, newGame);
                newGame.addPlayer(service);

                Thread t = new Thread(service);
                t.start();
            }
        } catch (IOException e){
            System.err.println("Server could not start...");
        }
    }
}