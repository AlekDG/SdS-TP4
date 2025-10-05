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
        double[][] R2Matrix = getForceMatrix();
        for (int i = 0; i < particleCount(); i++) {
            for (int j = 0; j < Particle.DIMENSION; j++) {
                R2Matrix[i][j] = R2Matrix[i][j] / mass();
            }
        }
        return R2Matrix;
    }

    double[][] computeR2FromState(double[][] positions, double[][] velocities);


    double[][] getR3Matrix();

    double[][] getR4Matrix();

    double[][] getR5Matrix();

}
