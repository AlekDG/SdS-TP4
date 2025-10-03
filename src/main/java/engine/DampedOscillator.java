package engine;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

public class DampedOscillator implements MovementModel {

    private final double K;
    private final double gamma;
    private final double mass;
    private final Particle particle;
    private final double deltaT;

    public DampedOscillator(double K, double gamma, double A, double mass, double deltaT) {
        this(
                K, gamma, mass, deltaT,
                new Particle(0, A, 0, 0, -A*gamma/(2*mass), 0, 0)
        );
    }

    private DampedOscillator(double K, double gamma, double mass, double deltaT, Particle particle) {
        this.K = K;
        this.gamma = gamma;
        this.mass = mass;
        this.deltaT = deltaT;
        this.particle = particle;
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

    @Override
    public MovementModel hardCopyModel() {
        return new DampedOscillator(K, gamma, mass, deltaT, particle.hardCopy());
    }

    public static void main(String[] args) {
        MovementModel model = new DampedOscillator(Math.pow(10, 4), 100, 1, 70, Math.pow(10, -2));
        EstimationMethod estimationMethod = new EstimationMethod(model, 5);
        Iterator<Time> timeIt;
        timeIt = estimationMethod.verletEstimation();
        while (timeIt.hasNext()) {
            Time time = timeIt.next();
            // Post Process
        }

        timeIt = estimationMethod.beemanEstimation();
        while (timeIt.hasNext()) {
            Time time = timeIt.next();
            // Post Process
        }

        timeIt = estimationMethod.gearEstimation();
        while (timeIt.hasNext()) {
            Time time = timeIt.next();
            // Post Process
        }
    }
}
