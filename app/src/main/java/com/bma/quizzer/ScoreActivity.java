package com.bma.quizzer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ScoreActivity extends AppCompatActivity {
private TextView scored,total;
private AppCompatButton done;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        scored = findViewById(R.id.scored);
        total = findViewById(R.id.total);
        done = findViewById(R.id.done);

        scored.setText(String.valueOf(getIntent().getIntExtra("score",0)));
        String totals = String.valueOf(getIntent().getIntExtra("total", 0));
        total.setText("Out of "+totals);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}