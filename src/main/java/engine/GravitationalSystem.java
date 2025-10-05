package engine;

import java.util.Comparator;
import java.util.List;

//TODO: Make this implement MovementModel when that interface gets refactored/fixed
public class GravitationalSystem{
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

    public double mass() {
        return mass;
    }

    public List<Particle> particles() {
        return particles;
    }

    public double deltaT() {
        return deltaT;
    }

    public int particleCount() {
        return particleCount;
    }

    public MovementModel hardCopyModel() {
        //TODO: Make a deep copy of the entire particle list. Will also be needed for collision
        return null;
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
                totalEnergy += (-G * mass *mass) / p.getDistance(p2);
            }
        }
        return totalEnergy;
    }

    //Sorts particles based on how close they are to the center
    //We're gonna need this for half-mass radius
    public void particleSort(){
        //Taking square root is computationally expensive and doesn't change order because
        //if(a<=b) then(sqrt(a)<=sqrt(b))
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

    public double[][] getForceMatrix(){
        double[][]forceMatrix = new double[particles.size()][3];
        for(Particle p : particles){
            forceMatrix[p.getId()] = forceArray(p);
        }
        return forceMatrix;
    }



    public boolean isForceFunctionSpeedDependant() {
        return false;
    }
}
