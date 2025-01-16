package pl.edu.agh.ssd;

import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
    private static final int WINDOW_SIZE = 800;
    private PhongMaterial[] materials;

    private double mouseOldX;
    private double objectAngleY = 0;  // Kąt obrotu wokół osi Y dla obiektów
    private Box[][][] boxGrid;

    @Override
    public void start(Stage stage) {
        // Tworzymy materiał dla obiektów
        materials = new PhongMaterial[10];
        for (int i = 0; i < 10; i++) {
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(new Color(0.5, 0.5, 0.5, i / 10.0));
            materials[i] = material;
        }

        // Formularz do konfiguracji parametrów
        Label widthLabel = new Label("Width:");
        TextField widthField = new TextField("50");  // Domyślny rozmiar szerokości
        Label heightLabel = new Label("Height:");
        TextField heightField = new TextField("50");  // Domyślny rozmiar wysokości
        Label depthLabel = new Label("Depth:");
        TextField depthField = new TextField("50");  // Domyślny rozmiar głębokości
        Label timeStepLabel = new Label("Time Step:");
        TextField timeStepField = new TextField("0.1");  // Domyślny krok czasowy
        Label velocityLabel = new Label("Default Source Velocity:");
        TextField velocityField = new TextField("3");  // Domyślna prędkość źródła
        Label densityLabel = new Label("Default Source Density:");
        TextField densityField = new TextField("100");  // Domyślna gęstość źródła
        Label diffRateLabel = new Label("Diffusion Rate:");
        TextField diffRateField = new TextField("0.00001");  // Domyślny współczynnik dyfuzji
        Label decayRateLabel = new Label("Decay Rate:");
        TextField decayRateField = new TextField("0.02");  // Domyślny współczynnik zaniku

        Button startButton = new Button("Start Simulation");

        VBox inputLayout = new VBox(10);
        inputLayout.getChildren().addAll(widthLabel, widthField, heightLabel, heightField, depthLabel, depthField,
                timeStepLabel, timeStepField, velocityLabel, velocityField, densityLabel, densityField,
                diffRateLabel, diffRateField, decayRateLabel, decayRateField, startButton);
        inputLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Scena formularza
        Scene inputScene = new Scene(inputLayout, WINDOW_SIZE, WINDOW_SIZE);
        stage.setScene(inputScene);
        stage.setTitle("Simulation Parameters");
        stage.show();

        startButton.setOnAction(e -> {
            try {
                // Pobranie wartości z formularza
                int width = Integer.parseInt(widthField.getText());
                int height = Integer.parseInt(heightField.getText());
                int depth = Integer.parseInt(depthField.getText());
                double timeStep = Double.parseDouble(timeStepField.getText());
                double velocity = Double.parseDouble(velocityField.getText());
                double density = Double.parseDouble(densityField.getText());
                double diffRate = Double.parseDouble(diffRateField.getText());
                double decayRate = Double.parseDouble(decayRateField.getText());

                if (width <= 0 || height <= 0 || depth <= 0 || timeStep <= 0 || velocity <= 0 || density < 0) {
                    showError("Invalid input values. Please check your parameters.");
                    return;
                }

                // Uruchamiamy symulację z wprowadzonymi parametrami
                startSimulation(stage, width, height, depth, timeStep, velocity, density, diffRate, decayRate);
            } catch (NumberFormatException ex) {
                showError("Please enter valid numbers.");
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void startSimulation(Stage stage, int width, int height, int depth, double timeStep, double velocity,
                                 double density, double diffRate, double decayRate) {
        // Tworzymy przestrzeń dla boxów
        boxGrid = new Box[width][height][depth];
        Group group = new Group();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < depth; k++) {
                    Box cube = new Box(5, 5, 5);
                    cube.setTranslateX((int)(i - width / 2) * 5);
                    cube.setTranslateY((int)(j - height / 2) * 5);
                    cube.setTranslateZ((int)(k - depth / 2) * 5);
                    boxGrid[i][j][k] = cube;
                    group.getChildren().add(cube);
                }
            }
        }

        // Kamera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(1000);
        camera.setTranslateX(30);
        camera.setTranslateY(-30);
        camera.setTranslateZ(-700);

        // Światło
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(0);
        light.setTranslateY(0);
        light.setTranslateZ(-500);
        group.getChildren().add(light);

        // Scena
        Scene scene = new Scene(group, 1400, 800);
        scene.setCamera(camera);

        // Zdarzenia na myszkę
        scene.setOnMousePressed(event -> mouseOldX = event.getSceneX());
        scene.setOnMouseDragged(event -> {
            double mouseDeltaX = event.getSceneX() - mouseOldX;
            objectAngleY += mouseDeltaX * 0.1;
            group.setRotationAxis(Rotate.Y_AXIS);
            group.setRotate(objectAngleY);
            mouseOldX = event.getSceneX();
        });
        scene.setOnScroll(event -> {
            double zoomAmount = event.getDeltaY() * 0.2;
            double newTranslateZ = camera.getTranslateZ() + zoomAmount;
            if (newTranslateZ > -50) newTranslateZ = -50;
            if (newTranslateZ < -1000) newTranslateZ = -1000;
            camera.setTranslateZ(newTranslateZ);
        });

        stage.setScene(scene);
        stage.show();

        // Inicjalizujemy symulację
        SmokeSimulation smokeSimulation = new SmokeSimulation(width, height, depth, timeStep, velocity, density,
                diffRate, decayRate);

        // Przykładowa logika: dodajemy źródła, wiatr, bariery
        smokeSimulation.addSource(15, 48, 15);
        smokeSimulation.addWind(48, 15, 1, 0, 0, 3);
        smokeSimulation.addBound(15, 31, 24, 24, 0, 31);
        smokeSimulation.addBound(40, 46, 10, 10, 0, 49);

        Timeline editSourcesTimeLine =  new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            smokeSimulation.removeSource(15, 48, 15); // Usuwamy źródło dymu
            smokeSimulation.addSource(48, 48, 1);
        }));

        editSourcesTimeLine.setCycleCount(1);
        editSourcesTimeLine.play();

        // Timeline symulacji
        Timeline timeline = getTimeline(smokeSimulation);
        timeline.play();
    }

    private Timeline getTimeline(SmokeSimulation smokeSimulation) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            smokeSimulation.update();
            for (int i = 0; i < smokeSimulation.room.gridSize[0]; i++) {
                for (int j = 0; j < smokeSimulation.room.gridSize[1]; j++) {
                    for (int k = 0; k < smokeSimulation.room.gridSize[2]; k++) {
                        double density = smokeSimulation.room.density[i][j][k];
                        if (smokeSimulation.room.isSource[i][j][k]) {
                            boxGrid[i][j][k].setMaterial(new PhongMaterial(Color.BLUE));
                            boxGrid[i][j][k].setVisible(true);
                        } else if (smokeSimulation.room.isBarrier[i][j][k]) {
                            boxGrid[i][j][k].setMaterial(new PhongMaterial(Color.RED));
                            boxGrid[i][j][k].setVisible(true);
                        } else if (smokeSimulation.room.isWindSource[i][j][k]) {
                            boxGrid[i][j][k].setMaterial(new PhongMaterial(Color.LIMEGREEN));
                            boxGrid[i][j][k].setVisible(true);
                        } else if (density > 0.3) {
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
        } else {
            return materials[9];
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
