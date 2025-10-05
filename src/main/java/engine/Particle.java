package engine;

import java.util.Locale;

public class Particle {
    public static final int DIMENSION = 3;
    private static int globalId = 0;
    private final int id;
    private double x, y, z;
    private double sx, sy, sz;
    private final double r;
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    public Particle(double x, double y, double z, double speedx, double speedy, double speedz, double radius) {
        this(globalId++, x, y, z, speedx, speedy, speedz, radius);
    }

    private Particle(int id, double x, double y, double z, double speedx, double speedy, double speedz, double radius) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
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

    public double getSpeedAbs(){
        return Math.sqrt(sx*sx + sy*sy + sz*sz);
    }

    public PosSpeedPair[] getPositionAndSpeedPair() { return new PosSpeedPair[] {
            new PosSpeedPair(x, sx),
            new PosSpeedPair(y, sy),
            new PosSpeedPair(z, sz)
        };
    }

    public void setPositionAndSpeedPair(PosSpeedPair[] posAndSpeedPair) {
        x = posAndSpeedPair[0].getPos();
        sx = posAndSpeedPair[0].getSpeed();
        y = posAndSpeedPair[1].getPos();
        sy = posAndSpeedPair[1].getSpeed();
        z = posAndSpeedPair[2].getPos();
        sz = posAndSpeedPair[2].getSpeed();
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

    public double getDistanceAbs(){
        return Math.sqrt(sx*sx + sy*sy + sz*sz);
    }

    public double getDistance(Particle p) {
        return Math.sqrt(Math.pow(x-p.x,2) + Math.pow(y-p.y,2) + Math.pow(z-p.z,2));
    }

    public double getRadius() {
        return r;
    }

    @Override
    public String toString() {
        return "%d: x=%.2f y=%.2f z=%.2f spx=%.2f spy=%.2f spx=%.2f".formatted(getId(), x, y, z, sx, sy, sz);
    }

    public String csvString() {
        return "%.8f,%.8f,%.8f,%.8f,%.8f,%.8f".formatted(x, y, z, sx, sy, sz);
    }
    public String extXyzLine() {
        // species then pos (x y z) then vel (sx sy sz)
        return String.format(Locale.US, "P %.8f %.8f %.8f %.8f %.8f %.8f",
                x, y, z, sx, sy, sz);
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

    public void updatePosition(double value, int axis) {
        switch (axis) {
            case X:
                x = value;
                break;
            case Y:
                y = value;
                break;
            case Z:
                z = value;
                break;
            default:
                break;
        }
    }

    public static class PosSpeedPair {
        private final double pos;
        private final double speed;
        public PosSpeedPair(double pos, double speed) {
            this.pos = pos;
            this.speed = speed;
        }

        public double getPos() {
            return pos;
        }

        public double getSpeed() {
            return speed;
        }
    }

    public Particle hardCopy() {
        return new Particle(id, x, y, z, sx, sy, sz, r);
    }
}
