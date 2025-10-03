package engine;

import java.util.List;
import java.util.function.BiFunction;

public interface MovementModel extends Iterable<Time> {

    BiFunction<Double, Double, Double> forceFunction();

    double mass();

    List<Particle> particles();

    double deltaT();
}
