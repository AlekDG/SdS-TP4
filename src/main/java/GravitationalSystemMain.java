import engine.*;
import tools.ParticleGenerator;
import tools.PostProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GravitationalSystemMain {
    private static final String N = "N";
    private static final String DT = "DT";
    private static final String MAX_T = "MaxT";
    private static final String SIMULATION = "SIM";
    private static final String OUTPUT_FILE = "OUTPUT_FILE";

    private static final double SMOOTHING_FACTOR = 10;
    private static final double INITIAL_VELOCITY_MODULUS = 0.1;
    private static final double RADIUS = 0;


    public static void main(String[] args) throws IOException {
        int n = Integer.parseInt(System.getProperty(N));
        double delta_t = Double.parseDouble(System.getProperty(DT));
        double max_t = Double.parseDouble(System.getProperty(MAX_T));
        int simulation = Integer.parseInt(System.getProperty(SIMULATION));
        String outputFile = System.getProperty(OUTPUT_FILE);

        switch (simulation) {
            case 1 -> optimalDeltaT(n, max_t);
            case 2 -> rhmSimulation(delta_t, max_t);
            case 3 -> galaxyCollision(n, delta_t, max_t);
            default -> test(n, delta_t, max_t);
        }

    }

    private static void rhmSimulation(double deltaT, double maxT) throws IOException {
        //Para cada valor de N, se realiza la simulacion 10 veces dejando las 10 iteraciones en el mismo archivo
        int[] particleCounts = {100, 500, 1000, 1500, 2000};
        AtomicInteger j = new AtomicInteger(0);
        for (int particleCount : particleCounts) {
            try (PostProcessor postProcessor = new PostProcessor("rhm" + particleCount + ".txt")) {
                for (int i = 0; i < 10; i++) {
                    postProcessor.writeRhmInitialLine(i);
                    Particle.resetGlobalId();
                    List<Particle> particles = new ArrayList<>();
                    ParticleGenerator.generate(particleCount, RADIUS, particles::add, INITIAL_VELOCITY_MODULUS);
                    GravitationalSystem system = new GravitationalSystem(particles, 1, 1, 0.1);
                    EstimationMethod estimationMethod = new EstimationMethod(system, deltaT, maxT);
                    Iterator<Time> timeIt = estimationMethod.gearEstimation();
                    GravitationalSystem systemIteratorCopy = (GravitationalSystem) estimationMethod.getCurrentModelCopy();
                    timeIt.forEachRemaining(time -> {
                        if (j.getAndIncrement() % (1 / (SMOOTHING_FACTOR * deltaT)) == 0) {
                            postProcessor.processRhm(time.time(), systemIteratorCopy.halfMassRadius());
                        }
                    });
                }
            }
        }
    }

    private static double errorEstimation(double eT, double e0) {
        return Math.abs(eT - e0) / Math.abs(e0);
    }

    private static void optimalDeltaT(int n, double max_t) throws IOException {
        double[] deltaTs = {1, 0.1, 0.01, 0.001, 0.0001};
        List<Particle> particles = new ArrayList<>();
        ParticleGenerator.generate(n, RADIUS, particles::add, INITIAL_VELOCITY_MODULUS);
        AtomicInteger i = new AtomicInteger(0);
        //Beeman
        for (double deltaT : deltaTs) {
            System.out.println("Starting Beeman simulation with " + n + " particles, delta_t = " + deltaT + ", max_t = " + max_t);
            GravitationalSystem system = new GravitationalSystem(particles, 1, 1, 0.1);
            EstimationMethod estimationMethod = new EstimationMethod(system, deltaT, max_t);
            Iterator<Time> timeIt = estimationMethod.beemanEstimation();
            GravitationalSystem systemIteratorCopy = (GravitationalSystem) estimationMethod.getCurrentModelCopy();
            double initialEnergy = system.systemEnergy();
            try (PostProcessor postProcessorEnergy = new PostProcessor("optimalDeltaTBeemanEnergy" + deltaT + ".txt")) {
                postProcessorEnergy.processSystemEnergy(new Time(0, particles), initialEnergy);
                timeIt.forEachRemaining(time -> {
                    if (i.getAndIncrement() % (1 / (SMOOTHING_FACTOR * deltaT)) == 0) {
                        double currentEnergy = systemIteratorCopy.systemEnergy();
                        double error = errorEstimation(currentEnergy, initialEnergy);
                        postProcessorEnergy.processSystemEnergy(time, error);
                    }
                });
            }
        }
        i.set(0);

        //Verlet
        for (double deltaT : deltaTs) {
            System.out.println("Starting Verlet simulation with " + n + " particles, delta_t = " + deltaT + ", max_t = " + max_t);
            GravitationalSystem system = new GravitationalSystem(particles, 1, 1, 0.1);
            EstimationMethod estimationMethod = new EstimationMethod(system, deltaT, max_t);
            Iterator<Time> timeIt = estimationMethod.verletEstimation();
            GravitationalSystem systemIteratorCopy = (GravitationalSystem) estimationMethod.getCurrentModelCopy();
            double initialEnergy = system.systemEnergy();
            try (PostProcessor postProcessorEnergy = new PostProcessor("optimalDeltaTVerletEnergy" + deltaT + ".txt")) {
                postProcessorEnergy.processSystemEnergy(new Time(0, particles), initialEnergy);
                timeIt.forEachRemaining(time -> {
                    if (i.getAndIncrement() % (1 / (SMOOTHING_FACTOR * deltaT)) == 0) {
                        double currentEnergy = systemIteratorCopy.systemEnergy();
                        double error = errorEstimation(currentEnergy, initialEnergy);
                        postProcessorEnergy.processSystemEnergy(time, error);
                    }
                });
            }
        }
        i.set(0);

        //Gear
        for (double deltaT : deltaTs) {
            System.out.println("Starting Gear simulation with " + n + " particles, delta_t = " + deltaT + ", max_t = " + max_t);
            GravitationalSystem system = new GravitationalSystem(particles, 1, 1, 0.1);
            EstimationMethod estimationMethod = new EstimationMethod(system, deltaT, max_t);
            Iterator<Time> timeIt = estimationMethod.gearEstimation();
            GravitationalSystem systemIteratorCopy = (GravitationalSystem) estimationMethod.getCurrentModelCopy();
            double initialEnergy = system.systemEnergy();
            try (PostProcessor postProcessorEnergy = new PostProcessor("optimalDeltaTGearEnergy" + deltaT + ".txt")) {
                postProcessorEnergy.processSystemEnergy(new Time(0, particles), initialEnergy);
                timeIt.forEachRemaining(time -> {
                    if (i.getAndIncrement() % (1 / (SMOOTHING_FACTOR * deltaT)) == 0) {
                        double currentEnergy = systemIteratorCopy.systemEnergy();
                        double error = errorEstimation(currentEnergy, initialEnergy);
                        postProcessorEnergy.processSystemEnergy(time, error);
                    }
                });
            }
        }


    }

    private static void galaxyCollision(int n, double delta_t, double max_t) throws IOException {
        List<Particle> galaxyParticles = ParticleGenerator.generateColisionGalaxys(n);
        GravitationalSystem system = new GravitationalSystem(galaxyParticles, 1, 1, 0.1);
        EstimationMethod estimationMethod = new EstimationMethod(system, delta_t, max_t);
        //TODO: elegir el mejor estimador para el sistema basandonos en el ej 2.1
        Iterator<Time> iterator = estimationMethod.verletEstimation();
        try (PostProcessor postProcessor = new PostProcessor("galaxyColissionAnimation.txt")) {
            postProcessor.processTimeAnim(new Time(0, galaxyParticles));
            AtomicInteger i = new AtomicInteger(0);
            iterator.forEachRemaining(time -> {
                if (i.getAndIncrement() % (1 / (SMOOTHING_FACTOR * delta_t)) == 0) {
                    postProcessor.processTimeAnim(time);
                }

            });
        }

    }

    private static void test(int n, double delta_t, double max_t) throws IOException {
        List<Particle> particles = new ArrayList<>();
        ParticleGenerator.generate(n, RADIUS, particles::add, INITIAL_VELOCITY_MODULUS);
        GravitationalSystem system = new GravitationalSystem(particles, 1, 1, 0.1);
        EstimationMethod estimationMethod = new EstimationMethod(system, delta_t, max_t);

//        System.out.println("Starting simulation with " + n + " particles, delta_t = " + delta_t + ", max_t = " + max_t + "and Verlet method.");
//        System.out.println("System energy before: " + system.systemEnergy());
        Iterator<Time> timeIt;
//        timeIt = estimationMethod.verletEstimation();
//        GravitationalSystem systemIteratorCopy1 = (GravitationalSystem) estimationMethod.getCurrentModelCopy();
        AtomicInteger i = new AtomicInteger(0);
//        try (PostProcessor postProcessor = new PostProcessor("verletGravitational.txt");
//             PostProcessor postProcessorEnergy = new PostProcessor("energyVerletGravitational.txt");
//             PostProcessor animProcessor = new PostProcessor("animVerletGravitational.txt")) {
//            postProcessorEnergy.processSystemEnergy(new Time(0, particles), systemIteratorCopy1.systemEnergy());
//            timeIt.forEachRemaining(time -> {
//                if (i.getAndIncrement() % (1 / (SMOOTHING_FACTOR * delta_t)) == 0) {
//                    postProcessor.processTime(time);
//                    animProcessor.processTimeAnim(time);
//                    postProcessorEnergy.processSystemEnergy(time, systemIteratorCopy1.systemEnergy());
//                }
//            });
//        }
//
//        System.out.println("Starting simulation with " + n + " particles, delta_t = " + delta_t + ", max_t = " + max_t + "and Beeman method.");
//        System.out.println("System energy before: " + system.systemEnergy());
//        timeIt = estimationMethod.beemanEstimation();
//        GravitationalSystem systemIteratorCopy2 = (GravitationalSystem) estimationMethod.getCurrentModelCopy();
//        i.set(0);
//        try (PostProcessor postProcessor = new PostProcessor("beemanGravitational.txt");
//             PostProcessor postProcessorEnergy = new PostProcessor("energyBeemanGravitational.txt");
//             PostProcessor animProcessor2 = new PostProcessor("animBeemanGravitational.txt")) {
//            postProcessorEnergy.processSystemEnergy(new Time(0, particles), systemIteratorCopy2.systemEnergy());
//            timeIt.forEachRemaining(time -> {
//                if (i.getAndIncrement() % (1 / (SMOOTHING_FACTOR * delta_t)) == 0) {
//                    postProcessor.processTime(time);
//                    postProcessorEnergy.processSystemEnergy(time, systemIteratorCopy2.systemEnergy());
//                    animProcessor2.processTimeAnim(time);
//                }
//            });
//        }

        System.out.println("Starting simulation with " + n + " particles, delta_t = " + delta_t + ", max_t = " + max_t + "and Gear method.");
        System.out.println("System energy before: " + system.systemEnergy());
        timeIt = estimationMethod.gearEstimation();
        GravitationalSystem systemIteratorCopy3 = (GravitationalSystem) estimationMethod.getCurrentModelCopy();
        i.set(0);
        try (PostProcessor postProcessor = new PostProcessor("gearGravitational.txt");
             PostProcessor postProcessorEnergy = new PostProcessor("energyGearGravitational.txt");
             PostProcessor animProcessor3 = new PostProcessor("animGearGravitational.txt")) {
            postProcessorEnergy.processSystemEnergy(new Time(0, particles), systemIteratorCopy3.systemEnergy());
            timeIt.forEachRemaining(time -> {
                if (i.getAndIncrement() % (1 / (SMOOTHING_FACTOR * delta_t)) == 0) {
                    postProcessor.processTime(time);
                    postProcessorEnergy.processSystemEnergy(time, systemIteratorCopy3.systemEnergy());
                    animProcessor3.processTimeAnim(time);
                }
            });
        }
    }

}
