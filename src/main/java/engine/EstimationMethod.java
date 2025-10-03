package engine;

import java.util.Iterator;

public class EstimationMethod {

    private final MovementModel model;
    private final double endTime;

    public EstimationMethod(MovementModel model, double endTime) {
        this.model = model;
        this.endTime = endTime;
    }

    public Iterator<Time> gearEstimation() {
        return null;
    }

    public Iterator<Time> beemanEstimation() {
        return null;
    }

    public Iterator<Time> verletEstimation() {
        return new VerletIterator();
    }

    private class VerletIterator implements Iterator<Time> {
        private double time;
        private final MovementModel modelCopy;
        private final double deltaT;
        private final double deltaTPow2;
        private final double mass;
        private final double[] prevPos;

        public VerletIterator() {
            time = 0;
            modelCopy = model.hardCopyModel();
            deltaT = modelCopy.deltaT();
            deltaTPow2 = Math.pow(deltaT, 2);
            mass = modelCopy.mass();
            prevPos = new double[Particle.DIMENSION];

            // This initial loop is to get x(t - DeltaT) using euler
            for (Particle p : modelCopy.particles()) {
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].getPos();
                    double speed = p.getPositionAndSpeedPair()[i].getSpeed();
                    double force = modelCopy.forceFunction().apply(pos, speed);
                    prevPos[i] = pos - deltaT * speed + force * deltaTPow2 / (2 * mass); // euler
                }
            }
        }

        @Override
        public boolean hasNext() {
            return time < endTime;
        }

        @Override
        public Time next() {
            for (Particle p : modelCopy.particles()) {
                Particle.PosSpeedPair[] newPosAndSpeed = new Particle.PosSpeedPair[Particle.DIMENSION];
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].getPos();
                    double speed = p.getPositionAndSpeedPair()[i].getSpeed();
                    double force = modelCopy.forceFunction().apply(pos, speed);
                    double nextPos = 2 * pos - prevPos[i] + force * deltaTPow2 / mass;

                    // Assuming speed won't change a lot -> nextSpeedPred = speed
                    double nextForce = modelCopy.forceFunction().apply(nextPos, speed);
                    double nextNextPos = 2 * nextPos - pos + nextForce * deltaTPow2 / mass;
                    double nextSpeed = (nextNextPos - pos) / (2 * deltaT); // nextSpeedCorrected

                    prevPos[i] = pos;
                    newPosAndSpeed[i] = new Particle.PosSpeedPair(nextPos, nextSpeed);
                }
                p.setPositionAndSpeedPair(newPosAndSpeed);
            }
            time += deltaT;
            return new Time(time, modelCopy.particles());
        }
    }

}