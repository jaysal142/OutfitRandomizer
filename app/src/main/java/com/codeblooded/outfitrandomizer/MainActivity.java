package com.codeblooded.outfitrandomizer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    //Variables
    Animation topAnim, bottomAnim;
    ImageView image;
    TextView tagline1, tagline2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Animations
        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        //Hooks
        image = findViewById(R.id.brand_logo_main);
        tagline1 = findViewById(R.id.tagline_main_1);
        tagline2 = findViewById(R.id.tagline_main_2);


        image.setAnimation(topAnim);
        tagline1.setAnimation(bottomAnim);
        tagline2.setAnimation(bottomAnim);


        int SPLASH_SCREEN = 5000;
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this,Login.class);

            Pair[] pairs = new Pair[2];
            pairs[0] = new Pair<View,String>(image, "logo_transition");
            pairs[1] = new Pair<View,String>(tagline1, "header_transition");

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,pairs);
            startActivity(intent, options.toBundle());
        }, SPLASH_SCREEN);
    }
}