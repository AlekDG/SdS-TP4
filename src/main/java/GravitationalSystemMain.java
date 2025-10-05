import engine.*;
import tools.ParticleGenerator;
import tools.PostProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GravitationalSystemMain {
    private static final String N = "n";
    private static final String DT = "dt";
    private static final String OUTPUT_FILE = "OUTPUT_FILE";
    private static final double INITIAL_VELOCITY_MODULUS = 0.1;
    private static final double RADIUS = 0;
    private static final String MAX_T = "max_t";


    public static void main(String[] args) throws IOException {
        int n = 1000;//Integer.parseInt(System.getProperty(N));
        double delta_t = 0.1 ;//Double.parseDouble(System.getProperty(DT));
        double max_t = 100; // Double.parseDouble(System.getProperty(MAX_T));
        String outputFile = System.getProperty(OUTPUT_FILE);

        List<Particle> particles = new ArrayList<>();
        ParticleGenerator.generate(n, RADIUS, particles::add, INITIAL_VELOCITY_MODULUS);
        MovementModel system = new GravitationalSystem(particles, 1, delta_t, 1, 0.1);
        EstimationMethod estimationMethod = new EstimationMethod(system, max_t);
        System.out.println("Starting simulation with " + n + " particles, delta_t = " + delta_t + ", max_t = " + max_t + "and Verlet method.");
        System.out.println("System energy before: " + ((GravitationalSystem) system).systemEnergy());
        Iterator<Time> timeIt;
        timeIt = estimationMethod.verletEstimation();
        try (PostProcessor postProcessor = new PostProcessor("verletGravitational.txt") ;
                PostProcessor postProcessorEnergy = new PostProcessor("energyVerletGravitational.txt")) {
            timeIt.forEachRemaining( time -> {
                postProcessor.processTime(time);
                postProcessorEnergy.processSystemEnergy(time, ((GravitationalSystem) system).systemEnergy());
            });
        }

    }
}
