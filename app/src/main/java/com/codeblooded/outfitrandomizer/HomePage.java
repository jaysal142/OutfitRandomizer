package com.codeblooded.outfitrandomizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codeblooded.outfitrandomizer.data.local.AppDatabase;
import com.codeblooded.outfitrandomizer.data.local.UserDao;
import com.codeblooded.outfitrandomizer.data.local.UserEntity;
import com.codeblooded.outfitrandomizer.ui.OutfitCard;
import com.codeblooded.outfitrandomizer.ui.OutfitCardAdapter;
import com.codeblooded.outfitrandomizer.ui.OutfitViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HomePage extends AppCompatActivity {

    TextView username;
    BottomNavigationView bottomNav;

    private OutfitCardAdapter adapter;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            return insets;
        });

        username = findViewById(R.id.username_label_homePage);
        userDao = AppDatabase.getInstance(getApplicationContext()).userDao();

        showAllUserData();

        RecyclerView recycler = findViewById(R.id.outfits_recycler_homePage);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);

        adapter = new OutfitCardAdapter(item -> {
            //TODO: handle click: open details etc
        });
        recycler.setAdapter(adapter);

        OutfitViewModel vm = new ViewModelProvider(this).get(OutfitViewModel.class);

        vm.getOutfits().observe(this, list -> adapter.submitList(list));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_homePage);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            int bottom = (insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom) + 30;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
            return insets;
        });

        bottomNav = findViewById(R.id.bottom_nav_homePage);
        bottomNav.setSelectedItemId(R.id.nav_home);

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
            } else if (id == R.id.nav_user) {
                startActivity(new Intent(this, UserProfile.class));
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
        username.setText(u != null ? u : "");
    }
}