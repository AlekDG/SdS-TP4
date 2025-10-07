package tools;

import engine.Particle;
import engine.Time;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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
            List<Particle> parts = time.particles(); // if it's Iterable, collect to a List first
            int n = parts.size();
            writer.write(String.valueOf(n));
            writer.newLine();

            // Extended XYZ header line: include Lattice and Properties (and Time)
            // Use Locale.US to ensure dot decimal separator
            String header = String.format(Locale.US,
                    "Lattice=\"0 0 0 0 0 0 0 0 0\" Properties=species:S:1:pos:R:3:vel:R:3 Time=%.8f",
                    time.time());
            writer.write(header);
            writer.newLine();

            for (Particle p : parts) {
                writer.write(p.extXyzLine());
                writer.newLine();
            }
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

    public void writeRhmInitialLine(int repetitionCount) {
        try {
            writer.write("repetition %d\n".formatted(repetitionCount));
        } catch (IOException e) {
            throw new RuntimeException("Error writing on output file", e);
        }
    }

    public void processRhm(double time, double rhm)  {
        try {
            writer.write("%f , %f\n".formatted(time, rhm));
        } catch (IOException e) {
            throw new RuntimeException("Error writing on output file", e);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}