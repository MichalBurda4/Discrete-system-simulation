package pl.edu.agh.ssd;

public class SmokeSimulation {

    /**
     * Siatka pomieszczenia
     */
    SmokeGrid room;
    /**
     * Krok czasowy
     */
    double timeStep;
    /**
     * Predkość dymu wydostającego się ze źródła
     */
    double defaultSourceVelocity;
    /**
     * Gęstość dymu wydostającego się ze źródła
     */
    double defaultSourceDensity;

    /**
     * Konstruktor solvera symulacji
     * @param width Szerokość pomieszczenia
     * @param height Wysokość pomieszczenia
     * @param depth Głębokość pomieszczenia
     * @param timeStep krok czasowy
     * @param defaultSourceVelocity Predkość dymu wydostającego się ze źródła
     * @param defaultSourceDensity Gęstość dymu wydostającego się ze źródła
     */
    SmokeSimulation(int width, int height, int depth, double timeStep, double defaultSourceVelocity, double defaultSourceDensity) {
        this.room = new SmokeGrid(width, height, depth);
        this.timeStep = timeStep;
        this.defaultSourceVelocity = defaultSourceVelocity;
        this.defaultSourceDensity = defaultSourceDensity;
    }

    /**
     * Krok głównej pętli symulacji
     */
    private void step(){
//        addBuoyancy();
//        addWind();
        diffuse();
        advect();
//        enforceBoundaryConditions();
        project();
    }

    /**
     * Metoda dodająca jasność dymu(density)
     * @param x współrzędna x
     * @param y współrzędna y
     * @param z współrzędna z
     * @param amount ilość jasności która chcemy dodać
     */
    public void addDensity(int x, int y, int z, double amount) {
        this.room.density[x][y][z] += amount;
    }

    /**
     * Metoda dodająca prędkość do komórki
     * @param x współrzędna x
     * @param y współrzędna y
     * @param z współrzędna z
     * @param amountX dodawana prędkość w osi x
     * @param amountY dodawana prędkość w osi y
     * @param amountZ dodawana prędkość w osi z
     */
    public void addVelocity(int x, int y, int z, double amountX, double amountY, double amountZ) {
        this.room.velocityX[x][y][z] += amountX;
        this.room.velocityY[x][y][z] += amountY;
        this.room.velocityZ[x][y][z] += amountZ;
    }


    /**
     * Metoda ustawiająca przeszkodę (w postaci punktu)
     * @param x współrzęda osi X
     * @param y współrzęda osi Y
     * @param z współrzęda osi Z
     */
    public void addBound(int x, int y, int z) {
        this.room.isBarrier[y][x][z] = true;
    }

    /**
     * Metoda ustawiajaca przeszkodę (w postaci lini lub prostopadłościanu zależnie od doboru argumentów) w pokoju.
     * Argumentami są przedziały w trzech osiach X, Y i Z. Używając tej metody kilka razy można utowrzyć np krzesło z "klocków"
     * @param startX początkowy indeks X
     * @param endX końcowy indeks X
     * @param startY początkowy indeks Y
     * @param endY końcowy indeks Y
     * @param startZ początkowy indeks Z
     * @param endZ końcowy indeks Z
     */
    public void addBound(int startX, int endX, int startY, int endY, int startZ, int endZ) {
        for(int i = startX; i <= endX; i++) {
            for(int j = startY; j <= endY; j++) {
                for(int k = startZ; k <= endZ; k++) {
                    this.room.isBarrier[i][j][k] = true;
                }
            }
        }
    }

    /**
     * Metoda ustawiająca zródło ciepła
     * @param x współrzęda osi X
     * @param y współrzęda osi Y
     * @param z współrzęda osi Z
     */
    public void addSource(int x, int y, int z) {
        this.room.isSource[y][x][z] = true;
        this.room.density[x][y][z] = defaultSourceDensity;
        this.room.velocityY[x][y][z] = defaultSourceVelocity;
    }

//    Przydatny moze sie okazac jeden z tych projektow
//    https://github.com/deltabrot/fluid-dynamics/blob/master/src/NavierStokesSolver.java
//    https://github.com/davidmoten/jns
//    Moze nie byc w nich uwzglednienie wiatru i temperatury. Tym zajmiemy sie w kolejnych etapach
//    Najwazniejsze jest diffuse, advect i project
//
    private void diffuse(){

//        Ta metoda odpowiada za rozpraszanie właściwości płynu (np. gęstości, temperatury, prędkości) w czasie.
//         Rozpraszanie modeluje dyfuzję, czyli proces wyrównywania wartości w płynie.
    }

    private void advect(){
//        Adwekcja odpowiada za przemieszczanie właściwości płynu (np. dymu lub temperatury) zgodnie z jego prędkością.
//         To krok, który odpowiada za transport w przestrzeni.
    }

    private void project(){
//        Ten krok zapewnia, że symulacja zachowuje zasadę nieściśliwości płynu (np. powietrze/dym traktujemy jako nieściśliwy).
//         W tym celu metoda usuwa składową wiru z pola prędkości.
    }

    private void enforceBoundaryConditions(){
//        Ten krok dodaje odbijanie się dymu od krawędzi pomieszczenia i przeszkód
    }

    private void addBuoyancy(){
//        Ten krok dodaje siłę wyporu wynikającą z różnicy temperatury. Gorętszy dym unosi się w górę, a chłodniejszy opada
    }

    private void addWind(){
//        Ten krok dodaje oddziaływanie wiatru na komórki
    }

}
