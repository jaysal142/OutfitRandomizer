package com.codeblooded.outfitrandomizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;

import com.codeblooded.outfitrandomizer.data.local.AppDatabase;
import com.codeblooded.outfitrandomizer.data.local.UserDao;
import com.codeblooded.outfitrandomizer.data.local.UserEntity;

import java.util.concurrent.Executors;

public class UserProfile extends AppCompatActivity {

    TextInputLayout username, phoneNo, email, password;
    TextView usernameLabel;
    ImageView profileImage;
    BottomNavigationView bottomNav;

    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_homePage);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            int bottom = (insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom) + 30;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
            return insets;
        });

        //Hooks
        username = findViewById(R.id.username_userProfile);
        phoneNo = findViewById(R.id.phone_num_userProfile);
        email = findViewById(R.id.email_userProfile);
        password = findViewById(R.id.password_userProfile);
        usernameLabel = findViewById(R.id.username_label_userProfile);
        profileImage = findViewById(R.id.profile_image_userProfile);
        bottomNav = findViewById(R.id.bottom_nav_homePage);

        bottomNav.setSelectedItemId(R.id.nav_user);

        userDao = AppDatabase.getInstance(getApplicationContext()).userDao();

        //Show All Data
        showAllUserData();

        bottomNav = findViewById(R.id.bottom_nav_homePage);
        bottomNav.setSelectedItemId(R.id.nav_user);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_wardrobe) {
                startActivity(new Intent(this, HomePage.class));
                return true;
            } else if (id == R.id.nav_generator) {
                startActivity(new Intent(this, HomePage.class));
                return true;
            } else if (id == R.id.nav_favorites) {
                startActivity(new Intent(this, HomePage.class));
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomePage.class));
                return true;
            }
            return false;
        });
    }

    private void showAllUserData() {
        String current = getSharedPreferences("session", MODE_PRIVATE).getString("current_username", null);

        if (current != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                UserEntity user = userDao.getUser(current);
                if (user != null) {
                    runOnUiThread(() -> bindToViews(user.username, user.phoneNo, user.email, null));
                }
            });
        }
    }

    private void bindToViews(String u, String p, String e, String pw) {
        if (username.getEditText() != null) {
            username.getEditText().setText(u != null ? u : "");
        }
        if (phoneNo.getEditText() != null) {
            phoneNo.getEditText().setText(p != null ? p : "");
        }
        if (email.getEditText() != null) {
            email.getEditText().setText(e != null ? e : "");
        }
        if (password.getEditText() != null) {
            password.getEditText().setText(pw != null ? pw : "");
        }
        usernameLabel.setText(u != null ? u : "");
    }
}