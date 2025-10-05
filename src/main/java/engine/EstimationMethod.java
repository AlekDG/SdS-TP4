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
            double[][] forceMatrix = modelCopy.getForceMatrix();
            // This initial loop is to get x(t - DeltaT) using euler
            for (Particle p : modelCopy.particles()) {
                double[] forceArray = forceMatrix[p.getId()];
                prevPos.add(new ArrayList<>());
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].getPos();
                    double speed = p.getPositionAndSpeedPair()[i].getSpeed();
                    double force = forceArray[i];
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
            double[][] forceMatrix = modelCopy.getForceMatrix();
            double[][] prevPositionArray = new double[modelCopy.particleCount()][Particle.DIMENSION];
            for(Particle p : modelCopy.particles()) {
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    prevPositionArray[p.getId()][i] = p.getPositionAndSpeedPair()[i].getPos();
                }
            }
            for (Particle p : modelCopy.particles()) {
                double[] forceArray = forceMatrix[p.getId()];
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].getPos();
                    double force = forceArray[i];
                    p.updatePosition(2 * pos - prevPos.get(p.getId()).get(i) + deltaTPow2 * force / mass, i);
                }
            }
            double[][] nextForceMatrix = modelCopy.getForceMatrix();
            for(Particle p : modelCopy.particles()) {
                Particle.PosSpeedPair[] newPosAndSpeed = new Particle.PosSpeedPair[Particle.DIMENSION];
                double[] forceArray = nextForceMatrix[p.getId()];
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double nextForce = forceArray[i];
                    double nextNextPos = 2 * p.getPositionAndSpeedPair()[i].getPos() - prevPositionArray[p.getId()][i] + deltaTPow2 * nextForce / mass;
                    double nextSpeed = (nextNextPos - prevPositionArray[p.getId()][i]) / (2 * deltaT); // nextSpeedCorrected
                    newPosAndSpeed[i] = new Particle.PosSpeedPair(p.getPositionAndSpeedPair()[i].getPos(), nextSpeed);
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
        private final List<List<Particle.PosSpeedPair>> prevPosAndSpeed;
        private double[][] prevForceMatrix;

        public BeemanIterator() {
            time = 0;
            modelCopy = model.hardCopyModel();
            deltaT = modelCopy.deltaT();
            deltaTPow2 = Math.pow(deltaT, 2);
            mass = modelCopy.mass();
            prevPosAndSpeed = new ArrayList<>();
            prevForceMatrix = modelCopy.getForceMatrix();
            // This initial loop is to get a(t - DeltaT) using euler
            for (Particle p : modelCopy.particles()) {
                prevPosAndSpeed.add(new ArrayList<>());
                double[] forceArray = prevForceMatrix[p.getId()];
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].getPos();
                    double speed = p.getPositionAndSpeedPair()[i].getSpeed();
                    double force = forceArray[i];
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
            double[][] forceMatrix = modelCopy.getForceMatrix();
            for (Particle p : modelCopy.particles()) {
                Particle.PosSpeedPair[] newPosAndSpeed = new Particle.PosSpeedPair[Particle.DIMENSION];
                double[] forceArray = forceMatrix[p.getId()];
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].getPos();
                    double speed = p.getPositionAndSpeedPair()[i].getSpeed();
                    double force = forceArray[i];
                    double prevForce = prevForceMatrix[p.getId()][i];
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
            prevForceMatrix = forceMatrix;
            return new Time(time, modelCopy.particles());
        }
    }

    private class GearIterator implements Iterator<Time> {
        private double time;
        private final MovementModel modelCopy;
        private final double deltaT;
        private final double deltaTPow2;
        private final double[][][] prevGears;
        private final static int COEFFICIENT_AMOUNT = 6;

        public GearIterator() {
            time = 0;
            modelCopy = model.hardCopyModel();
            deltaT = modelCopy.deltaT();
            deltaTPow2 = Math.pow(deltaT, 2);
            // for each particle and axis, I store it's previous coefficients
            prevGears = new double[model.particles().size()][Particle.DIMENSION][COEFFICIENT_AMOUNT];
            for (Particle p : modelCopy.particles()) {
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    Particle.PosSpeedPair pair = p.getPositionAndSpeedPair()[i];
                    double pos = pair.getPos();
                    double speed = pair.getSpeed();
                    double r2 = model.getR2().apply(pos, speed);
                    double r3 = model.getR3().apply(speed, r2);
                    double r4 = model.getR4().apply(r2, r3);
                    double r5 = model.getR5().apply(r3, r4);
                    prevGears[p.getId()][i][0] = pos;
                    prevGears[p.getId()][i][1] = speed;
                    prevGears[p.getId()][i][2] = r2;
                    prevGears[p.getId()][i][3] = r3;
                    prevGears[p.getId()][i][4] = r4;
                    prevGears[p.getId()][i][5] = r5;
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
                    double pos = prevGears[p.getId()][i][0];
                    double speed = prevGears[p.getId()][i][1];
                    double r2 = prevGears[p.getId()][i][2];
                    double r3 = prevGears[p.getId()][i][3];
                    double r4 = prevGears[p.getId()][i][4];
                    double r5 = prevGears[p.getId()][i][5];

                    double rPred =  taylorValueForList(new double[] {pos, speed, r2, r3, r4, r5});
                    double r1Pred = taylorValueForList(new double[] {speed, r2, r3, r4, r5});
                    double r2Pred = taylorValueForList(new double[] {r2, r3, r4, r5});
                    double r3Pred = taylorValueForList(new double[] {r3, r4, r5});
                    double r4Pred = taylorValueForList(new double[] {r4, r5});
                    double r5Pred = taylorValueForList(new double[] {r5});

                    double deltaA = modelCopy.getR2().apply(rPred, r1Pred) - r2Pred;
                    double deltaR2 = deltaA * deltaTPow2 / factorial(2); // factor de corrección

                    double posCorr = rPred + correction(0, deltaR2, deltaT);
                    double speedCorr = r1Pred + correction(1, deltaR2, deltaT);
                    prevGears[p.getId()][i][0] = posCorr;
                    prevGears[p.getId()][i][1] = speedCorr;
                    prevGears[p.getId()][i][2] = r2Pred + correction(2, deltaR2, deltaT);
                    prevGears[p.getId()][i][3] = r3Pred + correction(3, deltaR2, deltaT);
                    prevGears[p.getId()][i][4] = r4Pred + correction(4, deltaR2, deltaT);
                    prevGears[p.getId()][i][5] = r5Pred + correction(5, deltaR2, deltaT);

                    newPosAndSpeed[i] = new Particle.PosSpeedPair(posCorr, speedCorr);
                }
                p.setPositionAndSpeedPair(newPosAndSpeed);
            }
            time += deltaT;
            return new Time(time, modelCopy.particles());
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

        private double correction(int q, double deltaR2, double deltaT) {
            // For Gear Order 5
            double a0;
            if (modelCopy.isForceFunctionSpeedDependant())
                a0 = 3.0 / 16.0;
            else
                a0 = 3.0 / 20.0;
            double a1 = 251.0 / 360.0;
            double a2 = 1.0;
            double a3 = 11.0 / 18.0;
            double a4 = 1.0 / 6.0;
            double a5 = 1.0 / 60.0;

            double[] aCoefficients = new double[] {a0, a1, a2, a3, a4, a5};
            return aCoefficients[q] * deltaR2 * factorial(q) / Math.pow(deltaT, q);
        }
    }
}