package engine;

import java.util.List;
import java.util.function.BiFunction;

public interface MovementModel {

    BiFunction<Double, Double, Double> forceFunction();

    double mass();

    List<Particle> particles();

    double deltaT();

    MovementModel hardCopyModel();
}
