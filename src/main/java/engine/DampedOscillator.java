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

    private BinaryOperator<Double> getR3() {
        return (r1, r2) -> forceFunction().apply(r1, r2) / mass;
    }

    private BinaryOperator<Double> getR4() {
        return (r2, r3) -> forceFunction().apply(r2, r3) / mass;
    }

    private BinaryOperator<Double> getR5() {
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
        double[][] forceMatrix = {{0, 0, 0}};
        forceMatrix[0][1] = forceFunction().apply(particle.getY(), particle.getSpeedY());
        return forceMatrix;
    }

    @Override
    public double[][] computeR2FromState(double[][] positions, double[][] velocities) {
        double[][] R2Matrix = new double[1][Particle.DIMENSION];
        R2Matrix[0][1] = forceFunction().apply(positions[0][1], velocities[0][1]) / mass();
        return R2Matrix;
    }

    @Override
    public double[][] getR3Matrix() {
        double[][] R2Matrix = getR2Matrix();
        double[][] R3Matrix = new double[1][Particle.DIMENSION];
        R3Matrix[0][1] = getR3().apply(particle.getSpeedY(), R2Matrix[0][1]);
        return R3Matrix;
    }

    @Override
    public double[][] getR4Matrix() {
        double[][] R2Matrix = getR2Matrix();
        double[][] R3Matrix = getR3Matrix();
        double[][] R4Matrix = new double[1][Particle.DIMENSION];
        R4Matrix[0][1] = getR4().apply(R2Matrix[0][1], R3Matrix[0][1]);
        return R4Matrix;
    }

    @Override
    public double[][] getR5Matrix() {
        double[][] R3Matrix = getR3Matrix();
        double[][] R4Matrix = getR4Matrix();
        double[][] R5Matrix = new double[1][Particle.DIMENSION];
        R5Matrix[0][1] = getR5().apply(R3Matrix[0][1], R4Matrix[0][1]);
        return R5Matrix;
    }

    @Override
    public MovementModel hardCopyModel() {
        return new DampedOscillator(K, gamma, mass, deltaT, particle.hardCopy());
    }
}
