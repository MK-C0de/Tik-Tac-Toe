import javafx.application.Application;
import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class ClientGUI extends Application {

    private final int MAXSIZE = 3;
    private Rectangle[][] squares;

    @Override
    public void start (Stage primaryStage){
        Stage stage = primaryStage;
        primaryStage.setTitle("Tic Tac Toe");

        GridPane root = new GridPane();
        Scene scene = new Scene(root, 450, 450, Color.WHITE);

        int[][] localBoard = new int[MAXSIZE][MAXSIZE];
        this.squares = new Rectangle[MAXSIZE][MAXSIZE];

        for (int row = 0; row < MAXSIZE; ++row){
            for (int col = 0; col < MAXSIZE; ++col){
                Rectangle square = createSquare();
                root.add(square, col, row);

                this.squares[row][col] = square;
                localBoard[row][col] = 0;
            }
        }

        Client newClient = new Client(this, localBoard);
        Thread t = new Thread(newClient);
        newClient.setThread(t);
        t.start();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Rectangle createSquare () {
        Rectangle newSquare = new Rectangle(150, 150);

        newSquare.setFill(Color.WHITE);
        newSquare.setStrokeType(StrokeType.INSIDE);
        newSquare.setStroke(Color.BLACK);
        newSquare.setStrokeWidth(1);

        return newSquare;
    }

    public Rectangle[][] getSquares () {
        return this.squares;
    }

    public static void main (String[] args){
        Application.launch(args);
    }
}
