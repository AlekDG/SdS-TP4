package engine;

import java.util.List;
import java.util.function.BinaryOperator;

public interface MovementModel {

    BinaryOperator<Double> getR2();

    BinaryOperator<Double> getR3();

    BinaryOperator<Double> getR4();

    BinaryOperator<Double> getR5();

    double mass();

    List<Particle> particles();

    double deltaT();

    MovementModel hardCopyModel();

    boolean isForceFunctionSpeedDependant();

    int particleCount();

    double[][] getForceMatrix();
}
