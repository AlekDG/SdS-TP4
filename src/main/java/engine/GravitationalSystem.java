package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GravitationalSystem implements MovementModel {
    private final List<Particle> particles;
    private final double mass;
    private final int particleCount;
    private final double G;
    private final double h;
    private final double GM2;

    public GravitationalSystem(List<Particle> particles, double mass, double G, double h) {
        this.particles = new ArrayList<>(particles);
        this.mass = mass;
        this.particleCount = particles.size();
        this.G = G;
        this.h = h;
        this.GM2 = this.mass * this.mass * this.G;
    }

    @Override
    public double mass() {
        return mass;
    }

    @Override
    public List<Particle> particles() {
        return particles;
    }

    @Override
    public int particleCount() {
        return particleCount;
    }

    @Override
    public MovementModel hardCopyModel() {
        List<Particle> newParticles = particles.stream().map(Particle::hardCopy).toList();
        return new GravitationalSystem(newParticles, mass, G, h);
    }

    public double systemEnergy(){
        return kineticEnergy()+potentialEnergy();
    }

    private double kineticEnergy(){
        double totalEnergy=0;
        for(Particle p : particles){
            totalEnergy += p.getSpeedAbs() * (mass / 2);
        }
        return totalEnergy;
    }

    private double potentialEnergy(){
        double energy = 0;
        for(Particle p : particles){
            for(int i = particles.indexOf(p) + 1; i < particleCount; i++){
                Particle p2 = particles.get(i);
                double dx = p.getX() - p2.getX();
                double dy = p.getY() - p2.getY();
                double dz = p.getZ() - p2.getZ();
                double r2 = dx*dx + dy*dy + dz*dz;
                energy += (-GM2) / Math.sqrt(r2 + h*h);
            }
        }
        return energy;
    }

    public double halfMassRadius(){
        //All particles have the same mass so the center of mass is the average of every particle's coordinates
        double cx = 0, cy = 0, cz = 0;
        for(Particle p : particles){
            cx += p.getX();
            cy += p.getY();
            cz += p.getZ();
        }
        cx /= particleCount;
        cy /= particleCount;
        cz /= particleCount;

        double[] squareDistances = new double[particleCount];
        for(Particle p : particles){
            double dx = p.getX() - cx;
            double dy = p.getY() - cy;
            double dz = p.getZ() - cz;
            squareDistances[p.getId()] = dx * dx + dy * dy + dz * dz;
        }
        Arrays.sort(squareDistances);
        return Math.sqrt(squareDistances[particleCount/2 - 1]);
    }

    private double[] forceCalculation(Particle p1, Particle p2) {
        final double dx = p1.getX() - p2.getX();
        final double dy = p1.getY() - p2.getY();
        final double dz = p1.getZ() - p2.getZ();

        final double r2 = dx*dx + dy*dy + dz*dz;
        final double denom = r2 + h*h;
        final double invDist = 1.0 / Math.sqrt(denom);
        final double invDist3 = invDist * invDist * invDist;
        final double scalar = -GM2 * invDist3;

        return new double[] { dx * scalar, dy * scalar, dz * scalar };
    }

    private double[] forceArray(Particle p) {
        double[] forces = new double[] {0,0,0};
        for(Particle p2 : particles) {
            if (!p.equals(p2)) {
                double[] forces2 = forceCalculation(p, p2);
                for (int i = 0; i < forces2.length && i < forces.length; i++) {
                    forces[i] += forces2[i];
                }
            }
        }
        return forces;
    }

    @Override
    public double[][] getForceMatrix(){
        double[][] forceMatrix = new double[particles.size()][3];
        particles.parallelStream().forEach(p -> forceMatrix[p.getId()] = forceArray(p));
        return forceMatrix;
    }

    @Override
    public boolean isForceFunctionSpeedDependant() {
        return false;
    }

    @Override
    public double[][] getR3Matrix() {
        return getEmptyMatrix();
    }

    @Override
    public double[][] getR4Matrix() {
        return getEmptyMatrix();    }

    @Override
    public double[][] getR5Matrix() {
        return getEmptyMatrix();    }

    private double[][] getEmptyMatrix() {
        double[][] toReturn = new double[particleCount()][Particle.DIMENSION];
        for (int i = 0; i < particleCount(); i++) {
            for (int j = 0; j < Particle.DIMENSION; j++) {
                toReturn[i][j] = 0;
            }
        }
        return toReturn;
    }
}
