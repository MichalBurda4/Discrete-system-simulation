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
    double diffRate;

    /**
     * Konstruktor solvera symulacji
     *
     * @param width                 Szerokość pomieszczenia
     * @param height                Wysokość pomieszczenia
     * @param depth                 Głębokość pomieszczenia
     * @param timeStep              krok czasowy
     * @param defaultSourceVelocity Predkość dymu wydostającego się ze źródła
     * @param defaultSourceDensity  Gęstość dymu wydostającego się ze źródła
     */
    SmokeSimulation(int width, int height, int depth, double timeStep, double defaultSourceVelocity, double defaultSourceDensity, double diffRate) {
        this.room = new SmokeGrid(width, height, depth);
        this.timeStep = timeStep;
        this.defaultSourceVelocity = defaultSourceVelocity;
        this.defaultSourceDensity = defaultSourceDensity;
        this.diffRate = diffRate;
    }

    /**
     * Metoda dodająca jasność dymu(density)
     *
     * @param x      współrzędna x
     * @param y      współrzędna y
     * @param z      współrzędna z
     * @param amount ilość jasności która chcemy dodać
     */
    public void addDensity(int x, int y, int z, double amount) {
        this.room.density[x][y][z] += amount;
    }

    /**
     * Metoda dodająca prędkość do komórki
     *
     * @param x       współrzędna x
     * @param y       współrzędna y
     * @param z       współrzędna z
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
     *
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
     *
     * @param startX początkowy indeks X
     * @param endX   końcowy indeks X
     * @param startY początkowy indeks Y
     * @param endY   końcowy indeks Y
     * @param startZ początkowy indeks Z
     * @param endZ   końcowy indeks Z
     */
    public void addBound(int startX, int endX, int startY, int endY, int startZ, int endZ) {
        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                for (int k = startZ; k <= endZ; k++) {
                    this.room.isBarrier[i][j][k] = true;
                }
            }
        }
    }

    /**
     * Metoda ustawiająca zródło ciepła
     *
     * @param x współrzęda osi X
     * @param y współrzęda osi Y
     * @param z współrzęda osi Z
     */
    public void addSource(int x, int y, int z) {
        this.room.isSource[x][y][z] = true;
        this.room.density[x][y][z] = defaultSourceDensity;
        this.room.velocityY[x][y][z] = -defaultSourceVelocity;
    }

    //    Przydatny moze sie okazac jeden z tych projektow
//    https://github.com/deltabrot/fluid-dynamics/blob/master/src/NavierStokesSolver.java
//    https://github.com/davidmoten/jns
//    Moze nie byc w nich uwzglednienie wiatru i temperatury. Tym zajmiemy sie w kolejnych etapach
//    Najwazniejsze jest diffuse, advect i project
//


    //        Ta metoda odpowiada za rozpraszanie właściwości płynu (np. gęstości, temperatury, prędkości) w czasie.
//         Rozpraszanie modeluje dyfuzję, czyli proces wyrównywania wartości w płynie.
    private void diffuse(double[][][] current, double[][][] previous, double diffRate) {

        double a = timeStep * diffRate * room.gridSize[0] * room.gridSize[1];
        for (int iteration = 0; iteration < 5; iteration++) {
            for (int x = 1; x < room.gridSize[0] - 1; x++) {
                for (int y = 1; y < room.gridSize[1] - 1; y++) {
                    for (int z = 1; z < room.gridSize[2] - 1; z++) {

                        current[x][y][z] = (previous[x][y][z] + a * (
                                current[x + 1][y][z] + current[x - 1][y][z] +
                                        current[x][y + 1][z] + current[x][y - 1][z] +
                                        current[x][y][z + 1] + current[x][y][z - 1]
                        )) / (1 + 6 * a);

                    }
                }
            }
            enforceBoundaryConditions(); // Ustaw granice na krawędziach
        }
    }

//    Pierwsza wersja advect ktora nie jest juz uzywana
//Adwekcja odpowiada za przemieszczanie właściwości płynu (np. dymu lub temperatury) zgodnie z jego prędkością.
//To krok, który odpowiada za transport w przestrzeni.
    private void advect(double[][][] current, double[][][] previous,
                        double[][][] velocityX, double[][][] velocityY, double[][][] velocityZ) {
        for (int x = 1; x < room.gridSize[0] - 1; x++) {
            for (int y = 1; y < room.gridSize[1] - 1; y++) {
                for (int z = 1; z < room.gridSize[2] - 1; z++) {
                    double xPos = x - timeStep * velocityX[x][y][z];
                    double yPos = y - timeStep * velocityY[x][y][z];
                    double zPos = z - timeStep * velocityZ[x][y][z];

                    xPos = Math.max(0.5, Math.min(xPos, room.gridSize[0] - 1.5));
                    yPos = Math.max(0.5, Math.min(yPos, room.gridSize[1] - 1.5));
                    zPos = Math.max(0.5, Math.min(zPos, room.gridSize[2] - 1.5));

                    int x0 = (int) Math.floor(xPos);
                    int x1 = x0 + 1;
                    int y0 = (int) Math.floor(yPos);
                    int y1 = y0 + 1;
                    int z0 = (int) Math.floor(zPos);
                    int z1 = z0 + 1;

                    double s1 = xPos - x0;
                    double s0 = 1 - s1;
                    double t1 = yPos - y0;
                    double t0 = 1 - t1;
                    double u1 = zPos - z0;
                    double u0 = 1 - u1;

                    current[x][y][z] =
                            s0 * (t0 * (u0 * previous[x0][y0][z0] + u1 * previous[x0][y0][z1])
                                    + t1 * (u0 * previous[x0][y1][z0] + u1 * previous[x0][y1][z1]))
                                    + s1 * (t0 * (u0 * previous[x1][y0][z0] + u1 * previous[x1][y0][z1])
                                    + t1 * (u0 * previous[x1][y1][z0] + u1 * previous[x1][y1][z1]));
                }
            }
        }
        enforceBoundaryConditions(); // Ustaw granice na krawędziach
    }

    private void advectV2(double[][][] current, double[][][] previous, double[][][] velocityX, double[][][] velocityY, double[][][] velocityZ) {
        double i0, i1, j0, j1, k0, k1;
        double dtx = timeStep * (room.gridSize[0] - 2);
        double dty = timeStep * (room.gridSize[1] - 2);
        double dtz = timeStep * (room.gridSize[2] - 2);

        double s0, s1, t0, t1, u0, u1;
        double tmp1, tmp2, tmp3, x, y, z;
        double iFloat, jFloat, kFloat;
        int i, j, k;

        for (k = 1, kFloat = 1; k < room.gridSize[2] - 1; k++, kFloat++) {
            for (j = 1, jFloat = 1; j < room.gridSize[1] - 1; j++, jFloat++) {
                for (i = 1, iFloat = 1; i < room.gridSize[0] - 1; i++, iFloat++) {
                    tmp1 = dtx * room.velocityX[i][j][k];
                    tmp2 = dty * room.velocityY[i][j][k];
                    tmp3 = dtz * room.velocityZ[i][j][k];
                    x = iFloat - tmp1;
                    y = jFloat - tmp2;
                    z = kFloat - tmp3;

                    if (x < 0.5) x = 0.5;
                    if (x > room.gridSize[0] + 0.5) x = room.gridSize[0] + 0.5;
                    i0 = Math.floor(x);
                    i1 = i0 + 1;
                    if (y < 0.5) y = 0.5;
                    if (y > room.gridSize[1] + 0.5) y = room.gridSize[1] + 0.5;
                    j0 = Math.floor(y);
                    j1 = j0 + 1;
                    if (z < 0.5) z = 0.5;
                    if (z > room.gridSize[2] + 0.5) z = room.gridSize[2] + 0.5;
                    k0 = Math.floor(z);
                    k1 = k0 + 1;

                    s1 = x - i0;
                    s0 = 1 - s1;
                    t1 = y - j0;
                    t0 = 1 - t1;
                    u1 = z - k0;
                    u0 = 1 - u1;

                    int i0i = (int) i0;
                    int i1i = (int) i1;
                    int j0i = (int) j0;
                    int j1i = (int) j1;
                    int k0i = (int) k0;
                    int k1i = (int) k1;
                    if(i0i > room.gridSize[0] - 1 || i1i > room.gridSize[0] - 1) continue;
                    if(j0i > room.gridSize[1] - 1 || j1i > room.gridSize[1] - 1) continue;
                    if(k0i > room.gridSize[2] - 1 || k1i > room.gridSize[2] - 1) continue;
                    current[i][j][k] =
                           s0 * (t0 * (u0 * previous[i0i][j0i][k0i]
                                    +  u1 * previous[i0i][j0i][k1i])
                              + (t1 * (u0 * previous[i0i][j1i][k0i]
                                    +  u1 * previous[i0i][j1i][k1i])))
                         + s1 * (t0 * (u0 * previous[i1i][j0i][k0i]
                                    +  u1 * previous[i1i][j0i][k1i])
                              + (t1 * (u0 * previous[i1i][j1i][k0i]
                                    +  u1 * previous[i1i][j1i][k1i])));
                }
            }
        }
        enforceBoundaryConditions(); // Ustaw granice na krawędziach
    }


    //Ten krok zapewnia, że symulacja zachowuje zasadę nieściśliwości płynu (np. powietrze/dym traktujemy jako nieściśliwy).
//W tym celu metoda usuwa składową wiru z pola prędkości.
    private void project(double[][][] velocityX, double[][][] velocityY, double[][][] velocityZ,
                         double[][][] pressure, double[][][] divergence) {
        // Oblicz dywergencję
        for (int x = 1; x < room.gridSize[0] - 1; x++) {
            for (int y = 1; y < room.gridSize[1] - 1; y++) {
                for (int z = 1; z < room.gridSize[2] - 1; z++) {
                    divergence[x][y][z] = -0.5 * (
                            velocityX[x + 1][y][z] - velocityX[x - 1][y][z]
                                    + velocityY[x][y + 1][z] - velocityY[x][y - 1][z]
                                    + velocityZ[x][y][z + 1] - velocityZ[x][y][z - 1]
                    ) / room.gridSize[0];
                    pressure[x][y][z] = 0;
                }
            }
        }

        // Rozwiąż równe Laplace’a dla ciśnienia
        for (int iteration = 0; iteration < 20; iteration++) {
            for (int x = 1; x < room.gridSize[0] - 1; x++) {
                for (int y = 1; y < room.gridSize[1] - 1; y++) {
                    for (int z = 1; z < room.gridSize[2] - 1; z++) {
                        pressure[x][y][z] = (divergence[x][y][z] +
                                pressure[x + 1][y][z] + pressure[x - 1][y][z] +
                                pressure[x][y + 1][z] + pressure[x][y - 1][z] +
                                pressure[x][y][z + 1] + pressure[x][y][z - 1]) / 6;
                    }
                }
            }
        }

        // Zaktualizuj pole prędkości
        for (int x = 1; x < room.gridSize[0] - 1; x++) {
            for (int y = 1; y < room.gridSize[1] - 1; y++) {
                for (int z = 1; z < room.gridSize[2] - 1; z++) {
                    velocityX[x][y][z] -= 0.5 * (pressure[x + 1][y][z] - pressure[x - 1][y][z]) * room.gridSize[0];
                    velocityY[x][y][z] -= 0.5 * (pressure[x][y + 1][z] - pressure[x][y - 1][z]) * room.gridSize[1];
                    velocityZ[x][y][z] -= 0.5 * (pressure[x][y][z + 1] - pressure[x][y][z - 1]) * room.gridSize[2];
                }
            }
        }
    }


    private void enforceBoundaryConditions() {
//        Ten krok dodaje odbijanie się dymu od krawędzi pomieszczenia i przeszkód
    }

    private void addBuoyancy() {
//        Ten krok dodaje siłę wyporu wynikającą z różnicy temperatury. Gorętszy dym unosi się w górę, a chłodniejszy opada
    }

    private void addWind() {
//        Ten krok dodaje oddziaływanie wiatru na komórki
    }

    // Główna metoda aktualizująca symulację
    public void update() {

        // Rozpraszanie
        diffuse(room.velocityX, room.prevVelocityX, diffRate);
        diffuse(room.velocityY, room.prevVelocityY, diffRate);
        diffuse(room.velocityZ, room.prevVelocityZ, diffRate);

        project(room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ, room.pressure, room.divergence);

        advectV2(room.velocityX, room.prevVelocityX, room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ);
        advectV2(room.velocityY, room.prevVelocityY, room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ);
        advectV2(room.velocityZ, room.prevVelocityZ, room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ);

        project(room.velocityX, room.velocityY, room.velocityZ, room.pressure, room.divergence);

        diffuse(room.density, room.prevDensity, diffRate);

        advectV2(room.density, room.prevDensity, room.velocityX, room.velocityY, room.velocityZ);

        room.prevVelocityX = copy(room.velocityX);
        room.prevVelocityY = copy(room.velocityY);
        room.prevVelocityZ = copy(room.velocityZ);
        room.prevDensity = copy(room.density);

    }

    // Kopiowanie siatki 3D
    private double[][][] copyGrid(double[][][] grid) {
        int xSize = grid.length;
        int ySize = grid[0].length;
        int zSize = grid[0][0].length;

        double[][][] copy = new double[xSize][ySize][zSize];
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                System.arraycopy(grid[x][y], 0, copy[x][y], 0, zSize);
            }
        }
        return copy;
    }

    private double[][][] copy(double[][][] original) {
        double[][][] copy = new double[original.length][original[0].length][original[0][0].length];
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[i].length; j++) {
                System.arraycopy(original[i][j], 0, copy[i][j], 0, original[i][j].length);
            }
        }
        return copy;
    }

}
