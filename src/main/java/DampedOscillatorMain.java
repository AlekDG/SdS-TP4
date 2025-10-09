import engine.DampedOscillator;
import engine.EstimationMethod;
import engine.MovementModel;
import engine.Time;
import tools.PostProcessor;

import java.io.IOException;
import java.util.Iterator;
public class DampedOscillatorMain {
    public static void main(String[] args) throws IOException {
        double deltaT = Math.pow(10, -2);
        MovementModel model = new DampedOscillator(Math.pow(10, 4), 100, 1, 70);
        EstimationMethod estimationMethod = new EstimationMethod(model, deltaT, 5);
        Iterator<Time> timeIt;
        timeIt = estimationMethod.verletEstimation();
        try (PostProcessor postProcessor = new PostProcessor("verlet.txt")) {
            timeIt.forEachRemaining(postProcessor::processTime);
        }

        timeIt = estimationMethod.beemanEstimation();
        try (PostProcessor postProcessor = new PostProcessor("beeman.txt")) {
            timeIt.forEachRemaining(postProcessor::processTime);
        }

        timeIt = estimationMethod.gearEstimation();
        try (PostProcessor postProcessor = new PostProcessor("gear.txt")) {
            timeIt.forEachRemaining(postProcessor::processTime);
        }
    }
}