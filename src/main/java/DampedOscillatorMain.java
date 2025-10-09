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
    public static void main(String[] args) throws IOException {
        String estimator = System.getProperty(ESTIMATOR);
        double deltaT = Double.parseDouble(System.getProperty(DT));
        double A = 1;
        double gamma = 100;
        double mass = 70;
        double endTime = 5;
        MovementModel model = new DampedOscillator(Math.pow(10, 4), gamma, A, mass);
        EstimationMethod estimationMethod = new EstimationMethod(model, deltaT, endTime);
        Iterator<Time> timeIt;
        if (estimator.equals("verlet")) {
            timeIt = estimationMethod.verletEstimation();
            try (PostProcessor postProcessor = new PostProcessor("verlet.txt")) {
                timeIt.forEachRemaining(postProcessor::processTime);
            }
        }
        else if (estimator.equals("beeman")) {
            timeIt = estimationMethod.beemanEstimation();
            try (PostProcessor postProcessor = new PostProcessor("beeman.txt")) {
                timeIt.forEachRemaining(postProcessor::processTime);
            }
        }
        else {
            timeIt = estimationMethod.gearEstimation();
            try (PostProcessor postProcessor = new PostProcessor("gear.txt")) {
                timeIt.forEachRemaining(postProcessor::processTime);
            }
        }
    }
}