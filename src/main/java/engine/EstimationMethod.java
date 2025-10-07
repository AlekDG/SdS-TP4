package engine;

import java.util.Arrays;
import java.util.Iterator;

public class EstimationMethod {

    private final MovementModel model;
    private MovementModel currentModelCopy;
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

    public MovementModel getCurrentModelCopy() {
        return currentModelCopy;
    }

    private class VerletIterator implements Iterator<Time> {
        private double time;
        private final double deltaT;
        private final double deltaTPow2;
        private final double mass;
        private double[][] prevPos;

        public VerletIterator() {
            time = 0;
            currentModelCopy = model.hardCopyModel();
            deltaT = currentModelCopy.deltaT();
            deltaTPow2 = Math.pow(deltaT, 2);
            mass = currentModelCopy.mass();
            prevPos = new double[currentModelCopy.particleCount()][Particle.DIMENSION];
            double[][] forceMatrix = currentModelCopy.getForceMatrix();
            // This initial loop is to get x(t - DeltaT) using euler
            for (Particle p : currentModelCopy.particles()) {
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].pos();
                    double speed = p.getPositionAndSpeedPair()[i].speed();
                    double force = forceMatrix[p.getId()][i];
                    double prevPosAux = pos - deltaT * speed + deltaTPow2 * force / (2 * mass); // euler
                    prevPos[p.getId()][i] = prevPosAux;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return time <= endTime;
        }

        @Override
        public Time next() {
            double[][] forceMatrix = currentModelCopy.getForceMatrix();
            double[][] currentPos = new double[currentModelCopy.particleCount()][Particle.DIMENSION];
            for (Particle p : currentModelCopy.particles()) {
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].pos();
                    double force = forceMatrix[p.getId()][i];
                    currentPos[p.getId()][i] = pos;
                    p.updatePosition(2 * pos - prevPos[p.getId()][i] + deltaTPow2 * force / mass, i);
                }
            }
            double[][] nextForceMatrix = currentModelCopy.getForceMatrix();
            for(Particle p : currentModelCopy.particles()) {
                Particle.PosSpeedPair[] newPosAndSpeed = new Particle.PosSpeedPair[Particle.DIMENSION];
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double nextForce = nextForceMatrix[p.getId()][i];
                    double nextNextPos = 2 * p.getPositionAndSpeedPair()[i].pos() - currentPos[p.getId()][i] + deltaTPow2 * nextForce / mass;
                    double nextSpeed = (nextNextPos - currentPos[p.getId()][i]) / (2 * deltaT); // nextSpeedCorrected
                    newPosAndSpeed[i] = new Particle.PosSpeedPair(p.getPositionAndSpeedPair()[i].pos(), nextSpeed);
                }
                p.setPositionAndSpeedPair(newPosAndSpeed);
            }
            prevPos = Arrays.stream(currentPos)
                    .map(double[]::clone)
                    .toArray(double[][]::new);
            time += deltaT;
            currentModelCopy.particleSort();
            return new Time(time, currentModelCopy.particles());
        }
    }

    private class BeemanIterator implements Iterator<Time> {
        private double time;
        private final double deltaT;
        private final double deltaTPow2;
        private final double mass;
        private final Particle.PosSpeedPair[][] prevPosAndSpeed;
        private double[][] prevForceMatrix;

        public BeemanIterator() {
            time = 0;
            currentModelCopy = model.hardCopyModel();
            deltaT = currentModelCopy.deltaT();
            deltaTPow2 = Math.pow(deltaT, 2);
            mass = currentModelCopy.mass();
            prevPosAndSpeed = new Particle.PosSpeedPair[currentModelCopy.particleCount()][Particle.DIMENSION];
            prevForceMatrix = currentModelCopy.getForceMatrix();
            // This initial loop is to get a(t - DeltaT) using euler
            for (Particle p : currentModelCopy.particles()) {
                double[] forceArray = prevForceMatrix[p.getId()];
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].pos();
                    double speed = p.getPositionAndSpeedPair()[i].speed();
                    double force = forceArray[i];
                    double prevPos = pos - deltaT * speed + force * deltaTPow2 / (2 * mass);
                    double prevSpeed = speed - deltaT * force / mass;
                    Particle.PosSpeedPair prevPosAndSpeedAux = new Particle.PosSpeedPair(prevPos, prevSpeed);
                    prevPosAndSpeed[p.getId()][i] = prevPosAndSpeedAux;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return time <= endTime;
        }

        @Override
        public Time next() {
            double[][] forceMatrix = currentModelCopy.getForceMatrix();
            for (Particle p : currentModelCopy.particles()) {
                Particle.PosSpeedPair[] newPosAndSpeed = new Particle.PosSpeedPair[Particle.DIMENSION];
                double[] forceArray = forceMatrix[p.getId()];
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double pos = p.getPositionAndSpeedPair()[i].pos();
                    double speed = p.getPositionAndSpeedPair()[i].speed();
                    double force = forceArray[i];
                    double prevForce = prevForceMatrix[p.getId()][i];
                    double nextPos = pos + speed * deltaT + 2 * deltaTPow2 * (force / (3 * mass)) - deltaTPow2 * prevForce / (6 * mass);
                    double nextSpeedPred = speed + 3 * deltaT * force / (2 * mass) - deltaT * prevForce / (2 * mass);
                    newPosAndSpeed[i] = new Particle.PosSpeedPair(nextPos,nextSpeedPred);
                    prevPosAndSpeed[p.getId()][i] = new Particle.PosSpeedPair(pos, speed);
                }
                p.setPositionAndSpeedPair(newPosAndSpeed);
            }
            double[][] nextForceMatrix = currentModelCopy.getForceMatrix();
            for (Particle p : currentModelCopy.particles()) {
                Particle.PosSpeedPair[] newPosAndSpeed = new Particle.PosSpeedPair[Particle.DIMENSION];
                double[] nextForceArray = nextForceMatrix[p.getId()];
                double[] forceArray = forceMatrix[p.getId()];
                double[] prevForceArray = prevForceMatrix[p.getId()];
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    double speed = prevPosAndSpeed[p.getId()][i].speed();
                    double nextSpeed = speed + deltaT * nextForceArray[i] / (3 * mass) + 5 * deltaT * forceArray[i] / (6 * mass) - deltaT * prevForceArray[i] / (6 * mass);
                    newPosAndSpeed[i] = new Particle.PosSpeedPair(p.getPositionAndSpeedPair()[i].pos(), nextSpeed);
                }
                p.setPositionAndSpeedPair(newPosAndSpeed);
            }
            time += deltaT;
            currentModelCopy.particleSort();
            prevForceMatrix = forceMatrix;
            return new Time(time, currentModelCopy.particles());
        }
    }

    private class GearIterator implements Iterator<Time> {
        private double time;
        private final double deltaT;
        private final double deltaTPow2;
        // Previous values
        private final double[][][] prevGears;
        private final static int COEFFICIENT_AMOUNT = 6;
        int N;
        int DIM = Particle.DIMENSION;
        // Predictions
        double[][] posPred;
        double[][] velPred;
        double[][] r2Pred;
        double[][] r3Pred;
        double[][] r4Pred;
        double[][] r5Pred;

        public GearIterator() {
            time = 0;
            currentModelCopy = model.hardCopyModel();
            N = currentModelCopy.particles().size();
            posPred = new double[N][DIM];
            velPred = new double[N][DIM];
            r2Pred = new double[N][DIM];
            r3Pred = new double[N][DIM];
            r4Pred = new double[N][DIM];
            r5Pred = new double[N][DIM];

            deltaT = currentModelCopy.deltaT();
            deltaTPow2 = Math.pow(deltaT, 2);
            // for each particle and axis, I store it's previous coefficients
            prevGears = new double[currentModelCopy.particles().size()][Particle.DIMENSION][COEFFICIENT_AMOUNT];
            double[][] R2Matrix = currentModelCopy.getR2Matrix();
            double[][] R3Matrix = currentModelCopy.getR3Matrix();
            double[][] R4Matrix = currentModelCopy.getR4Matrix();
            double[][] R5Matrix = currentModelCopy.getR5Matrix();
            for (Particle p : currentModelCopy.particles()) {
                for (int i = 0; i < Particle.DIMENSION; i++) {
                    Particle.PosSpeedPair pair = p.getPositionAndSpeedPair()[i];
                    prevGears[p.getId()][i][0] = pair.pos();
                    prevGears[p.getId()][i][1] = pair.speed();
                    prevGears[p.getId()][i][2] = R2Matrix[p.getId()][i];
                    prevGears[p.getId()][i][3] = R3Matrix[p.getId()][i];
                    prevGears[p.getId()][i][4] = R4Matrix[p.getId()][i];
                    prevGears[p.getId()][i][5] = R5Matrix[p.getId()][i];
                }
            }

        }

        @Override
        public boolean hasNext() {
            return time <= endTime;
        }

        @Override
        public Time next() {
            for (Particle p : currentModelCopy.particles()) {
                int id = p.getId();
                for (int d = 0; d < DIM; d++) {
                    double pos = prevGears[id][d][0];
                    double speed = prevGears[id][d][1];
                    double r2 = prevGears[id][d][2];
                    double r3 = prevGears[id][d][3];
                    double r4 = prevGears[id][d][4];
                    double r5 = prevGears[id][d][5];

                    posPred[id][d] = taylorValueForList(new double[] {pos, speed, r2, r3, r4, r5});
                    velPred[id][d] = taylorValueForList(new double[] {speed, r2, r3, r4, r5});
                    r2Pred[id][d] = taylorValueForList(new double[] {r2, r3, r4, r5});
                    r3Pred[id][d] = taylorValueForList(new double[] {r3, r4, r5});
                    r4Pred[id][d] = taylorValueForList(new double[] {r4, r5});
                    r5Pred[id][d] = taylorValueForList(new double[] {r5});
                }
            }

            double[][] r2Computed = currentModelCopy.computeR2FromState(posPred, velPred);

            for (Particle p : currentModelCopy.particles()) {
                int id = p.getId();
                Particle.PosSpeedPair[] newPosAndSpeed = new Particle.PosSpeedPair[DIM];

                for (int d = 0; d < DIM; d++) {
                    double rPred = posPred[id][d];
                    double r1Pred = velPred[id][d];
                    double r2p = r2Pred[id][d];
                    double r3p = r3Pred[id][d];
                    double r4p = r4Pred[id][d];
                    double r5p = r5Pred[id][d];

                    double deltaA = r2Computed[id][d] - r2p;
                    double deltaR2 = deltaA * deltaTPow2 / 2;

                    double posCorr = rPred + correction(0, deltaR2);
                    double speedCorr = r1Pred + correction(1, deltaR2);

                    prevGears[id][d][0] = posCorr;
                    prevGears[id][d][1] = speedCorr;
                    prevGears[id][d][2] = r2p + correction(2, deltaR2);
                    prevGears[id][d][3] = r3p + correction(3, deltaR2);
                    prevGears[id][d][4] = r4p + correction(4, deltaR2);
                    prevGears[id][d][5] = r5p + correction(5, deltaR2);

                    newPosAndSpeed[d] = new Particle.PosSpeedPair(posCorr, speedCorr);
                }
                p.setPositionAndSpeedPair(newPosAndSpeed);
            }

            time += deltaT;
            currentModelCopy.particleSort();
            return new Time(time, currentModelCopy.particles());
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

        private double taylorValueForList(double[] derivatives) {
            double total = 0;
            for (int i = 0; i < derivatives.length; i++) {
                total += derivatives[i] * Math.pow(deltaT, i) / factorial(i);
            }
            return total;
        }

        private double correction(int q, double deltaR2) {
            // For Gear Order 5
            double a0;
            if (currentModelCopy.isForceFunctionSpeedDependant())
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