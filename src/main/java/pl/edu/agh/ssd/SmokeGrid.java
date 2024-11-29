package pl.edu.agh.ssd;

import javafx.scene.paint.Color;

public class SmokeGrid {

    /**
     * Rozmair siatki
     * gridSize[0] == X
     * gridSize[1] == Y
     * gridSize[2] == Z
     */
    int[] gridSize;

    /**
     * Aktualne prędkości komórki wzgledem odpowienich osi
     */
    double[][][] velocityX, velocityY, velocityZ;

    /**
     * Poprzedzające prędkości komórki wzgledem odpowienich osi
     */
    double[][][] prevVelocityX, prevVelocityY, prevVelocityZ;

    /**
     * Aktualna i poprzedzająca wartość temperatury w komórce
     */
    double[][][] temperature, prevTemperature;

    /**
     * Aktualna i poprzedzająca wartość ciśnienia w komórce
     */
    double[][][] pressure, prevPressure;

    /**
     * Gęstość dymu w komórce
     */
    double[][][] density;

    /**
     * Przeszkody i źródła dymu
     */
    boolean[][][] isBarrier, isSource;


    /**
     * Konstruktor siatki
     * @param X liczba komórek w siatce w osi X
     * @param Y liczba komórek w siatce w osi Y
     * @param Z liczba komórek w siatce w osi Z
     */
    public SmokeGrid(int X, int Y, int Z) {
        gridSize = new int[]{X, Y, Z};
        velocityX = new double[X][Y][Z];
        velocityY = new double[X][Y][Z];
        velocityZ = new double[X][Y][Z];
        prevVelocityX = new double[X][Y][Z];
        prevVelocityY = new double[X][Y][Z];
        prevVelocityZ = new double[X][Y][Z];
        temperature = new double[X][Y][Z];
        prevTemperature = new double[X][Y][Z];
        pressure = new double[X][Y][Z];
        prevPressure = new double[X][Y][Z];
        density = new double[X][Y][Z];
        isBarrier = new boolean[X][Y][Z];
        isSource = new boolean[X][Y][Z];

        initValues(velocityX, 0);
        initValues(velocityY, 0);
        initValues(velocityZ, 0);

        initValues(prevVelocityX, 0);
        initValues(prevVelocityY, 0);
        initValues(prevVelocityZ, 0);

        initValues(pressure, 101325);
        initValues(prevPressure, 101325);

        initValues(temperature, 293.15);
        initValues(prevTemperature, 293.15);

        initValues(density, 0);

        initBarrier(isBarrier);
        initBarrier(isSource);
    }

    /**
     * Metoda pomocnicza inicjalizujaca wartosci pól klasy SmokeGrid typu double[][][]
     * @param values tablica 3D dla której chcemy zainicjalizować wartości
     */
    private void initValues(double[][][] values, double value) {
        for(int x = 0; x < gridSize[0]; x++) {
            for(int y = 0; y < gridSize[1]; y++) {
                for(int z = 0; z < gridSize[2]; z++) {
                    values[x][y][z] = value;
                }
            }
        }
    }

    /**
     * Metoda pomocnicza inicjalizujaca wartosci pól klasy SmokeGrid typu boolean[][][]
     * @param values tablica 3D dla której chcemy zainicjalizować wartości
     */
    private void initBarrier(boolean[][][] values) {
        for(int x = 0; x < gridSize[0]; x++) {
            for(int y = 0; y < gridSize[1]; y++) {
                for(int z = 0; z < gridSize[2]; z++) {
                    values[x][y][z] = false;
                }
            }
        }
    }

    public static Color densityToColor(double density) {
        return new Color(0, 0, 0, density);
    }

    /**
     * Metoda tylko do testów wypisująca do konsoli przekrój siatki z barierami i zródłami dymu
     */
    public void printGrid2D(){
        for(int x = 0; x < gridSize[0]; x++){
            for(int y = 0; y < gridSize[1]; y++){
                if(isBarrier[x][y][0]){
                    System.out.print("X ");
                }
                else if (isSource[x][y][0]){
                    System.out.print("S ");
                }
                else {
                    System.out.print("O ");
                }
            }
            System.out.println();
        }
    }
}
