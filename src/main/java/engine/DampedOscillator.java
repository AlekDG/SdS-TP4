package engine;

import tools.PostProcessor;

import java.io.IOException;
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
    public BiFunction<Double, Double, Double> getR3() {
        return (r1, r2) -> forceFunction().apply(r1, r2) / mass;
    }

    @Override
    public BiFunction<Double, Double, Double> getR4() {
        return (r2, r3) -> forceFunction().apply(r2, r3) / mass;
    }

    @Override
    public BiFunction<Double, Double, Double> getR5() {
        return (r3, r4) -> forceFunction().apply(r3, r4) / mass;
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
    public boolean isForceFunctionSpeedDependant() {
        return true;
    }

    @Override
    public MovementModel hardCopyModel() {
        return new DampedOscillator(K, gamma, mass, deltaT, particle.hardCopy());
    }

    public static void main(String[] args) throws IOException {
        MovementModel model = new DampedOscillator(Math.pow(10, 4), 100, 1, 70, Math.pow(10, -2));
        EstimationMethod estimationMethod = new EstimationMethod(model, 5);
        Iterator<Time> timeIt;
        timeIt = estimationMethod.verletEstimation();
        try (PostProcessor postProcessor = new PostProcessor("verlet.txt")) {
            timeIt.forEachRemaining(postProcessor::processTime);
        }

        timeIt = estimationMethod.beemanEstimation();
        try (PostProcessor postProcessor = new PostProcessor("beeman.txt")) {
            timeIt.forEachRemaining(postProcessor::processTime);
        }

        timeIt = estimationMethod.gearEstimation();
        try (PostProcessor postProcessor = new PostProcessor("gear.txt")) {
            timeIt.forEachRemaining(postProcessor::processTime);
        }
    }
}
