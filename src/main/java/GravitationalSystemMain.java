import engine.*;
import tools.ParticleGenerator;
import tools.PostProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GravitationalSystemMain {
    private static final String N = "n";
    private static final String DT = "dt";
    private static final String OUTPUT_FILE = "OUTPUT_FILE";
    private static final double SMOOTHING_FACTOR = 5;

    private static final double INITIAL_VELOCITY_MODULUS = 0.1;
    private static final double RADIUS = 0;
    private static final String MAX_T = "max_t";


    public static void main(String[] args) throws IOException {
        int n = 200;//Integer.parseInt(System.getProperty(N));
        double delta_t = 0.001;//Double.parseDouble(System.getProperty(DT));
        double max_t = 100; // Double.parseDouble(System.getProperty(MAX_T));
        String outputFile = System.getProperty(OUTPUT_FILE);
        int simulation = 2; //0 for deltaT optimization, 1 for rhm ....

        switch (simulation) {
            case 0 -> optimalDeltaT(n, max_t);
            case 1 -> rhmSimulation(n, delta_t, max_t);
            default -> test(n, delta_t, max_t);
        }

    }

    private static void rhmSimulation(int n, double deltaT, double maxT) {
        throw new RuntimeException("Not implemented yet");
    }

    private static double errorEstimation(double eT, double e0) {
        return Math.abs(eT - e0) / Math.abs(e0);
    }

    private static void optimalDeltaT(int n, double max_t) throws IOException {
        double[] deltaTs = {10, 5, 1, 0.1, 0.05, 0.01};
        List<Particle> particles = new ArrayList<>();
        ParticleGenerator.generate(n, RADIUS, particles::add, INITIAL_VELOCITY_MODULUS);
        //Beeman
        for (double deltaT : deltaTs) {
            System.out.println("Starting Beeman simulation with " + n + " particles, delta_t = " + deltaT + ", max_t = " + max_t);
            MovementModel system = new GravitationalSystem(particles, 1, deltaT, 1, 0.1);
            EstimationMethod estimationMethod = new EstimationMethod(system, max_t);
            Iterator<Time> timeIt = estimationMethod.beemanEstimation();
            MovementModel systemIteratorCopy = estimationMethod.getCurrentModelCopy();
            double initialEnergy = ((GravitationalSystem) system).systemEnergy();
            try (PostProcessor postProcessorEnergy = new PostProcessor("optimalDeltaTBeemanEnergy" + deltaT + ".txt")) {
                timeIt.forEachRemaining(time -> {
                    double currentEnergy = ((GravitationalSystem) systemIteratorCopy).systemEnergy();
                    double error = errorEstimation(currentEnergy, initialEnergy);
                    postProcessorEnergy.processSystemEnergy(time, error);
                });
            }
        }

        //Verlet
        for (double deltaT : deltaTs) {
            System.out.println("Starting Verlet simulation with " + n + " particles, delta_t = " + deltaT + ", max_t = " + max_t);
            MovementModel system = new GravitationalSystem(particles, 1, deltaT, 1, 0.1);
            EstimationMethod estimationMethod = new EstimationMethod(system, max_t);
            Iterator<Time> timeIt = estimationMethod.verletEstimation();
            MovementModel systemIteratorCopy = estimationMethod.getCurrentModelCopy();
            double initialEnergy = ((GravitationalSystem) system).systemEnergy();
            try (PostProcessor postProcessorEnergy = new PostProcessor("optimalDeltaTVerletEnergy" + deltaT + ".txt")) {
                timeIt.forEachRemaining(time -> {
                    double currentEnergy = ((GravitationalSystem) systemIteratorCopy).systemEnergy();
                    double error = errorEstimation(currentEnergy, initialEnergy);
                    postProcessorEnergy.processSystemEnergy(time, error);
                });
            }
        }

        //Gear
        for (double deltaT : deltaTs) {
            System.out.println("Starting Gear simulation with " + n + " particles, delta_t = " + deltaT + ", max_t = " + max_t);
            MovementModel system = new GravitationalSystem(particles, 1, deltaT, 1, 0.1);
            EstimationMethod estimationMethod = new EstimationMethod(system, max_t);
            Iterator<Time> timeIt = estimationMethod.gearEstimation();
            MovementModel systemIteratorCopy = estimationMethod.getCurrentModelCopy();
            double initialEnergy = ((GravitationalSystem) system).systemEnergy();
            try (PostProcessor postProcessorEnergy = new PostProcessor("optimalDeltaTGearEnergy" + deltaT + ".txt")) {
                timeIt.forEachRemaining(time -> {
                    double currentEnergy = ((GravitationalSystem) systemIteratorCopy).systemEnergy();
                    double error = errorEstimation(currentEnergy, initialEnergy);
                    postProcessorEnergy.processSystemEnergy(time, error);
                });
            }
        }


    }

    private static void test(int n, double delta_t, double max_t) throws IOException {
        List<Particle> particles = new ArrayList<>();
        ParticleGenerator.generate(n, RADIUS, particles::add, INITIAL_VELOCITY_MODULUS);
        MovementModel system = new GravitationalSystem(particles, 1, delta_t, 1, 0.1);
        EstimationMethod estimationMethod = new EstimationMethod(system, max_t);

        System.out.println("Starting simulation with " + n + " particles, delta_t = " + delta_t + ", max_t = " + max_t + "and Verlet method.");
        System.out.println("System energy before: " + ((GravitationalSystem) system).systemEnergy());
        Iterator<Time> timeIt;
        timeIt = estimationMethod.verletEstimation();
        MovementModel systemIteratorCopy1 = estimationMethod.getCurrentModelCopy();
        AtomicInteger i = new AtomicInteger(0);
        try (PostProcessor postProcessor = new PostProcessor("verletGravitational.txt") ;
             PostProcessor postProcessorEnergy = new PostProcessor("energyVerletGravitational.txt");
             PostProcessor animProcessor = new PostProcessor("animVerletGravitational.txt")) {
            timeIt.forEachRemaining( time -> {
                if(i.getAndIncrement()%(1/(SMOOTHING_FACTOR*delta_t))==0){
                    postProcessor.processTime(time);
                    animProcessor.processTimeAnim(time);
                    postProcessorEnergy.processSystemEnergy(time, ((GravitationalSystem) systemIteratorCopy1).systemEnergy());
                }
            });
        }

        System.out.println("Starting simulation with " + n + " particles, delta_t = " + delta_t + ", max_t = " + max_t + "and Beeman method.");
        System.out.println("System energy before: " + ((GravitationalSystem) system).systemEnergy());
        timeIt = estimationMethod.beemanEstimation();
        MovementModel systemIteratorCopy2 = estimationMethod.getCurrentModelCopy();
        i.set(0);
        try (PostProcessor postProcessor = new PostProcessor("beemanGravitational.txt") ;
             PostProcessor postProcessorEnergy = new PostProcessor("energyBeemanGravitational.txt");
             PostProcessor animProcessor2 = new PostProcessor("animBeemanGravitational.txt")) {
            timeIt.forEachRemaining( time -> {
                if(i.getAndIncrement()%(1/(SMOOTHING_FACTOR*delta_t))==0){
                    postProcessor.processTime(time);
                    postProcessorEnergy.processSystemEnergy(time, ((GravitationalSystem) systemIteratorCopy2).systemEnergy());
                    animProcessor2.processTimeAnim(time);
                }
            });
        }

        System.out.println("Starting simulation with " + n + " particles, delta_t = " + delta_t + ", max_t = " + max_t + "and Gear method.");
        System.out.println("System energy before: " + ((GravitationalSystem) system).systemEnergy());
        timeIt = estimationMethod.beemanEstimation();
        MovementModel systemIteratorCopy3 = estimationMethod.getCurrentModelCopy();
        i.set(0);
        try (PostProcessor postProcessor = new PostProcessor("gearGravitational.txt") ;
             PostProcessor postProcessorEnergy = new PostProcessor("energyGearGravitational.txt");
             PostProcessor animProcessor3 = new PostProcessor("animGearGravitational.txt")) {
            timeIt.forEachRemaining( time -> {
                if(i.getAndIncrement()%(1/(SMOOTHING_FACTOR*delta_t))==0){
                    postProcessor.processTime(time);
                    postProcessorEnergy.processSystemEnergy(time, ((GravitationalSystem) systemIteratorCopy3).systemEnergy());
                    animProcessor3.processTimeAnim(time);
                }
            });
        }
    }

}
