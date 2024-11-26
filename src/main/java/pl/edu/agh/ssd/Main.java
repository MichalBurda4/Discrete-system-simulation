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
        // Label label = new Label("Work in progress...");
        Canvas canvas = new Canvas(400, 400);
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        StackPane stackPane = new StackPane(canvas);
        Scene scene = new Scene(stackPane, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        Color color = new Color(0.5,0.5,0.5, 0.5);
        ctx.setFill(color);
        ctx.fillRect(10, 10, 100, 100);
    }
}