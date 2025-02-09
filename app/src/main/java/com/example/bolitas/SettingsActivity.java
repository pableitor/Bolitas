package com.example.bolitas;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private Spinner backgroundColorSpinner;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        backgroundColorSpinner = findViewById(R.id.backgroundColorSpinner);

        // Set up the SharedPreferences
        sharedPreferences = getSharedPreferences("MySettings", Context.MODE_PRIVATE);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.background_colors,
                android.R.layout.simple_spinner_item
        );

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        backgroundColorSpinner.setAdapter(adapter);

        // Load the selected background color from SharedPreferences
        int selectedColor = sharedPreferences.getInt("backgroundColor", 0); // Default to white (index 0)
        backgroundColorSpinner.setSelection(selectedColor);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save the selected background color to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("backgroundColor", backgroundColorSpinner.getSelectedItemPosition());
        editor.apply();
    }
}