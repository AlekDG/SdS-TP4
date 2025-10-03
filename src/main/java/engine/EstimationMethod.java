package engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        return new BeemanIterator();
    }

    public Iterator<Time> verletEstimation() {
        return new VerletIterator();
    }

    private class GearIterator implements Iterator<Time> {
        public GearIterator() {
            
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Time next() {
            return null;
        }
    }

    private class BeemanIterator implements Iterator<Time> {
        private double time;
        private final MovementModel modelCopy;
        private final double deltaT;
        private final double deltaTPow2;
        private final double mass;
        // TODO estos prevPosAndSpeed hacerlos dentro de las partículas, es re confuso acá
        private final List<List<Particle.PosSpeedPair>> prevPosAndSpeed;

        public BeemanIterator() {
            time = 0;
            modelCopy = model.hardCopyModel();
            deltaT = modelCopy.deltaT();
            deltaTPow2 = Math.pow(deltaT, 2);
            mass = modelCopy.mass();
            prevPosAndSpeed = new ArrayList<>();

            // This initial loop is to get a(t - DeltaT) using euler
            for (Particle p : modelCopy.particles()) {
                prevPosAndSpeed.add(new ArrayList<>());
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].getPos();
                    double speed = p.getPositionAndSpeedPair()[i].getSpeed();
                    double force = modelCopy.forceFunction().apply(pos, speed);
                    double prevPos = pos - deltaT * speed + force * deltaTPow2 / (2 * mass);
                    double prevSpeed = speed - deltaT * force / mass;
                    Particle.PosSpeedPair prevPosAndSpeedAux = new Particle.PosSpeedPair(prevPos, prevSpeed);
                    prevPosAndSpeed.get(p.getId()).add(i, prevPosAndSpeedAux);
                }
            }
        }

        @Override
        public boolean hasNext() {
            return time <= endTime;
        }

        @Override
        public Time next() {
            for (Particle p : modelCopy.particles()) {
                Particle.PosSpeedPair[] newPosAndSpeed = new Particle.PosSpeedPair[Particle.DIMENSION];
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].getPos();
                    double speed = p.getPositionAndSpeedPair()[i].getSpeed();
                    double force = modelCopy.forceFunction().apply(pos, speed);

                    Particle.PosSpeedPair prevPosAndSpeedAux = prevPosAndSpeed.get(p.getId()).get(i);
                    double prevForce = modelCopy.forceFunction().apply(prevPosAndSpeedAux.getPos(), prevPosAndSpeedAux.getSpeed());

                    double nextPos = pos + speed * deltaT + 2 * deltaTPow2 * (force / (3 * mass)) - deltaTPow2 * prevForce / (6 * mass);
                    double nextSpeedPred = speed + 3 * deltaT * force / (2 * mass) - deltaT * prevForce / (2 * mass);
                    double nextForce = modelCopy.forceFunction().apply(nextPos, nextSpeedPred);
                    prevPosAndSpeed.get(p.getId()).set(i, new Particle.PosSpeedPair(pos, speed));
                    double nextSpeed = speed + deltaT * nextForce / (3 * mass) + 5 * deltaT * force / (6 * mass) - deltaT * prevForce / (6 * mass);
                    newPosAndSpeed[i] = new Particle.PosSpeedPair(nextPos, nextSpeed);
                }
                p.setPositionAndSpeedPair(newPosAndSpeed);
            }
            time += deltaT;
            return new Time(time, modelCopy.particles());
        }
    }

    private class VerletIterator implements Iterator<Time> {
        private double time;
        private final MovementModel modelCopy;
        private final double deltaT;
        private final double deltaTPow2;
        private final double mass;
        private final List<List<Double>> prevPos;

        public VerletIterator() {
            time = 0;
            modelCopy = model.hardCopyModel();
            deltaT = modelCopy.deltaT();
            deltaTPow2 = Math.pow(deltaT, 2);
            mass = modelCopy.mass();
            prevPos = new ArrayList<>();

            // This initial loop is to get x(t - DeltaT) using euler
            for (Particle p : modelCopy.particles()) {
                prevPos.add(new ArrayList<>());
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].getPos();
                    double speed = p.getPositionAndSpeedPair()[i].getSpeed();
                    double force = modelCopy.forceFunction().apply(pos, speed);
                    double prevPosAux = pos - deltaT * speed + deltaTPow2 * force / (2 * mass); // euler
                    prevPos.get(p.getId()).add(i, prevPosAux);
                }
            }
        }

        @Override
        public boolean hasNext() {
            return time <= endTime;
        }

        @Override
        public Time next() {
            for (Particle p : modelCopy.particles()) {
                Particle.PosSpeedPair[] newPosAndSpeed = new Particle.PosSpeedPair[Particle.DIMENSION];
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].getPos();
                    double speed = p.getPositionAndSpeedPair()[i].getSpeed();
                    double force = modelCopy.forceFunction().apply(pos, speed);
                    double nextPos = 2 * pos - prevPos.get(p.getId()).get(i) + deltaTPow2 * force / mass;

                    // Assuming speed won't change a lot -> nextSpeedPred = speed
                    double nextForce = modelCopy.forceFunction().apply(nextPos, speed);
                    double nextNextPos = 2 * nextPos - pos + deltaTPow2 * nextForce / mass;
                    double nextSpeed = (nextNextPos - pos) / (2 * deltaT); // nextSpeedCorrected

                    prevPos.get(p.getId()).set(i, pos);
                    newPosAndSpeed[i] = new Particle.PosSpeedPair(nextPos, nextSpeed);
                }
                p.setPositionAndSpeedPair(newPosAndSpeed);
            }
            time += deltaT;
            return new Time(time, modelCopy.particles());
        }
    }

}