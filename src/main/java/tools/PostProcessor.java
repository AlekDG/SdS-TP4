package tools;

import engine.Particle;
import engine.Time;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class PostProcessor implements Closeable {
    private static final String OUTPUT_FILE_NAME = "output.txt";
    private final BufferedWriter writer;

    public PostProcessor(String outputName) {
        Locale.setDefault(Locale.US);
        try {
            if (outputName == null)
                outputName = OUTPUT_FILE_NAME;
            writer = new BufferedWriter(new FileWriter(outputName));
        } catch (IOException e) {
            throw new RuntimeException("Error opening file");
        }
    }

    public void processTime(Time time) {
        try {
            writer.write(String.valueOf(time.time()));
            writer.newLine();
            time.particles().forEach(this::processParticle);
        } catch (IOException e) {
            throw new RuntimeException("Error writing on output file");
        }
    }

    private void processParticle(Particle particle) {
        try {
            writer.write(particle.csvString());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing on output file");
        }
    }

    public void processTimeAnim(Time time) {
        try {
            // write number of particles (XYZ frame header)
            int n = time.particles().size();               // assumes particles() returns a List
            writer.write(String.valueOf(n));
            writer.newLine();
            // comment line: include time value so it's visible to OVITO as the frame comment
            writer.write("# t=" + String.format(Locale.US, "%.8f", time.time()));
            writer.newLine();

            // write one line per particle: element x y z vx vy vz
            time.particles().forEach(this::processParticleAnim);
        } catch (IOException e) {
            throw new RuntimeException("Error writing on output file", e);
        }
    }

    private void processParticleAnim(Particle particle) {
        try {
            writer.write(particle.xyzString()); // new method on Particle
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing on output file", e);
        }
    }

    public void processSystemEnergy(Time time, double energy) {
        try {
            writer.write(time.time() + "," + energy);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing on output file");
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}