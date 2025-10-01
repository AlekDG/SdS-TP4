package engine;

public class Particle {
    private static int globalId = 1;
    private final int id;
    private double x, y, z;
    private double sx, sy, sz;
    private final double r;

    public Particle(double x, double y, double z, double speedx,double speedy,double speedz, double radius) {
        this.id = globalId++;
        this.x = x;
        this.y = y;
        this.sx = speedx;
        this.sy = speedy;
        this.sz = speedz;
        this.r = radius;
    }

    public double getSpeedX() {
        return sx;
    }

    public double getSpeedY() {
        return sy;
    }

    public double getSpeedZ() {
        return sz;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getRadius() {
        return r;
    }

    @Override
    public String toString() {
        return "%d: x=%.2f y=%.2f z=%.2f spx=%.2f spy=%.2f spx=%.2f".formatted(getId(), x, y, z, sx, sy, sz);
    }

    public String csvString() {
        return "%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f".formatted(x, y, z, sx, sy, sz, r);
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Particle p && id == p.id;
    }
}
