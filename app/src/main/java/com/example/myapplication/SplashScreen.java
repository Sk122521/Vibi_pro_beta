package com.example.myapplication;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.myapplication.databinding.ActivitySplashScreenBinding;
import com.example.myapplication.databinding.ActivityTopicsBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    private ActivitySplashScreenBinding binding;
    private FirebaseAuth mauth;
    private static int SPLASH_TIME_OUT = 5000; // 2 secon// ds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_splash_screen);

     //   provideAnimation();

        mauth = FirebaseAuth.getInstance();


        if(mauth.getCurrentUser() == null){
            new Thread(new Runnable() {
                public void run() {
                    try {
                        // sleep for 3 seconds
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        // start the main activity
                        Intent intent = new Intent(SplashScreen.this, LoginActivityAdmin.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }).start();
        }else{
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

}