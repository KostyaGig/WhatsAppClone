package ru.kostya.whatsapp.views.startup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ru.kostya.whatsapp.views.MainActivity;
import ru.kostya.whatsapp.R;

public class SplashScreenctivity extends AppCompatActivity {

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screenctivity);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreenctivity.this,MainActivity.class));
                    finish();
                }
            },1000);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreenctivity.this, WelcomeActivity.class));
                    finish();
                }
            }, 1000);
        }
    }
}