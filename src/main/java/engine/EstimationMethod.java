package engine;

import java.util.Iterator;
import java.util.List;

public class EstimationMethod {

    public void GearEstimation() {

    }

    public void BeemanEstimation() {

    }

    public void VerletEstimation(MovementModel model) {
        double deltaT = model.deltaT();
        double deltaTPow2 = Math.pow(deltaT, 2);
        double mass = model.mass();
        double[] prevPos = new double[Particle.DIMENSION];
        for (Particle p : model.particles()) {
            for (int i = 0; i < Particle.DIMENSION; i++) {
                double pos = p.getPositionAndSpeedPair()[i].getPos();
                double speed = p.getPositionAndSpeedPair()[i].getSpeed();
                double force = model.forceFunction().apply(pos, speed);
                prevPos[i] = pos - deltaT * speed + force * deltaTPow2 / (2 * mass); // euler
            }
        }

        for (Particle p : model.particles()) {
            for (int i = 0; i < Particle.DIMENSION; i++) {
                double pos = p.getPositionAndSpeedPair()[i].getPos();
                double speed = p.getPositionAndSpeedPair()[i].getSpeed();
                double force = model.forceFunction().apply(pos, speed);
                double nextPos = 2 * pos - prevPos[i] + force * deltaTPow2 / mass;

                // Asumo que la velocidad no cambiará mucho
                double nextForce = model.forceFunction().apply(nextPos, speed);
                double nextNextPos = 2 * nextPos - pos + nextForce * deltaTPow2 / mass;
                double nextSpeed = (nextNextPos - pos) / (2 * deltaT);

                prevPos[i] = pos;
                // update particle position and speed
            }
        }
    }
}
