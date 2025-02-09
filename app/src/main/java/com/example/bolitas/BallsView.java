package com.example.bolitas;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BallsView extends View {

    //private static final String TAG = "BolitasView";

    private final List<Ball> balls;
    private final Paint ballPaint;
    private final Paint backgroundPaint;
    private Ball controlBall;

    // Fixed gradient for all balls (white to transparent)
    private RadialGradient fixedBallGradient;

    // Constants for gradient parameters
    private static final float BACKGROUND_GRADIENT_CENTER_X_RATIO = 0.1f;
    private static final float BACKGROUND_GRADIENT_CENTER_Y_RATIO = 0.1f;
    private static final float BACKGROUND_GRADIENT_RADIUS_RATIO = 1.5f;
    private static final float FIXED_BALL_GRADIENT_CENTER_X = 0.5f;
    private static final float FIXED_BALL_GRADIENT_CENTER_Y = 0.5f;
    private static final float FIXED_BALL_GRADIENT_RADIUS = 3.0f;
    private static final int CONTROL_BALL_INITIAL_RADIUS = 40;
    private static final double CONTROL_BALL_MASS = 1e20;
    private static final float BALL_SHRINK_FACTOR = 0.95f;

    private final Random random;
    // Paint for drawing text
    private final Paint textPaint;
    private int ballCount;
    private Context context; // Store the context here
    private int backgroundColor;
    private int maxSize;
    private int minSize;

    public BallsView(Context context) {
        this(context, null);
    }

    public BallsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BallsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context; // Store the context passed to the constructor
        balls = new ArrayList<>();
        ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint = new Paint();
        random = new Random();
        // Initialize the text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40); // Adjust the text size as needed
        ballCount = 0;
        init(); //now the call to
    }

    private void init() {
        // Initialize the background paint
        backgroundPaint.setStyle(Paint.Style.FILL); // Ensure it fills the area

        //Get shared preferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySettings", Context.MODE_PRIVATE);
        backgroundColor = sharedPreferences.getInt("backgroundColor", 0); // Default to first item (white)
        maxSize = sharedPreferences.getInt("maxSize", 100); // Default to 100
        minSize = sharedPreferences.getInt("minSize", 20); // Default to 20



    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Set up the fixed ball gradient
        fixedBallGradient = new RadialGradient(
                FIXED_BALL_GRADIENT_CENTER_X, FIXED_BALL_GRADIENT_CENTER_Y, FIXED_BALL_GRADIENT_RADIUS,
                new int[]{Color.WHITE, Color.DKGRAY, Color.BLACK}, // Adding an intermediate gray
                new float[]{0f, 0.7f, 1f}, // Adjusting positions for softer transition
                Shader.TileMode.CLAMP);
        updateBackgroundColor(backgroundColor);
        if (balls.isEmpty()) {
            controlBall = createBall(w / 2.0, h / 2.0, CONTROL_BALL_INITIAL_RADIUS, Color.BLACK, 0, 0);
            controlBall.setMass(CONTROL_BALL_MASS); //very high mass to prevent control ball to move
            balls.add(controlBall);
            ballCount++;
        }
    }

    private void updateBackgroundColor(int backgroundColor) {
        int[] colors = {Color.WHITE, Color.BLACK, Color.RED, Color.BLUE};
        if (backgroundColor < 0 || backgroundColor >= colors.length)
            backgroundColor = 0; //Validate value
        int color = colors[backgroundColor];
        // Set up the background gradient
        RadialGradient radialGradient = new RadialGradient(
                getWidth() * BACKGROUND_GRADIENT_CENTER_X_RATIO, getHeight() * BACKGROUND_GRADIENT_CENTER_Y_RATIO,
                Math.max(getWidth(), getHeight()) / BACKGROUND_GRADIENT_RADIUS_RATIO,
                new int[]{color, Color.BLACK},
                null, Shader.TileMode.CLAMP);
        backgroundPaint.setShader(radialGradient);
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

        // 5. Draw the ball count text
        drawBallCount(canvas);

        // 6. Schedule next redraw.
        invalidate();
    }

    private void drawBalls(Canvas canvas) {
        for (Ball ball : balls) {
            // Set the fixed gradient
            fixedBallGradient.setLocalMatrix(ball.getMatrix()); //Adjust the center
            ballPaint.setShader(fixedBallGradient);

            // Create a color filter to blend with the ball's color
            ColorFilter filter = new LightingColorFilter(ball.getColor(), 0); // Multiply by the ball's color

            // Apply the filter
            ballPaint.setColorFilter(filter);

            // Draw the ball
            RectF oval = ball.getBounds();
            canvas.drawOval(oval, ballPaint);

            // Reset the shader to draw the next ball
            ballPaint.setShader(null);

            // Reset the filter
            ballPaint.setColorFilter(null);
        }
    }

    private void moveBalls() {
        Iterator<Ball> iterator = balls.iterator();
        while (iterator.hasNext()) {
            Ball ball = iterator.next();
            if (ball.isAbsorbed()) {
                ball.moveTowards(controlBall, BALL_SHRINK_FACTOR); // Move towards control ball if being absorbed
                if (ball.getRadius() <= 1) {
                    iterator.remove(); // Remove from the list
                    ballCount--;
                }
            } else {
                ball.move(getWidth(), getHeight());
            }
        }
    }


    private void handleCollisions() {
        // Make a copy of the list to avoid ConcurrentModificationException
        List<Ball> ballsCopy = new ArrayList<>(balls);
        for (Ball ball : ballsCopy) {
            //No need to check against the control ball twice
            for (int j = balls.indexOf(ball) + 1; j < balls.size(); j++) {
                Ball ball2 = balls.get(j);
                if (ball.collisionDetect(ball2)) {
                    if (ball.equals(controlBall)){
                        ball2.setAbsorbed(true);
                    } else {
                        // Handle the collision here.
                        ball.resolveCollision(ball2);
                    }
                }
            }
        }
    }
    private void drawBallCount(Canvas canvas) {
        String text = ballCount + " Balls";
        float textWidth = textPaint.measureText(text);
        float x = (getWidth() - textWidth) / 2f; // Center horizontally
        float y = getHeight() - textPaint.getTextSize() / 2; // Place near the bottom

        canvas.drawText(text, x, y, textPaint);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            controlBall.setVelocity(
                    (event.getX() - controlBall.getX()) / 20,
                    (event.getY() - controlBall.getY()) / 20
            );
            controlBall.setPosition(event.getX(), event.getY());
            return true;
        }
        return super.onTouchEvent(event);
    }
    public void addBall() {
        Ball newBall = createRandomBall();

        // Check for collision before adding
        if (!isCollidingWithExistingBalls(newBall)) {
            balls.add(newBall);
            ballCount++;
        }
    }

    private boolean isCollidingWithExistingBalls(Ball newBall) {
        for (Ball ball : balls) {
            if (newBall.collisionDetect(ball)) {
                return true;
            }
        }
        return false;
    }

    private Ball createRandomBall() {
        double radius = 40 * (0.5 + random.nextDouble() * 1.5);
        double x = radius + random.nextDouble() * (getWidth() - 2 * radius); // Ensure ball is fully within bounds
        double y = radius + random.nextDouble() * (getHeight() - 2 * radius);

        double dx = random.nextDouble() * (random.nextBoolean() ? -2 : 2);
        double dy = random.nextDouble() * (random.nextBoolean() ? -2 : 2);

        int color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        return createBall(x, y, radius, color, dx, dy);
    }

    private Ball createBall(double x, double y, double radius, int color, double dx, double dy) {
        return new Ball(x, y, radius, color, dx, dy);
    }
}