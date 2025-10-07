package tools;

import engine.Particle;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class ParticleGenerator {
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    private static final int VELOCITY_COMPONENTS = 3;
    private static final double DELTAX = 4;
    private static final double DELTAY = 0.5;

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
            double[] velocity = getInitialVelocityWithModulus(initialVelocityModulus, rand);
            consumer.accept(new Particle(x, y, z, velocity[X], velocity[Y], velocity[Z], radius));
        }
    }

    public static List<Particle> generateColisionGalaxys(
            int particleNumberPerGalaxy
    ) {
        List<Particle> firstGalaxyParticles = new ArrayList<>();
        generate(particleNumberPerGalaxy, 0, firstGalaxyParticles::add, 0.1);
        List<Particle> particles = new ArrayList<>();
        for (Particle p : firstGalaxyParticles) {
            particles.add(p);
            Particle opposite = new Particle(p.getX() + DELTAX, p.getY() + DELTAY, p.getZ(),
                    p.getSpeedX() + 0.1, p.getSpeedY(), p.getSpeedZ(), 0);
            particles.add(opposite);
            p.setSpeedX(p.getSpeedX() - 0.1);
        }
        return particles;
    }

    public static double[] getInitialVelocityWithModulus(double modulus, Random rand) {
        double[] v = new double[VELOCITY_COMPONENTS];
        //Obtengo un vector aleatorio de componentes para v
        for (int i = 0; i < VELOCITY_COMPONENTS; i++) {
            v[i] = 2 * rand.nextDouble() - 1;
        }
        //Normalizo el vector y ajusto el modulo
        double norm = Math.sqrt(v[X] * v[X] + v[Y] * v[Y] + v[Z] * v[Z]);
        v[X] = (v[X] / norm) * modulus;
        v[Y] = (v[Y] / norm) * modulus;
        v[Z] = (v[Z] / norm) * modulus;
        return v;
    }
}
