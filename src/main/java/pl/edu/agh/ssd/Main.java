package pl.edu.agh.ssd;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smoke simulation");
        Canvas canvas = new Canvas(400, 400);
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        StackPane stackPane = new StackPane(canvas);
        Scene scene = new Scene(stackPane, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        Color color = new Color(0.5,0.5,0.5, 0.5);
        // Grid grid = new Grid(100, 100, 100);
        // for (int i = 0; i < 100; i++) {
        //     for (int j = 0; j < 100; j++) {
        //         ctx.setFill(Grid.densityToColor(grid.density[i][j][55]));
        //         ctx.fillRect(i * 4, j * 4, 4, 4);
        //     }
        // }
        ctx.setFill(color);
        ctx.fillRect(10, 10, 100, 100);
    }
}