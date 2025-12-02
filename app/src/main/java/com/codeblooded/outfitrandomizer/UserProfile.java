package com.codeblooded.outfitrandomizer;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.codeblooded.outfitrandomizer.data.local.OutfitDao;
import com.codeblooded.outfitrandomizer.data.local.WardrobeDAO;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.codeblooded.outfitrandomizer.data.local.AppDatabase;
import com.codeblooded.outfitrandomizer.data.local.UserDao;
import com.codeblooded.outfitrandomizer.data.local.UserEntity;

import java.util.concurrent.Executors;

public class UserProfile extends AppCompatActivity {
    ImageView profileImage;
    TextView usernameLabel, emailLabel;

    private UserDao userDao;
    private WardrobeDAO wardrobeDAO;
    private OutfitDao outfitDao;

    private TextView outfitsCount, favoritesCount;

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

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_userProfile);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            int bottom = (insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom) + 30;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
            return insets;
        });

        //Hooks
        profileImage = findViewById(R.id.profile_image_userProfile);
        usernameLabel = findViewById(R.id.username_label_userProfile);
        emailLabel = findViewById(R.id.email_label_userProfile);
        outfitsCount = findViewById(R.id.outfits_count_userProfile);
        favoritesCount = findViewById(R.id.favorites_count_userProfile);

        Button resetButton = findViewById(R.id.clearStorage_button_userProfile);
        Button logoutButton = findViewById(R.id.logout_button_userProfile);

        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        userDao = database.userDao();
        wardrobeDAO = database.wardrobeDAO();
        outfitDao = database.outfitDao();

        //Show All Data
        showAllUserData();
        loadCounts();

        resetButton.setOnClickListener(v -> confirmClearStorage());
        logoutButton.setOnClickListener(v -> logout());

        bottomNav.setSelectedItemId(R.id.nav_user);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_wardrobe) {
                startActivity(new Intent(this, Wardrobe.class));
                return true;
            } else if (id == R.id.nav_generator) {
                startActivity(new Intent(this, Generator.class));
                return true;
            } else if (id == R.id.nav_outfits) {
                startActivity(new Intent(this, Outfits.class));
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
                    runOnUiThread(() -> bindToViews(user.username, user.email));
                }
            });
        }
    }

    private void loadCounts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int outfitCount = outfitDao.countAll();
            int favoriteCount = outfitDao.countFavorites();
            runOnUiThread(() -> {
                outfitsCount.setText(String.valueOf(outfitCount));
                favoritesCount.setText(String.valueOf(favoriteCount));
            });
        });
    }

    private void confirmClearStorage() {
        new AlertDialog.Builder(this).setTitle("Clear Wardrobe & Outfit Storage").setMessage("This will delete all saved wardrobe items and outfits. This cannot be undone.")
                .setPositiveButton("Delete", ((dialog, which) -> clearData()))
                .setNegativeButton("Cancel", null).show();
    }

    private void clearData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            wardrobeDAO.clearAll();
            outfitDao.clearAll();
            runOnUiThread(() -> {
                Toast.makeText(this, "All Wardrobe & Outfits Deleted.", Toast.LENGTH_SHORT).show();
                loadCounts();
            });
        });
    }

    private void logout() {
        getSharedPreferences("session", MODE_PRIVATE).edit().remove("current_username").apply();
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void bindToViews(String u, String e) {
        usernameLabel.setText(u != null ? u : "");
        emailLabel.setText(e != null ? e : "");
    }
}