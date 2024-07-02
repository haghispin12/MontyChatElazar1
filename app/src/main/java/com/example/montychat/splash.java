package com.example.montychat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class splash extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1500; // משך הזמן במילישניות

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setStatusBarColor(ContextCompat.getColor(splash.this,R.color.input_back2));


        // התחלת פעילות ראשית לאחר השהיה של SPLASH_DURATION מילישניות
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(splash.this, log_In.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DURATION);
    }
}
