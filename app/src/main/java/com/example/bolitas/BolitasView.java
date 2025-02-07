package com.example.bolitas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (balls.isEmpty()) {
            controlBall = new Bola(w / 2.0, h / 2.0, 40, Color.BLACK, 0, 0);
            balls.add(controlBall);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); // Call super.onDraw() first

        // 1. Clear background only once per draw cycle.
        canvas.drawColor(Color.YELLOW);

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
            paint.setColor(ball.getColor()); // Use a getter for better encapsulation
            RectF oval = ball.getBounds(); // Use a method to encapsulate calculations
            canvas.drawOval(oval, paint);
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

        int color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        Bola nuevaBola = new Bola(x, y, r, color, dx, dy);
        boolean collision = false;
        for (Bola ball : balls) {
            if (nuevaBola.collisionDetect(ball)) {
                collision = true;
            }
        }
        if(!collision){
            balls.add(nuevaBola);
        }
    }

    // Example implementations for the Bola class.
    public static class Bola {
        private double x, y, r;
        private int color;
        private double dx, dy;

        public Bola(double x, double y, double r, int color, double dx, double dy) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.color = color;
            this.dx = dx;
            this.dy = dy;
        }

        public RectF getBounds() {
            return new RectF((float) (x - r), (float) (y - r), (float) (x + r), (float) (y + r));
        }

        public int getColor() {
            return color;
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
            // Basic Elastic Collision logic, feel free to expand upon this to make more realistic collisions.
            // Swap speeds
            double tempDx = this.dx;
            double tempDy = this.dy;
            this.dx = other.dx;
            this.dy = other.dy;
            other.dx = tempDx;
            other.dy = tempDy;
            // Adjust positions to avoid overlap
            double distance = Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
            double overlap = (this.r + other.r - distance) / 2.0;
            if (distance > 0) {
                double dx = (this.x - other.x) / distance;
                double dy = (this.y - other.y) / distance;
                this.x += overlap * dx;
                this.y += overlap * dy;
                other.x -= overlap * dx;
                other.y -= overlap * dy;
            }
        }
    }
}
