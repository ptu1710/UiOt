package com.ixxc.uiot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ixxc.myuit.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(null);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }, 500);
    }
}