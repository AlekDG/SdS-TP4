package tools;

import engine.Particle;


import java.util.Random;
import java.util.function.Consumer;

public class ParticleGenerator {
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    public static void generate(
            int particleNumber,
            double radius,
            Consumer<Particle> consumer,
            double initialVelocityModulus
    ) {
        Random rand = new Random(System.currentTimeMillis());
        double x, y, z;
        for (int i = 0; i < particleNumber; i++) {
            x = rand.nextGaussian();
            y = rand.nextGaussian();
            z = rand.nextGaussian();
            double[] velocity = getInitialVelocityWithModulus(initialVelocityModulus);
            consumer.accept(new Particle(x, y, z, velocity[X], velocity[Y], velocity[Z], radius));
        }
    }

    public static double[] getInitialVelocityWithModulus(double modulus) {
        Random rand = new Random(System.currentTimeMillis());
        //Obtengo un vector aleatorio de componentes para v
        double vx = rand.nextDouble();
        double vy = rand.nextDouble();
        double vz = rand.nextDouble();
        //Normalizo el vector y ajusto el modulo
        double norm = Math.sqrt(vx * vx + vy * vy + vz * vz);
        vx = (vx / norm) * modulus;
        vy = (vy / norm) * modulus;
        vz = (vz / norm) * modulus;
        return new double[]{vx, vy, vz};
    }
}
