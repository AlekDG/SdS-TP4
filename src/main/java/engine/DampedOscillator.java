package engine;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

public class DampedOscillator implements MovementModel {

    private final EstimationMethod estimationMethod;
    private final double K;
    private final double gamma;
    private final double mass;
    private final Particle particle;
    private final double deltaT;

    public DampedOscillator(EstimationMethod estimationMethod, double K, double gamma, double mass, double deltaT) {
        this.estimationMethod = estimationMethod;
        this.K = K;
        this.gamma = gamma;
        this.mass = mass;
        this.deltaT = deltaT;
        double A = 1;
        particle = new Particle(0, 0, 0, 0, -A*gamma/(2*mass), 0, 0);
    }

    @Override
    public Iterator<Time> iterator() {
        return null;
    }

    @Override
    public BiFunction<Double, Double, Double> forceFunction() {
        return (pos, speed) -> -K*pos - gamma*speed;
    }

    @Override
    public double mass() {
        return mass;
    }

    @Override
    public List<Particle> particles() {
        return List.of(particle);
    }

    @Override
    public double deltaT() {
        return deltaT;
    }
}
