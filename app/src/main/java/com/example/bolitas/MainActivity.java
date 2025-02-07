
package com.example.bolitas;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private BolitasView bolitasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bolitasView = new BolitasView(this);
        setContentView(bolitasView);

        bolitasView.setOnClickListener(v -> bolitasView.addBall());
    }
}