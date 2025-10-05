package engine;

import java.util.List;
import java.util.function.BinaryOperator;

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

    private BinaryOperator<Double> forceFunction() {
        return (pos, speed) -> -K*pos - gamma*speed;
    }

    @Override
    public BinaryOperator<Double> getR2() {
        return (pos, speed) -> forceFunction().apply(pos, speed) / mass();
    }

    @Override
    public BinaryOperator<Double> getR3() {
        return (r1, r2) -> forceFunction().apply(r1, r2) / mass;
    }

    @Override
    public BinaryOperator<Double> getR4() {
        return (r2, r3) -> forceFunction().apply(r2, r3) / mass;
    }

    @Override
    public BinaryOperator<Double> getR5() {
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
    public int particleCount() {
        return 1;
    }

    @Override
    public double[][] getForceMatrix() {
        double[][] forceMatrix = new double[1][1];
        forceMatrix[0][0] = forceFunction().apply(particle.getPositionAndSpeedPair()[0].getPos(),particle.getPositionAndSpeedPair()[0].getSpeed());
        return forceMatrix;
    }

    @Override
    public MovementModel hardCopyModel() {
        return new DampedOscillator(K, gamma, mass, deltaT, particle.hardCopy());
    }
}
