package org.hiperastar.examples.data;

public class Vec2D
{
    private final double x;
    private final double y;

    public Vec2D(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double length() { return Math.sqrt(x*x + y*y); }

    public Vec2D add(Vec2D other) { return new Vec2D(x+other.x, y+other.y); }
    public Vec2D subtract(Vec2D other) { return new Vec2D(x+other.x, y+other.y); }
}
