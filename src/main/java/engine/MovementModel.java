package engine;

import java.util.List;

public interface MovementModel {

    double mass();

    List<Particle> particles();

    double deltaT();

    MovementModel hardCopyModel();

    boolean isForceFunctionSpeedDependant();

    int particleCount();

    double[][] getForceMatrix();

    default double[][] getR2Matrix() {
        double[][] F = getForceMatrix();
        double[][] R2 = new double[particleCount()][Particle.DIMENSION];
        for (int i = 0; i < particleCount(); i++)
            for (int j = 0; j < Particle.DIMENSION; j++)
                R2[i][j] = F[i][j] / mass();
        return R2;
    }

    double[][] computeR2FromState(double[][] positions, double[][] velocities);


    double[][] getR3Matrix();

    double[][] getR4Matrix();

    double[][] getR5Matrix();

    void particleSort();
}
