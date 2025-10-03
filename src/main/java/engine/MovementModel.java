package engine;

import java.util.List;
import java.util.function.BiFunction;

public interface MovementModel {

    BiFunction<Double, Double, Double> forceFunction();

    default BiFunction<Double, Double, Double> getR2() {
        return (pos, speed) -> forceFunction().apply(pos, speed) / mass();
    }

    BiFunction<Double, Double, Double> getR3();

    BiFunction<Double, Double, Double> getR4();

    BiFunction<Double, Double, Double> getR5();

    double mass();

    List<Particle> particles();

    double deltaT();

    MovementModel hardCopyModel();

    boolean isForceFunctionSpeedDependant();
}
