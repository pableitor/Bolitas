package com.example.bolitas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BolitasView extends View {

    private List<Bola> balls; // Renamed to be more idiomatic
    private final Paint paint = new Paint(); // Initialize here and make it final
    private Bola controlBall;
    private Paint backgroundPaint; // Paint for the background gradient

    // Fixed gradient for all balls (white to transparent)
    private RadialGradient fixedBallGradient;

    public BolitasView(Context context) {
        super(context);
        init();
    }

    public BolitasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BolitasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
        balls = new ArrayList<>();

        // Initialize the background paint
        backgroundPaint = new Paint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Set up the background gradient
        RadialGradient radialGradient = new RadialGradient(
                w / 10f, h / 10f, Math.max(w, h) / 1.5f,
                new int[]{Color.WHITE, Color.DKGRAY},
                null, Shader.TileMode.CLAMP);
        backgroundPaint.setShader(radialGradient);

        // Set up the fixed ball gradient
        fixedBallGradient = new RadialGradient(
                0.5f, 0.5f, 2.0f, // Center and radius, we'll adjust the center when drawing
                new int[]{Color.WHITE,  Color.DKGRAY}, // Adding an intermediate gray
                null, // Adjusting positions for softer transition
                Shader.TileMode.CLAMP);

        if (balls.isEmpty()) {
            controlBall = new Bola(w / 2.0, h / 2.0, 40, Color.BLACK, 0, 0);
            controlBall.m = 1e20; //very high mass to prevent control ball to move
            balls.add(controlBall);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); // Call super.onDraw() first

        // 1. Clear background with gradient
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // 2. Draw all balls
        drawBalls(canvas);

        // 3. Move all balls.
        moveBalls();

        // 4. Check for and handle collisions.
        handleCollisions();

        // 5. Schedule next redraw.
        invalidate();
    }

    private void drawBalls(Canvas canvas) {
        for (Bola ball : balls) {

            // Set the fixed gradient
            fixedBallGradient.setLocalMatrix(ball.getMatrix()); //Adjust the center
            paint.setShader(fixedBallGradient);

            // Create a color filter to blend with the ball's color
            ColorFilter filter = new LightingColorFilter(ball.getColor(), 0); // Multiply by the ball's color

            // Apply the filter
            paint.setColorFilter(filter);

            // Draw the ball
            RectF oval = ball.getBounds();
            canvas.drawOval(oval, paint);

            // Reset the shader to draw the next ball
            paint.setShader(null);

            // Reset the filter
            paint.setColorFilter(null);
        }
    }

    private void moveBalls() {
        for (Bola ball : balls) {
            ball.move(getWidth(), getHeight()); // use a better name for the method
        }
    }

    private void handleCollisions() {
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Bola ball1 = balls.get(i);
                Bola ball2 = balls.get(j);
                if (ball1.collisionDetect(ball2)) { // Modified to pass individual ball
                    // Handle the collision here.
                    ball1.resolveCollision(ball2);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            controlBall.dx = (event.getX() - controlBall.x) / 20;
            controlBall.dy = (event.getY() - controlBall.y) / 20;
            controlBall.x = event.getX();
            controlBall.y = event.getY();
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void addBall() {
        Random random = new Random();
        double r = 40 * (0.5 + random.nextDouble() * 1.5);
        double x = r + random.nextDouble() * (getWidth() - 2 * r); // Ensure ball is fully within bounds
        double y = r + random.nextDouble() * (getHeight() - 2 * r);

        double dx = random.nextDouble() * (random.nextBoolean() ? -2 : 2);
        double dy = random.nextDouble() * (random.nextBoolean() ? -2 : 2);

        int color = Color.rgb(random.nextInt(256) , random.nextInt(256), random.nextInt(256));
        Bola nuevaBola = new Bola(x, y, r, color, dx, dy);
        boolean collision = false;
        for (Bola ball : balls) {
            if (nuevaBola.collisionDetect(ball)) {
                collision = true;
            }
        }
        if (!collision) {
            balls.add(nuevaBola);
        }
    }

    // Example implementations for the Bola class.
    public static class Bola {
        private double x;
        private double y;
        private final double r;
        private double m;
        private final int color;
        private double dx, dy;
        private final android.graphics.Matrix matrix;

        public Bola(double x, double y, double r, int color, double dx, double dy) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.color = color;
            this.dx = dx;
            this.dy = dy;
            this.m = Math.pow(r, 3); // Mass proportional to radius cubed
            matrix = new android.graphics.Matrix(); // Initialize the matrix
        }

        public RectF getBounds() {
            return new RectF((float) (x - r), (float) (y - r), (float) (x + r), (float) (y + r));
        }

        public int getColor() {
            return color;
        }

        public android.graphics.Matrix getMatrix() {
            matrix.reset();
            matrix.preScale((float) r, (float) r);
            matrix.preTranslate((float) (x / r) - 1, (float) (y / r) - 1);
            return matrix;
        }

        public void move(int width, int height) {
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
}