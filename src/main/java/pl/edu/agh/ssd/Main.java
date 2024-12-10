package pl.edu.agh.ssd;

import javafx.animation.AnimationTimer; // Poprawne zaimportowanie
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

    private static final int CELL_SIZE = 40; // Rozmiar jednej komórki w pikselach

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smoke simulation");

        // Rozmiar okna
        int canvasWidth = 400;
        int canvasHeight = 400;

        // Tworzenie obiektów JavaFX
        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        StackPane stackPane = new StackPane(canvas);
        Scene scene = new Scene(stackPane, canvasWidth, canvasHeight);
        primaryStage.setScene(scene);
        primaryStage.show();

        SmokeSimulation smokeSimulation = new SmokeSimulation(10, 10, 10, 0.05, 0.3, 1000);
        smokeSimulation.addBound(9, 9, 0, 9, 0, 0);
        smokeSimulation.addBound(4, 9, 1, 1, 0, 0);
        smokeSimulation.addBound(6, 6, 1, 3, 0, 0);
        smokeSimulation.addSource(2, 8, 0);
        smokeSimulation.room.printGrid2D(); //Podgląd siatki

        // Dodanie przeszkód
        smokeSimulation.addBound(2, 4, 2, 4, 0, 0);
        // Rysowanie siatki z przeszkodami i źródłami
        //drawGrid(ctx, smokeSimulation.room);



        // Animacja
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Aktualizacja symulacji
                smokeSimulation.update();

                // Rysowanie aktualnego stanu symulacji
                drawGrid(ctx, smokeSimulation.room);
            }
        }.start();

    }

    private void drawGrid(GraphicsContext ctx, SmokeGrid grid) {
        int cellSize = 40; // Rozmiar jednej komórki w pikselach

        for (int x = 0; x < grid.gridSize[0]; x++) {
            for (int y = 0; y < grid.gridSize[1]; y++) {
                // Sprawdź, czy jest to przeszkoda
                if (grid.isBarrier[x][y][0]) {
                    ctx.setFill(Color.RED); // Kolor dla przeszkód
                } else if (grid.isSource[x][y][0]) {
                    ctx.setFill(Color.BLUE); // Kolor dla źródeł
                } else {
                    ctx.setFill(Color.LIGHTGRAY); // Kolor dla pustych komórek
                }
                // Rysuj prostokąt
                ctx.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                ctx.setStroke(Color.BLACK); // Linie siatki
                ctx.strokeRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }
    }

    // Funkcja mapująca gęstość na kolor
    private Color densityToColor(double density) {
        // Normalizuj wartość gęstości do zakresu [0, 1]
        double clampedDensity = Math.max(0.0, Math.min(1.0, density));

        // Mapowanie na kolor: im wyższa gęstość, tym ciemniejszy kolor
        return new Color(1.0 - clampedDensity, 1.0 - clampedDensity, 1.0 - clampedDensity, 1.0);
    }
}
