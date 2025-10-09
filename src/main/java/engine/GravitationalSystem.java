package engine;

import java.util.ArrayList;
import java.util.Comparator;
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
        double halfEnergy = 0;
        for(Particle p : particles){
            for(int i = particles.indexOf(p) + 1; i < particleCount; i++){
                Particle p2 = particles.get(i);
                double dx = p.getX() - p2.getX();
                double dy = p.getY() - p2.getY();
                double dz = p.getZ() - p2.getZ();
                double r2 = dx*dx + dy*dy + dz*dz;
                halfEnergy += (-GM2) / Math.sqrt(r2 + h*h);
            }
        }
        return halfEnergy*2;
    }

    private void particleSort(){
        particles.sort(Comparator.comparingDouble(
                p -> p.getX() * p.getX() + p.getY() * p.getY() + p.getZ() * p.getZ()
        ));
    }

    public double halfMassRadius(){
        particleSort();
        double hmr;
        Particle midParticle = particles.get(particleCount / 2);
        hmr = midParticle.getDistanceAbs();
        return hmr;
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
        for(Particle p : particles){
            forceMatrix[p.getId()] = forceArray(p);
        }
        return forceMatrix;
    }

    @Override
    public boolean isForceFunctionSpeedDependant() {
        return false;
    }

    @Override
    public double[][] computeR2FromState(double[][] positions, double[][] velocities) {
        double[][] R2 = new double[particleCount()][Particle.DIMENSION];
        for (Particle p : particles) {
            int id = p.getId();
            double x = positions[id][0];
            double y = positions[id][1];
            double z = positions[id][2];
            double vx = velocities[id][0];
            double vy = velocities[id][1];
            double vz = velocities[id][2];

            Particle particle = new Particle(id, x, y, z, vx, vy, vz, 0);

            double[] forces = new double[] {0, 0, 0};

            for (Particle otherPar : particles) {
                int otherId = otherPar.getId();
                if (id == otherId)
                    continue;
                double otherX = positions[otherId][0];
                double otherY = positions[otherId][1];
                double otherZ = positions[otherId][2];
                double otherVX = velocities[otherId][0];
                double otherVY = velocities[otherId][1];
                double otherVZ = velocities[otherId][2];

                Particle otherParticle = new Particle(otherId, otherX, otherY, otherZ, otherVX, otherVY, otherVZ, 0);
                double[] currentParForce = forceCalculation(particle, otherParticle);
                forces[0] += currentParForce[0];
                forces[1] += currentParForce[1];
                forces[2] += currentParForce[2];
            }

            R2[id][0] = forces[0] / mass;
            R2[id][1] = forces[1] / mass;
            R2[id][2] = forces[2] / mass;
        }
        return R2;
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
