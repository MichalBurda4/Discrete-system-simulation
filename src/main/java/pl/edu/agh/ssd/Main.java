package pl.edu.agh.ssd;

import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
    private static final int WINDOW_SIZE = 800;
    private static final int CUBE_SIZE = 100;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(15, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);
    private PhongMaterial[] materials;
    private final int squareRoomSize = 32;

    private double mouseOldX;
    private double mouseOldY;
    private double cameraAngleX = 0;
    private double cameraAngleY = 0;

    Box[][][] boxGrid = new Box[squareRoomSize][squareRoomSize][squareRoomSize];

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

        Group group = new Group();

        int WIDTH = 1400;
        int HEIGHT = 800;
        for (int i = 0; i < squareRoomSize; i++) {
            for (int j = 0; j < squareRoomSize; j++) {
                for (int k = 0; k < squareRoomSize; k++) {
                    Box cube = new Box(5, 5, 5);

                    cube.setTranslateX((int) (i - squareRoomSize / 2) * 5);
                    cube.setTranslateY((int) (j - squareRoomSize / 2) * 5);
                    cube.setTranslateZ((int) (k - squareRoomSize / 2) * 5);

                    boxGrid[i][j][k] = cube;
                    group.getChildren().add(cube);
                }
            }
        }

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(1000);
        camera.setTranslateX(-100);
        camera.setTranslateY(30);
        camera.setTranslateZ(-400);

        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(0);
        light.setTranslateY(0);
        light.setTranslateZ(-500);
        group.getChildren().add(light);

        camera.getTransforms().addAll(rotateX, rotateY, rotateZ);

        Scene scene = new Scene(group, WIDTH, HEIGHT);
        scene.setCamera(camera);

        scene.setOnMousePressed(event -> {
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        scene.setOnMouseDragged(event -> {
            double mouseDeltaX = event.getSceneX() - mouseOldX;
            double mouseDeltaY = event.getSceneY() - mouseOldY;

            cameraAngleX += mouseDeltaY * 0.1;
            cameraAngleY += mouseDeltaX * 0.1;

            rotateX.setAngle(cameraAngleX);
            rotateY.setAngle(cameraAngleY);

            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        scene.setOnScroll(event -> {
            double zoomAmount = event.getDeltaY() * 0.2;
            double newTranslateZ = camera.getTranslateZ() + zoomAmount;

            if (newTranslateZ > -50) {
                newTranslateZ = -50;
            }
            if (newTranslateZ < -1000) {
                newTranslateZ = -1000;
            }

            camera.setTranslateZ(newTranslateZ);
        });

        stage.setScene(scene);
        stage.show();

        Timeline timeline = getTimeline();
        timeline.play();
    }

    private Timeline getTimeline() {
        SmokeSimulation smokeSimulation = new SmokeSimulation(squareRoomSize, squareRoomSize, squareRoomSize, 0.1, 3, 1000, 0.00001);
        smokeSimulation.addSource(15, 30, 15);
        smokeSimulation.addBound(15, 31, 24, 24, 0, 31);


        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            smokeSimulation.update();

            for (int i = 0; i < squareRoomSize; i++) {
                for (int j = 0; j < squareRoomSize; j++) {
                    for (int k = 0; k < squareRoomSize; k++) {
                        double density = smokeSimulation.room.density[i][j][k];
                        if (smokeSimulation.room.isSource[i][j][k]) {
                            boxGrid[i][j][k].setMaterial(new PhongMaterial(Color.BLUE));
                            boxGrid[i][j][k].setVisible(true);
                        } else if (smokeSimulation.room.isBarrier[i][j][k]) {
                            boxGrid[i][j][k].setMaterial(new PhongMaterial(Color.RED));
                            boxGrid[i][j][k].setVisible(true);
                        } else if (density > 0.5) {
                            boxGrid[i][j][k].setMaterial(getMaterial(density));
                            boxGrid[i][j][k].setVisible(true);
                        } else {
                            boxGrid[i][j][k].setVisible(false);
                        }
                    }
                }
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);

        timeline.play();
        return timeline;
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
