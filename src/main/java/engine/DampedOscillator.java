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

    public DampedOscillator(double K, double gamma, double mass, double deltaT) {
        this.K = K;
        this.gamma = gamma;
        this.mass = mass;
        this.deltaT = deltaT;
        double A = 1;
        particle = new Particle(0, 0, 0, 0, -A*gamma/(2*mass), 0, 0);
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
        return new DampedOscillator(K, gamma, mass, deltaT);
    }

    public static void main(String[] args) {
        MovementModel model = new DampedOscillator(Math.pow(10, 4), 100, 70, Math.pow(10, -2));
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
