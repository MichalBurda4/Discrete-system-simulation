package pl.edu.agh.ssd;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer; // Poprawne zaimportowanie
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
    private static final int WINDOW_SIZE = 800;
    private static final int CUBE_SIZE = 100;
    private final Rotate rotateX = new Rotate(5, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-15, Rotate.Y_AXIS);
    private PhongMaterial[] materials;

    Box[][][] boxGrid = new Box[10][10][10];

    @Override
    public void start(Stage stage) {
        materials = new PhongMaterial[10];
        for (int i = 0; i < 10; i++) {
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(new Color(0.5, 0.5, 0.5, i / 10.0));
            materials[i] = material;
        }
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(new Color(0.5, 0.5, 0.5, 0.6));

        int WIDTH = 1400;
        int HEIGHT = 800;
        Group group = new Group();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    Box cube = new Box(5, 5, 5);
                    // cube.setMaterial(material);
                    cube.setTranslateX(i * 5);
                    cube.setTranslateY(j * 5);
                    cube.setTranslateZ(k * 5);
                    // cube.setVisible(false);
                    boxGrid[i][j][k] = cube;
                    group.getChildren().add(cube);
                }
            }
        }

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(1000);
        camera.setTranslateX(100);
        camera.setTranslateY(20);
        camera.setTranslateZ(-300);
        camera.getTransforms().addAll(rotateX, rotateY);

        Scene scene = new Scene(group, WIDTH, HEIGHT);
        scene.setCamera(camera);
        stage.setScene(scene);
        stage.show();

        SmokeSimulation smokeSimulation = new SmokeSimulation(10, 10, 10, 0.05, 0.3, 1000);
        smokeSimulation.addBound(9, 9, 0, 9, 0, 0);
        smokeSimulation.addBound(4, 9, 1, 1, 0, 0);
        smokeSimulation.addBound(6, 6, 1, 3, 0, 0);
        smokeSimulation.addSource(2, 8, 0);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            smokeSimulation.update();

            // Rysowanie aktualnego stanu symulacji
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    for (int k = 0; k < 10; k++) {
                        double density = smokeSimulation.room.density[i][j][k];
                        boxGrid[i][j][k].setMaterial(getMaterial(density));
                    }
                }
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // new AnimationTimer() {
        //     @Override
        //     public void handle(long now) {
        //         // Aktualizacja symulacji
        //         smokeSimulation.update();

        //         // Rysowanie aktualnego stanu symulacji
        //         for (int i = 0; i < 10; i++) {
        //             for (int j = 0; j < 10; j++) {
        //                 for (int k = 0; k < 10; k++) {
        //                     boxGrid[i][j][k].setMaterial(getMaterial(smokeSimulation.room.density[i][j][k]));
        //                 }
        //             }
        //         }
        //     }
        // }.start();
    }

    private PhongMaterial getMaterial(double density) {
        double normDensity = Math.max(0.0, Math.min(1.0, density));

        if (normDensity >= 0 && normDensity < 0.1) {
            return materials[0];
        } else if (normDensity >= 0.1 && normDensity < 0.2) {
            return materials[1];
        } else if (normDensity >= 0.2 && normDensity < 0.3) {
            return materials[2];
        } else if (normDensity >= 0.3 && normDensity < 0.4) {
            return materials[3];
        } else if (normDensity >= 0.4 && normDensity < 0.5) {
            return materials[4];
        } else if (normDensity >= 0.5 && normDensity < 0.6) {
            return materials[5];
        } else if (normDensity >= 0.6 && normDensity < 0.7) {
            return materials[6];
        } else if (normDensity >= 0.7 && normDensity < 0.8) {
            return materials[7];
        } else if (normDensity >= 0.8 && normDensity < 0.9) {
            return materials[8];
        } else if (normDensity >= 0.9 && normDensity < 1.0) {
            return materials[9];
        }
        return materials[9];
    }

    public static void main(String[] args) {
        launch(args);
    }
}