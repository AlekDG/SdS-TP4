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

    public GravitationalSystem(List<Particle> particles, double mass, double deltaT, double G) {
        this.particles = particles;
        this.deltaT=deltaT;
        this.mass=mass;
        this.particleCount=particles.size();
        this.G=G;
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
            totalEnergy+=Math.pow( p.getSpeedAbs(),2)*(double)(mass/2);
        }
        return totalEnergy;
    }

    private double potentialEnergy(){
        double totalEnergy=0;
        for(Particle p : particles){
            for(int i=particles.indexOf(p)+1; i<particleCount; i++){
                Particle p2 = particles.get(i);
                totalEnergy+=(-G*mass*mass)/p.getDistance(p2);
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
                p -> p.getX()*p.getX() + p.getY()*p.getY() + p.getZ()*p.getZ()
        ));
    }

    public double halfMassRadius(){
        double hmr;
        Particle midParticle = particles.get(particleCount/2);
        hmr = midParticle.getDistanceAbs();
        return hmr;
    }



    public boolean isForceFunctionSpeedDependant() {
        return false;
    }
}
