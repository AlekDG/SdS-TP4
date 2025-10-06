package engine;

import java.util.Comparator;
import java.util.List;

public class GravitationalSystem implements MovementModel {
    private final List<Particle> particles;
    private final double mass;
    private final double deltaT;
    private final int particleCount;
    private final double G;
    public double h;

    public GravitationalSystem(List<Particle> particles, double mass, double deltaT, double G, double h) {
        this.particles = particles;
        this.deltaT = deltaT;
        this.mass = mass;
        this.particleCount = particles.size();
        this.G = G;
        this.h = h;
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
    public double deltaT() {
        return deltaT;
    }

    @Override
    public int particleCount() {
        return particleCount;
    }

    @Override
    public MovementModel hardCopyModel() {
        List<Particle> newParticles = particles.stream().map(Particle::hardCopy).toList();
        return new GravitationalSystem(newParticles, mass, deltaT, G, h);
    }

    public double systemEnergy(){
        return kineticEnergy()+potentialEnergy();
    }

    private double kineticEnergy(){
        double totalEnergy=0;
        for(Particle p : particles){
            totalEnergy += Math.pow(p.getSpeedAbs(), 2) * (mass / 2);
        }
        return totalEnergy;
    }

    private double potentialEnergy(){
        double totalEnergy = 0;
        for(Particle p : particles){
            for(int i = particles.indexOf(p) + 1; i < particleCount; i++){
                Particle p2 = particles.get(i);
                totalEnergy += (-G * mass *mass) / p.getDistance(p2); // TODO falta el h
            }
        }
        return totalEnergy;
    }

    // Sorts particles based on how close they are to the center
    // We're gonna need this for half-mass radius
    public void particleSort(){
        // Taking square root is computationally expensive and doesn't change order because
        // if(a<=b) then(sqrt(a)<=sqrt(b))
        particles.sort(Comparator.comparingDouble(
                p -> p.getX() * p.getX() + p.getY() * p.getY() + p.getZ() * p.getZ()
        ));
    }

    public double halfMassRadius(){
        double hmr;
        Particle midParticle = particles.get(particleCount / 2);
        hmr = midParticle.getDistanceAbs();
        return hmr;
    }

    private double[] forceCalculation(Particle p1, Particle p2){
        double d12 = p1.getDistance(p2);
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        double dz = p1.getZ() - p2.getZ();
        double scalar = -G*mass*mass/Math.pow((Math.pow(d12,2)+Math.pow(h,2)),(double)3/2);
        double v1 = dx*scalar;
        double v2 = dy*scalar;
        double v3 = dz*scalar;
        return new double[]{v1, v2, v3};
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

            // TODO Se debería calcular con las posiciones y velocidades predichas
            double[] forceArray = forceArray(particle);

            R2[id][0] = forceArray[0] / mass;
            R2[id][1] = forceArray[1] / mass;
            R2[id][2] = forceArray[2] / mass;
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
