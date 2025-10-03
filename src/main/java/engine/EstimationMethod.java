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

    public Iterator<Time> verletEstimation() {
        return new VerletIterator();
    }

    public Iterator<Time> beemanEstimation() {
        return new BeemanIterator();
    }

    public Iterator<Time> gearEstimation() {
        return new GearIterator();
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
                    prevPos.get(p.getId()).add(prevPosAux);
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
                    prevPosAndSpeed.get(p.getId()).add(prevPosAndSpeedAux);
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

    private class GearIterator implements Iterator<Time> {
        private double time;
        private final MovementModel modelCopy;
        private final double deltaT;
        private final double deltaTPow2;

        public GearIterator() {
            time = 0;
            modelCopy = model.hardCopyModel();
            deltaT = modelCopy.deltaT();
            deltaTPow2 = Math.pow(deltaT, 2);
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
                    double r2 = model.getR2().apply(pos, speed);
                    double r3 = model.getR3().apply(speed, r2);
                    double r4 = model.getR4().apply(r2, r3);
                    double r5 = model.getR5().apply(r3, r4);

                    double rPred =  taylorValueForList(new double[] {pos, speed, r2, r3, r4, r5});
                    double r1Pred = taylorValueForList(new double[] {speed, r2, r3, r4, r5});
                    double r2Pred = taylorValueForList(new double[] {r2, r3, r4, r5});

                    double deltaA = model.getR2().apply(rPred, r1Pred) - r2Pred;
                    double deltaR2 = deltaA * deltaTPow2 / factorial(2); // factor de corrección

                    // For Gear Order 5
                    double c0;
                    if (model.isForceFunctionSpeedDependant())
                        c0 = 3.0 / 16.0;
                    else
                        c0 = 3.0 / 20.0;
                    double c1 = 251.0 / 360.0;

                    double rCorr  = rPred  + c0 * deltaR2;
                    double r1Corr = r1Pred + c1 * deltaR2 / deltaT;

                    newPosAndSpeed[i] = new Particle.PosSpeedPair(rCorr, r1Corr);
                }
                p.setPositionAndSpeedPair(newPosAndSpeed);
            }
            time += deltaT;
            return new Time(time, modelCopy.particles());
        }
    }

    private long factorial(int n) {
        if (n <= 1)
            return 1;
        long toReturn = 1;
        for (int i = 2; i <= n; i++) {
            toReturn *= i;
        }
        return toReturn;
    }

    private double taylorValueForList(double[] derivates) {
        double deltaT = model.deltaT();
        double total = 0;
        for (int i = 0; i < derivates.length; i++) {
            total += derivates[i] * Math.pow(deltaT, i) / factorial(i);
        }
        return total;
    }

}