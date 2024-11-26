package pl.edu.agh.ssd;

import javafx.scene.paint.Color;

public class Grid {
    private double[][][] velocityX;
    private double[][][] velociyY;
    private double[][][] velocitZ;
    private double[][][] density;
    private double[][][] temperature;

    public Grid(int X, int Y, int Z) {
        this.velocityX = new double[X][Y][Z];
        this.velociyY = new double[X][Y][Z];
        this.velocitZ = new double[X][Y][Z];
        this.density = new double[X][Y][Z];
        this.temperature = new double[X][Y][Z];


        // --- Test
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    this.density[20 + i][10 + j][50 + k] = 0.5;
                }
            }
        }
    }

    public static Color densityToColor(double density) {
        return new Color(0, 0, 0, density);
    }
}
