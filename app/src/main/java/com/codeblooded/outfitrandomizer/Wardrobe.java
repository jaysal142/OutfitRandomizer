package com.codeblooded.outfitrandomizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codeblooded.outfitrandomizer.data.local.AppDatabase;
import com.codeblooded.outfitrandomizer.data.local.WardrobeDAO;
import com.codeblooded.outfitrandomizer.data.local.WardrobeEntity;
import com.codeblooded.outfitrandomizer.ui.WardrobeAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.Executors;

public class Wardrobe extends AppCompatActivity {
    BottomNavigationView bottomNav;
    Button addItem;

    private RecyclerView wardrobeRecycler;
    private WardrobeAdapter wardrobeAdapter;
    private AppDatabase database;
    private WardrobeDAO wardrobeDAO;

    private MaterialButton filterAll, filterJackets, filterShirts, filterPants, filterShoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wardrobe);

        database = AppDatabase.getInstance(getApplicationContext());
        wardrobeDAO = database.wardrobeDAO();
        wardrobeRecycler = findViewById(R.id.wardrobe_recycler_wardrobe);
        wardrobeRecycler.setLayoutManager(new LinearLayoutManager(this));
        wardrobeAdapter = new WardrobeAdapter(this);
        wardrobeRecycler.setAdapter(wardrobeAdapter);

        filterAll = findViewById(R.id.filter_all_wardrobe);
        filterJackets = findViewById(R.id.filter_jackets_wardrobe);
        filterShirts = findViewById(R.id.filter_shirts_wardrobe);
        filterPants = findViewById(R.id.filter_pants_wardrobe);
        filterShoes = findViewById(R.id.filter_shoes_wardrobe);

        loadAllWardrobeItems();

        filterAll.setOnClickListener(v -> {loadAllWardrobeItems();});
        filterJackets.setOnClickListener(v -> {loadCategoryWardrobeItems("Jacket");});
        filterShirts.setOnClickListener(v -> {loadCategoryWardrobeItems("Shirt");});
        filterPants.setOnClickListener(v -> {loadCategoryWardrobeItems("Pants");});
        filterShoes.setOnClickListener(v -> {loadCategoryWardrobeItems("Shoes");});

        bottomNav = findViewById(R.id.bottom_nav_wardrobe);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            int bottom = (insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom) + 30;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
            return insets;
        });

        bottomNav.setSelectedItemId(R.id.nav_wardrobe);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomePage.class));
                return true;
            } else if (id == R.id.nav_generator) {
                startActivity(new Intent(this, Randomizer.class));
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

        addItem = findViewById(R.id.addItem_button_wardrobe);

        addItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddItem.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllWardrobeItems();
    }

    private void loadAllWardrobeItems() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<WardrobeEntity> items = wardrobeDAO.getAll();
            runOnUiThread(() -> {
                if (wardrobeAdapter != null) {
                    wardrobeAdapter.setItems(items);
                }
            });
        });
    }
    private void loadCategoryWardrobeItems(String category) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<WardrobeEntity> items = wardrobeDAO.getByCategory(category);
            runOnUiThread(() -> {
                if (wardrobeAdapter != null) {
                    wardrobeAdapter.setItems(items);
                }
            });
        });
    }
}