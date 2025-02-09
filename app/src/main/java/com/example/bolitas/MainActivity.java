
package com.example.bolitas;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private BallsView bolitasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bolitasView = new BallsView(this);
        setContentView(bolitasView);

        bolitasView.setOnClickListener(v -> bolitasView.addBall());
    }
}