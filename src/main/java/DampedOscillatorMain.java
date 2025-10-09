import engine.DampedOscillator;
import engine.EstimationMethod;
import engine.MovementModel;
import engine.Time;
import tools.PostProcessor;

import java.io.IOException;
import java.util.Iterator;
public class DampedOscillatorMain {
    private static final String DT = "DT";
    private static final String ESTIMATOR = "estimator";
    private static final String OUTPUT = "output";
    public static void main(String[] args) throws IOException {
        String estimator = System.getProperty(ESTIMATOR);
        double deltaT = Double.parseDouble(System.getProperty(DT));
        String output = System.getProperty(OUTPUT);
        double A = 1;
        double gamma = 100;
        double mass = 70;
        double endTime = 5;
        MovementModel model = new DampedOscillator(Math.pow(10, 4), gamma, A, mass);
        EstimationMethod estimationMethod = new EstimationMethod(model, deltaT, endTime);
        Iterator<Time> timeIt;
        if (estimator.equals("verlet")) {
            timeIt = estimationMethod.verletEstimation();
            try (PostProcessor postProcessor = new PostProcessor(output)) {
                timeIt.forEachRemaining(postProcessor::processTime);
            }
            System.out.printf("Finished Verlet with deltaT=%f\n", deltaT);
        }
        else if (estimator.equals("beeman")) {
            timeIt = estimationMethod.beemanEstimation();
            try (PostProcessor postProcessor = new PostProcessor(output)) {
                timeIt.forEachRemaining(postProcessor::processTime);
            }
            System.out.printf("Finished Beeman with deltaT=%f\n", deltaT);
        }
        else {
            timeIt = estimationMethod.gearEstimation();
            try (PostProcessor postProcessor = new PostProcessor(output)) {
                timeIt.forEachRemaining(postProcessor::processTime);
            }
            System.out.printf("Finished Gear with deltaT=%f\n", deltaT);
        }
    }
}