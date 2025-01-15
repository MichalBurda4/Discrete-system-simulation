package pl.edu.agh.ssd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    ArrayList<ArrayList<Integer>> bounds = new ArrayList<>();

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
     * Metoda ustawiająca przeszkodę (w postaci punktu)
     *
     * @param x współrzęda osi X
     * @param y współrzęda osi Y
     * @param z współrzęda osi Z
     */
    public void addBound(int x, int y, int z) {
        this.room.isBarrier[x][y][z] = true;
        bounds.add(new ArrayList<>(Arrays.asList(x, y, z)));
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
                    addBound(i, j, k);
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

    public void removeSource(int x, int y, int z) {
        this.room.isSource[x][y][z] = false;
        this.room.density[x][y][z] = 0;
        this.room.velocityY[x][y][z] = 0;
    }

    public void addWind(int x, int y, int z, double velocityX, double velocityY, double velocityZ) {
        this.room.isWindSource[x][y][z] = true;
        this.room.velocityX[x][y][z] = velocityX;
        this.room.velocityY[x][y][z] = velocityY;
        this.room.velocityZ[x][y][z] = velocityZ;
    }

    //    Przydatny moze sie okazac jeden z tych projektow
//    https://github.com/deltabrot/fluid-dynamics/blob/master/src/NavierStokesSolver.java
//    https://github.com/davidmoten/jns
//    Moze nie byc w nich uwzglednienie wiatru i temperatury. Tym zajmiemy sie w kolejnych etapach
//    Najwazniejsze jest diffuse, advect i project
//


    //        Ta metoda odpowiada za rozpraszanie właściwości płynu (np. gęstości, temperatury, prędkości) w czasie.
//         Rozpraszanie modeluje dyfuzję, czyli proces wyrównywania wartości w płynie.
    private void diffuse(int b, double[][][] current, double[][][] previous, double diffRate) {

        double a = timeStep * diffRate * (room.gridSize[0] - 2) * (room.gridSize[1] - 2);
        for (int iteration = 0; iteration < 4; iteration++) {
            for (int x = 1; x < room.gridSize[0] - 1; x++) {
                for (int y = 1; y < room.gridSize[1] - 1; y++) {
                    for (int z = 1; z < room.gridSize[2] - 1; z++) {
                        if (room.isSource[x][y][z]) continue;
                        if (room.isBarrier[x][y][z]) continue;
                        if(room.isWindSource[x][y][z]) continue;
                        current[x][y][z] = (previous[x][y][z] + a * (
                                current[x + 1][y][z] + current[x - 1][y][z] +
                                        current[x][y + 1][z] + current[x][y - 1][z] +
                                        current[x][y][z + 1] + current[x][y][z - 1]
                        )) / (1 + 6 * a);

                    }
                }
            }
            enforceBoundaryConditions(b, current);
        }
    }


    private void diffuseParallel(int b, double[][][] current, double[][][] previous, double diffRate) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        double a = timeStep * diffRate * (room.gridSize[0] - 2) * (room.gridSize[1] - 2);

        List<Callable<Void>> tasks = new ArrayList<>();

        for (int x = 1; x < room.gridSize[0] - 1; x++) {
            final int currentX = x;
            tasks.add(() -> {
                for (int y = 1; y < room.gridSize[1] - 1; y++) {
                    for (int z = 1; z < room.gridSize[2] - 1; z++) {
                        if (room.isSource[currentX][y][z] || room.isBarrier[currentX][y][z]) continue;
                        current[currentX][y][z] = (previous[currentX][y][z] + a * (
                                current[currentX + 1][y][z] + current[currentX - 1][y][z] +
                                        current[currentX][y + 1][z] + current[currentX][y - 1][z] +
                                        current[currentX][y][z + 1] + current[currentX][y][z - 1]
                        )) / (1 + 6 * a);
                    }
                }
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();  // Opcjonalne logowanie błędu
        } finally {
            executor.shutdown();
        }

        enforceBoundaryConditions(b, current);
    }


    private void advect(int b, double[][][] current, double[][][] previous, double[][][] velocityX, double[][][] velocityY, double[][][] velocityZ) {
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
                    if (room.isSource[i][j][k]) continue;
                    if (room.isBarrier[i][j][k]) continue;
                    if(room.isWindSource[i][j][k]) continue;
                    tmp1 = dtx * velocityX[i][j][k];
                    tmp2 = dty * velocityY[i][j][k];
                    tmp3 = dtz * velocityZ[i][j][k];
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
                    if (i0i > room.gridSize[0] - 1 || i1i > room.gridSize[0] - 1) continue;
                    if (j0i > room.gridSize[1] - 1 || j1i > room.gridSize[1] - 1) continue;
                    if (k0i > room.gridSize[2] - 1 || k1i > room.gridSize[2] - 1) continue;
                    current[i][j][k] =
                            s0 * (t0 * (u0 * previous[i0i][j0i][k0i]
                                    + u1 * previous[i0i][j0i][k1i])
                                    + (t1 * (u0 * previous[i0i][j1i][k0i]
                                    + u1 * previous[i0i][j1i][k1i])))
                                    + s1 * (t0 * (u0 * previous[i1i][j0i][k0i]
                                    + u1 * previous[i1i][j0i][k1i])
                                    + (t1 * (u0 * previous[i1i][j1i][k0i]
                                    + u1 * previous[i1i][j1i][k1i])));
                }
            }
        }
        enforceBoundaryConditions(b, current);
    }

    private void advectParallel(int b, double[][][] current, double[][][] previous,
                        double[][][] velocityX, double[][][] velocityY, double[][][] velocityZ) {
        double dtx = timeStep * (room.gridSize[0] - 2);
        double dty = timeStep * (room.gridSize[1] - 2);
        double dtz = timeStep * (room.gridSize[2] - 2);

        List<Callable<Void>> tasks = new ArrayList<>();

        // Zastosuj równoległość na osiach X, Y i Z.
        for (int k = 1; k < room.gridSize[2] - 1; k++) {
            final int currentK = k;
            tasks.add(() -> {
                for (int j = 1; j < room.gridSize[1] - 1; j++) {
                    for (int i = 1; i < room.gridSize[0] - 1; i++) {
                        if (room.isSource[i][j][currentK] || room.isBarrier[i][j][currentK]) continue;

                        double tmp1 = dtx * velocityX[i][j][currentK];
                        double tmp2 = dty * velocityY[i][j][currentK];
                        double tmp3 = dtz * velocityZ[i][j][currentK];
                        double x = i - tmp1;
                        double y = j - tmp2;
                        double z = currentK - tmp3;

                        // Clamping coordinates to grid bounds
                        x = Math.max(0.5, Math.min(x, room.gridSize[0] + 0.5));
                        y = Math.max(0.5, Math.min(y, room.gridSize[1] + 0.5));
                        z = Math.max(0.5, Math.min(z, room.gridSize[2] + 0.5));

                        int i0 = (int) Math.floor(x);
                        int i1 = i0 + 1;
                        int j0 = (int) Math.floor(y);
                        int j1 = j0 + 1;
                        int k0 = (int) Math.floor(z);
                        int k1 = k0 + 1;

                        double s1 = x - i0;
                        double s0 = 1 - s1;
                        double t1 = y - j0;
                        double t0 = 1 - t1;
                        double u1 = z - k0;
                        double u0 = 1 - u1;

                        current[i][j][currentK] = s0 * (t0 * (u0 * previous[i0][j0][k0] + u1 * previous[i0][j0][k1])
                                + t1 * (u0 * previous[i0][j1][k0] + u1 * previous[i0][j1][k1]))
                                + s1 * (t0 * (u0 * previous[i1][j0][k0] + u1 * previous[i1][j0][k1])
                                + t1 * (u0 * previous[i1][j1][k0] + u1 * previous[i1][j1][k1]));
                    }
                }
                return null;
            });
        }

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        enforceBoundaryConditions(b, current);
    }



    //Ten krok zapewnia, że symulacja zachowuje zasadę nieściśliwości płynu (np. powietrze/dym traktujemy jako nieściśliwy).
//W tym celu metoda usuwa składową wiru z pola prędkości.
    private void project(double[][][] velocityX, double[][][] velocityY, double[][][] velocityZ,
                         double[][][] pressure, double[][][] divergence) {
        // Oblicz dywergencję
        for (int x = 1; x < room.gridSize[0] - 1; x++) {
            for (int y = 1; y < room.gridSize[1] - 1; y++) {
                for (int z = 1; z < room.gridSize[2] - 1; z++) {
                    if (room.isSource[x][y][z]) continue;
                    if (room.isBarrier[x][y][z]) continue;
                    if(room.isWindSource[x][y][z]) continue;
                    divergence[x][y][z] = -0.5 * (
                            (velocityX[x + 1][y][z] - velocityX[x - 1][y][z]) / room.gridSize[0]
                                    + (velocityY[x][y + 1][z] - velocityY[x][y - 1][z]) / room.gridSize[1]
                                    + (velocityZ[x][y][z + 1] - velocityZ[x][y][z - 1]) / room.gridSize[2]);
                    pressure[x][y][z] = 0;
                }
            }
        }

        enforceBoundaryConditions(0, divergence);
        enforceBoundaryConditions(0, pressure);

        // Rozwiąż równe Laplace’a dla ciśnienia
        for (int iteration = 0; iteration < 4; iteration++) {
            for (int x = 1; x < room.gridSize[0] - 1; x++) {
                for (int y = 1; y < room.gridSize[1] - 1; y++) {
                    for (int z = 1; z < room.gridSize[2] - 1; z++) {
                        if (room.isSource[x][y][z]) continue;
                        if (room.isBarrier[x][y][z]) continue;
                        if(room.isWindSource[x][y][z]) continue;
                        pressure[x][y][z] = (divergence[x][y][z] +
                                pressure[x + 1][y][z] + pressure[x - 1][y][z] +
                                pressure[x][y + 1][z] + pressure[x][y - 1][z] +
                                pressure[x][y][z + 1] + pressure[x][y][z - 1]) / 6;
                    }
                }
            }
            enforceBoundaryConditions(0, pressure);
        }

        // Zaktualizuj pole prędkości
        for (int x = 1; x < room.gridSize[0] - 1; x++) {
            for (int y = 1; y < room.gridSize[1] - 1; y++) {
                for (int z = 1; z < room.gridSize[2] - 1; z++) {
                    if (room.isSource[x][y][z]) continue;
                    if (room.isBarrier[x][y][z]) continue;
                    if(room.isWindSource[x][y][z]) continue;
                    velocityX[x][y][z] -= 0.5 * (pressure[x + 1][y][z] - pressure[x - 1][y][z]) * room.gridSize[0];
                    velocityY[x][y][z] -= 0.5 * (pressure[x][y + 1][z] - pressure[x][y - 1][z]) * room.gridSize[1];
                    velocityZ[x][y][z] -= 0.5 * (pressure[x][y][z + 1] - pressure[x][y][z - 1]) * room.gridSize[2];
                }
            }
        }
        enforceBoundaryConditions(1, velocityX);
        enforceBoundaryConditions(2, velocityY);
        enforceBoundaryConditions(3, velocityZ);
    }

    private void projectParallel(double[][][] velocityX, double[][][] velocityY, double[][][] velocityZ,
                         double[][][] pressure, double[][][] divergence) {

        List<Callable<Void>> tasks = new ArrayList<>();

        // Oblicz dywergencję równolegle
        for (int x = 1; x < room.gridSize[0] - 1; x++) {
            final int currentX = x;
            tasks.add(() -> {
                for (int y = 1; y < room.gridSize[1] - 1; y++) {
                    for (int z = 1; z < room.gridSize[2] - 1; z++) {
                        if (room.isSource[currentX][y][z] || room.isBarrier[currentX][y][z]) continue;
                        divergence[currentX][y][z] = -0.5 * (
                                (velocityX[currentX + 1][y][z] - velocityX[currentX - 1][y][z]) / room.gridSize[0] +
                                        (velocityY[currentX][y + 1][z] - velocityY[currentX][y - 1][z]) / room.gridSize[1] +
                                        (velocityZ[currentX][y][z + 1] - velocityZ[currentX][y][z - 1]) / room.gridSize[2]);
                        pressure[currentX][y][z] = 0;
                    }
                }
                return null;
            });
        }

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        enforceBoundaryConditions(0, divergence);
        enforceBoundaryConditions(0, pressure);

        // Rozwiąż równanie Laplace'a dla ciśnienia (Równolegle)
        tasks.clear();
        for (int iteration = 0; iteration < 4; iteration++) {
            for (int x = 1; x < room.gridSize[0] - 1; x++) {
                final int currentX = x;
                tasks.add(() -> {
                    for (int y = 1; y < room.gridSize[1] - 1; y++) {
                        for (int z = 1; z < room.gridSize[2] - 1; z++) {
                            if (room.isSource[currentX][y][z] || room.isBarrier[currentX][y][z]) continue;
                            pressure[currentX][y][z] = (divergence[currentX][y][z] +
                                    pressure[currentX + 1][y][z] + pressure[currentX - 1][y][z] +
                                    pressure[currentX][y + 1][z] + pressure[currentX][y - 1][z] +
                                    pressure[currentX][y][z + 1] + pressure[currentX][y][z - 1]) / 6;
                        }
                    }
                    return null;
                });
            }

            try {
                executor.invokeAll(tasks);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            enforceBoundaryConditions(0, pressure);
        }

        // Aktualizuj pole prędkości (Równolegle)
        tasks.clear();
        for (int x = 1; x < room.gridSize[0] - 1; x++) {
            final int currentX = x;
            tasks.add(() -> {
                for (int y = 1; y < room.gridSize[1] - 1; y++) {
                    for (int z = 1; z < room.gridSize[2] - 1; z++) {
                        if (room.isSource[currentX][y][z] || room.isBarrier[currentX][y][z]) continue;
                        velocityX[currentX][y][z] -= 0.5 * (pressure[currentX + 1][y][z] - pressure[currentX - 1][y][z]) * room.gridSize[0];
                        velocityY[currentX][y][z] -= 0.5 * (pressure[currentX][y + 1][z] - pressure[currentX][y - 1][z]) * room.gridSize[1];
                        velocityZ[currentX][y][z] -= 0.5 * (pressure[currentX][y][z + 1] - pressure[currentX][y][z - 1]) * room.gridSize[2];
                    }
                }
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        enforceBoundaryConditions(1, velocityX);
        enforceBoundaryConditions(2, velocityY);
        enforceBoundaryConditions(3, velocityZ);
    }



    //    to jest test i raczej nie bedzie uzywane ale narazie zostawiam
    private void handleCollisionVelocity(int b, double[][][] x){
        for (ArrayList<Integer> bound : bounds){
            if(bound.get(0) == 0 || bound.get(1) == 0 || bound.get(2) == 0 || bound.get(0) == 31 || bound.get(1) == 31 || bound.get(2) == 31){
                continue;
            }
            if (b == 3){
                if (x[bound.get(0)][bound.get(1)][bound.get(2)] > 0){
                    x[bound.get(0)][bound.get(1)][bound.get(2)] = -x[bound.get(0)][bound.get(1)][bound.get(2) + 1];
                }
                else{
                    x[bound.get(0)][bound.get(1)][bound.get(2)] = -x[bound.get(0)][bound.get(1)][bound.get(2) - 1];
                }
            }
            else if (b == 2){
                if (x[bound.get(0)][bound.get(1)][bound.get(2)] > 0){
                    x[bound.get(0)][bound.get(1)][bound.get(2)] = -x[bound.get(0)][bound.get(1) + 1][bound.get(2)];
                }
                else{
                    x[bound.get(0)][bound.get(1)][bound.get(2)] = -x[bound.get(0)][bound.get(1) - 1][bound.get(2)];
                }
            }
            else if (b == 1){
                if (x[bound.get(0)][bound.get(1)][bound.get(2)] > 0){
                    x[bound.get(0)][bound.get(1)][bound.get(2)] = -x[bound.get(0) + 1][bound.get(1)][bound.get(2)];
                }
                else{
                    x[bound.get(0)][bound.get(1)][bound.get(2)] = -x[bound.get(0) - 1][bound.get(1)][bound.get(2)];
                }
            }
        }
    }

//    to jest test i raczej nie bedzie uzywane ale narazie zostawiam
    private void handleCollisionDensity(int b, double[][][] x){
        for (ArrayList<Integer> bound : bounds){
            x[bound.get(0)][bound.get(1)][bound.get(2)] = x[bound.get(0)][bound.get(1) - 1][bound.get(2)];  //to jest wielkie uproszczenie
        }
    }

    private void enforceBoundaryConditions(int b, double[][][] x) {
        int sizeX = room.gridSize[0];
        int sizeY = room.gridSize[1];
        int sizeZ = room.gridSize[2];

        //    to jest test i raczej nie bedzie uzywane ale narazie zostawiam
//        if (b == 0){
//            handleCollisionDensity(b, x);
//        }
//        else {
//            handleCollisionVelocity(b, x);
//        }


        for (int j = 1; j < sizeY - 1; j++) {
            for (int i = 1; i < sizeX - 1; i++) {
                x[i][j][0] = b == 3 ? -x[i][j][1] : x[i][j][1];
                x[i][j][sizeZ - 1] = b == 3 ? -x[i][j][sizeZ - 2] : x[i][j][sizeZ - 2];
            }
        }
        for (int k = 1; k < sizeZ - 1; k++) {
            for (int i = 1; i < sizeX - 1; i++) {
                x[i][0][k] = b == 2 ? -x[i][1][k] : x[i][1][k];
                x[i][sizeY - 1][k] = b == 2 ? -x[i][sizeY - 2][k] : x[i][sizeY - 2][k];
            }
        }
        for (int k = 1; k < sizeZ - 1; k++) {
            for (int j = 1; j < sizeY - 1; j++) {
                x[0][j][k] = b == 1 ? -x[1][j][k] : x[1][j][k];
                x[sizeX - 1][j][k] = b == 1 ? -x[sizeX - 2][j][k] : x[sizeX - 2][j][k];
            }
        }
        x[0][0][0] = 0.33 * (x[1][0][0] + x[0][1][0] + x[0][0][1]);
        x[0][sizeY - 1][0] = 0.33 * (x[1][sizeY - 1][0] + x[0][sizeY - 2][0] + x[0][sizeY - 1][1]);
        x[0][0][sizeZ - 1] = 0.33 * (x[1][0][sizeZ - 1] + x[0][1][sizeZ - 1] + x[0][0][sizeZ - 2]);
        x[sizeX - 1][0][0] = 0.33 * (x[sizeX - 2][0][0] + x[sizeX - 1][1][0] + x[sizeX - 1][0][1]);
        x[0][sizeY - 1][sizeZ - 1] = 0.33 * (x[1][sizeY - 1][sizeZ - 1] + x[0][sizeY - 2][sizeZ - 1] + x[0][sizeY - 1][sizeZ - 2]);
        x[sizeX - 1][sizeY - 1][0] = 0.33 * (x[sizeX - 2][sizeY - 1][0] + x[sizeX - 1][sizeY - 2][0] + x[sizeX - 1][sizeY - 1][1]);
        x[sizeX - 1][0][sizeZ - 1] = 0.33 * (x[sizeX - 2][0][sizeZ - 1] + x[sizeX - 1][0][sizeZ - 2] + x[sizeX - 1][1][sizeZ - 1]);
        x[sizeX - 1][sizeY - 1][sizeZ - 1] = 0.33 * (x[sizeX - 2][sizeY - 1][sizeZ - 1] + x[sizeX - 1][sizeY - 2][sizeZ - 1] + x[sizeX - 1][sizeY - 1][sizeZ - 2]);
    }

    private void addBuoyancy() {
//        Ten krok dodaje siłę wyporu wynikającą z różnicy temperatury. Gorętszy dym unosi się w górę, a chłodniejszy opada
    }

    // Główna metoda aktualizująca symulację
    public void update() {

        // Rozpraszanie
        diffuse(1, room.velocityX, room.prevVelocityX, diffRate);
        diffuse(2, room.velocityY, room.prevVelocityY, diffRate);
        diffuse(3, room.velocityZ, room.prevVelocityZ, diffRate);

        project(room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ, room.velocityX, room.velocityY);

        advect(1, room.velocityX, room.prevVelocityX, room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ);
        advect(2, room.velocityY, room.prevVelocityY, room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ);
        advect(3, room.velocityZ, room.prevVelocityZ, room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ);

        project(room.velocityX, room.velocityY, room.velocityZ, room.prevVelocityX, room.prevVelocityY);

        diffuse(0, room.prevDensity, room.density, diffRate);

        advect(0, room.density, room.prevDensity, room.velocityX, room.velocityY, room.velocityZ);
//
//        diffuseParallel(1, room.velocityX, room.prevVelocityX, diffRate);
//        diffuseParallel(2, room.velocityY, room.prevVelocityY, diffRate);
//        diffuseParallel(3, room.velocityZ, room.prevVelocityZ, diffRate);
//
//        projectParallel(room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ, room.velocityX, room.velocityY);
//
//        advectParallel(1, room.velocityX, room.prevVelocityX, room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ);
//        advectParallel(2, room.velocityY, room.prevVelocityY, room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ);
//        advectParallel(3, room.velocityZ, room.prevVelocityZ, room.prevVelocityX, room.prevVelocityY, room.prevVelocityZ);
//
//        projectParallel(room.velocityX, room.velocityY, room.velocityZ, room.prevVelocityX, room.prevVelocityY);
//
//        diffuseParallel(0, room.prevDensity, room.density, diffRate);
//
//        advectParallel(0, room.density, room.prevDensity, room.velocityX, room.velocityY, room.velocityZ);

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
