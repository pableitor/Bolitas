package com.example.bolitas; // Replace with your actual package name

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;

class Bola {
    public double x, y, despx, despy, r, m;
    int color;

    public Bola(Context context, int width, int height) {
        Random random = new Random();
        r = 40 * (0.5 + random.nextDouble() * 1.5);
        x = r + random.nextDouble() * (width - 2 * r); // Ensure ball is fully within bounds
        y = r + random.nextDouble() * (height - 2 * r);

        despx = random.nextDouble() * (random.nextBoolean() ? -2 : 2);
        despy = random.nextDouble() * (random.nextBoolean() ? -2 : 2);

        m = Math.pow(r, 3);
        color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public void mueve(int width, int height) {
        if (((x + r) >= width) || ((x - r) <= 0)) {
            despx *= -1;
            x = Math.max(r, Math.min(x, width - r)); // Prevent ball from getting stuck
        }
        x += despx;

        if (((y + r) >= height) || ((y - r) <= 0)) {
            despy *= -1;
            y = Math.max(r, Math.min(y, height - r)); // Prevent ball from getting stuck
        }
        y += despy;
    }

    public synchronized boolean collisionDetect(ArrayList<Bola> listaBolas) {
        boolean collision = false;
        double A, B, C;

        int curr_ball = listaBolas.indexOf(this);

        for (int i = 0; i < listaBolas.size(); i++) {
            if (i != curr_ball) {
                Bola hold_check_ball = listaBolas.get(i);

                A = x - hold_check_ball.x;
                B = y - hold_check_ball.y;
                C = Math.sqrt((A * A) + (B * B));

                if (C <= (r + hold_check_ball.r)) {
                    double x1 = x;
                    double y1 = y;
                    double x2 = hold_check_ball.x;
                    double y2 = hold_check_ball.y;
                    double dx = x2 - x1;
                    double dy = y2 - y1;
                    double alpha = Math.atan2(dy, dx);
                    double cos = Math.cos(alpha);
                    double sin = Math.sin(alpha);

                    double x1t = cos * x1 + sin * y1;
                    double y1t = cos * y1 - sin * x1;
                    double x2t = cos * x2 + sin * y2;
                    double y2t = cos * y2 - sin * x2;

                    if (x1t < x2t) {
                        x1t = x1t - (r + hold_check_ball.r - C) / 2;
                        x2t = x2t + (r + hold_check_ball.r - C) / 2;
                    } else {
                        x2t = x2t - (r + hold_check_ball.r - C) / 2;
                        x1t = x1t + (r + hold_check_ball.r - C) / 2;
                    }

                    x1 = cos * x1t - sin * y1t;
                    y1 = cos * y1t + sin * x1t;
                    x2 = cos * x2t - sin * y2t;
                    y2 = cos * y2t + sin * x2t;

                    x = x1;
                    y = y1;
                    hold_check_ball.x = x2;
                    hold_check_ball.y = y2;

                    double m21 = hold_check_ball.m / m;

                    double vx21 = hold_check_ball.despx - despx;
                    double vy21 = hold_check_ball.despy - despy;

                    double a = dy / dx;

                    double dvx2 = -2 * (vx21 + a * vy21) / ((1 + a * a) * (1 + m21));
                    hold_check_ball.despx = hold_check_ball.despx + dvx2;
                    hold_check_ball.despy = hold_check_ball.despy + a * dvx2;
                    despx = despx - m21 * dvx2;
                    despy = despy - a * m21 * dvx2;

                    collision = true;
                    break;
                }
            }
        }
        return collision;
    }
}

class BolitasView extends View {
    ArrayList<Bola> listaBolas = new ArrayList<>();
    Paint paint = new Paint();
    //Random random = new Random();

    public BolitasView(Context context) {
        super(context);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (listaBolas.isEmpty()) {
            Bola nuevaBola = new Bola(getContext(), w, h);
            nuevaBola.despx = 0;
            nuevaBola.despy = 0;
            nuevaBola.m = 1e20;
            listaBolas.add(nuevaBola);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.YELLOW);

        for (Bola bola : listaBolas) {
            paint.setColor(bola.color);
            canvas.drawOval((float) (bola.x - bola.r), (float) (bola.y - bola.r),
                    (float) (bola.x + bola.r), (float) (bola.y + bola.r), paint);
        }

        for (Bola bola : listaBolas) {
            bola.mueve(getWidth(), getHeight());
        }

        for (int i = 0; i < listaBolas.size(); i++) {
            for (int j = i + 1; j < listaBolas.size(); j++) {
                if (listaBolas.get(i).collisionDetect(listaBolas)) {
                    // Handle potential issues after collision, if needed
                }
            }
        }


        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            Bola controlBall = listaBolas.get(0);
            controlBall.despx = (event.getX() - controlBall.x) / 20;
            controlBall.despy = (event.getY() - controlBall.y) / 20;
            controlBall.x = event.getX();
            controlBall.y = event.getY();
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void addBall() {
        Bola nuevaBola;

        do { // crea una bola random y check si colisiona con alguna otra

            nuevaBola = new Bola(getContext(), getWidth(), getHeight());
                if (nuevaBola.collisionDetect(listaBolas)) {

                    break;
                }

        } while (true);
        listaBolas.add(nuevaBola);
    }
}

public class MainActivity extends AppCompatActivity {

    BolitasView bolitasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bolitasView = new BolitasView(this);
        setContentView(bolitasView);

        bolitasView.setOnClickListener(v -> bolitasView.addBall());
    }
}