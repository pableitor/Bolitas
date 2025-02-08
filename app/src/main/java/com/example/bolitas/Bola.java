package com.example.bolitas;

import android.graphics.Matrix;
import android.graphics.RectF;

public class Bola {
    private double x, y, r, m;
    private final int color;
    private double dx, dy;
    private final Matrix matrix;
    private boolean isAbsorbed; // Flag to indicate if the ball is being absorbed

    public Bola(double x, double y, double r, int color, double dx, double dy) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.color = color;
        this.dx = dx;
        this.dy = dy;
        this.m = Math.pow(r, 3); // Mass proportional to radius cubed
        matrix = new Matrix(); // Initialize the matrix
        isAbsorbed = false; // Initially, the ball is not being absorbed
    }

    public RectF getBounds() {
        return new RectF((float) (x - r), (float) (y - r), (float) (x + r), (float) (y + r));
    }

    public int getColor() {
        return color;
    }

    public Matrix getMatrix() {
        matrix.reset();
        matrix.preScale((float) r, (float) r);
        matrix.preTranslate((float) (x / r) - 1, (float) (y / r) - 1);
        return matrix;
    }

    public void setAbsorbed(boolean absorbed) {
        isAbsorbed = absorbed;
    }

    public boolean isAbsorbed() {
        return isAbsorbed;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public double getX(){
        return this.x;
    }

    public double getY(){
        return this.y;
    }

    public void setVelocity(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }
    public double getdx(){
        return this.dx;
    }
    public double getdy(){
        return this.dy;
    }
    public double getRadius() {
        return r;
    }
    public void setMass(double mass){
        this.m = mass;
    }


    public void moveTowards(Bola other, float shrinkFactor) {
        if (!isAbsorbed) return; // Only move if being absorbed

        //Move the ball to the control ball
        x = other.x;
        y = other.y;
        // Shrink the ball
        r *= shrinkFactor; // Decrease the radius
        if (r <=0) isAbsorbed=false;
    }

    public void move(int width, int height) {
        if (isAbsorbed) return; // Don't move if being absorbed

        x += dx;
        y += dy;

        // Check for boundaries
        if (x - r < 0 || x + r > width) {
            dx = -dx;
            x = Math.max(r, Math.min(x, width - r)); // Prevent ball from getting stuck
        }
        if (y - r < 0 || y + r > height) {
            dy = -dy;
            y = Math.max(r, Math.min(y, height - r)); // Prevent ball from getting stuck
        }
    }

    public boolean collisionDetect(Bola other) {
        double distance = Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
        return distance < this.r + other.r;
    }

    public void resolveCollision(Bola other) {
        // 1. Calculate relative velocity and collision angle
        double vx21 = other.dx - this.dx;
        double vy21 = other.dy - this.dy;
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double alpha = Math.atan2(dy, dx);
        double cos = Math.cos(alpha);
        double sin = Math.sin(alpha);

        // 2. Rotate velocities to align with collision direction
        double vx21t = cos * vx21 + sin * vy21;

        // 3. Calculate new velocities along the collision direction
        double m21 = other.m / this.m;
        double dvx2 = -2 * vx21t / (1 + m21);
        double newOtherDx = other.dx + dvx2 * cos;
        double newThisDx = this.dx - m21 * dvx2 * cos;
        double newOtherDy = other.dy + dvx2 * sin;
        double newThisDy = this.dy - m21 * dvx2 * sin;

        // 4. Rotate velocities back to original frame of reference

        // 5. Apply new velocities
        other.dx = newOtherDx;
        other.dy = newOtherDy;
        this.dx = newThisDx;
        this.dy = newThisDy;

        // 6. Adjust positions to avoid overlap
        double distance = Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
        double overlap = (this.r + other.r - distance);
        if (overlap > 0) {
            double adjustmentX = overlap * (this.x - other.x) / distance;
            double adjustmentY = overlap * (this.y - other.y) / distance;
            this.x += adjustmentX;
            this.y += adjustmentY;
            other.x -= adjustmentX;
            other.y -= adjustmentY;
        }
    }

}