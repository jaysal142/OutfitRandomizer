package com.codeblooded.outfitrandomizer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codeblooded.outfitrandomizer.data.local.AppDatabase;
import com.codeblooded.outfitrandomizer.data.local.OutfitDao;
import com.codeblooded.outfitrandomizer.data.local.OutfitEntity;
import com.codeblooded.outfitrandomizer.data.local.UserDao;
import com.codeblooded.outfitrandomizer.data.local.UserEntity;
import com.codeblooded.outfitrandomizer.ui.OutfitCardAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HomePage extends AppCompatActivity {

    TextView username;
    BottomNavigationView bottomNav;

    private OutfitCardAdapter adapter;
    private UserDao userDao;

    private TextView emptyMessage;
    private List<OutfitEntity> allFavorites = new ArrayList<>();
    private String currentSearch = "";

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

        RecyclerView favoritesRecycler = findViewById(R.id.favorites_recycler_homePage);
        emptyMessage = findViewById(R.id.empty_message_homePage);

        favoritesRecycler.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecycler.setHasFixedSize(true);

        adapter = new OutfitCardAdapter(outfit -> {
            Intent intent = new Intent(this, OutfitDetails.class);
            intent.putExtra("outfit", outfit);
            startActivity(intent);
        });
        favoritesRecycler.setAdapter(adapter);

        TextInputLayout searchLayout = findViewById(R.id.search_homePage);
        TextInputEditText searchText = (TextInputEditText) searchLayout.getEditText();
        if(searchText != null) {
            searchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    favoritesFilter(s != null ? s.toString() : "");
                }
            });
        }

        OutfitDao outfitDao = AppDatabase.getInstance(this).outfitDao();
        outfitDao.getFavoriteOutfits().observe(this, outfits -> {
            allFavorites = (outfits != null) ? outfits : new ArrayList<>();
            favoritesFilter(currentSearch);
        });

        bottomNav = findViewById(R.id.bottom_nav_homePage);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            int bottom = (insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom) + 30;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
            return insets;
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
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
            } else if (id == R.id.nav_user) {
                startActivity(new Intent(this, UserProfile.class));
                return true;
            }
            return false;
        });
    }

    private void favoritesFilter(String search) {
        currentSearch = (search != null) ? search.trim() : "";
        List <OutfitEntity> displayList = new ArrayList<>();
        if (currentSearch.isEmpty()) {
            displayList.addAll(allFavorites);
        } else {
            String lower = currentSearch.toLowerCase();
            for (OutfitEntity outfit : allFavorites) {
                if (outfit != null && outfit.name != null && outfit.name.toLowerCase().contains(lower)) {
                    displayList.add(outfit);
                }
            }
        }

        adapter.submitList(displayList);
        updateEmptyMessage(displayList.isEmpty());
    }

    private void updateEmptyMessage(boolean isEmpty) {
        if (isEmpty) {
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            emptyMessage.setVisibility(View.GONE);
        }
    }

    private void showAllUserData() {
        String current = getSharedPreferences("session", MODE_PRIVATE).getString("current_username", null);

        if (current != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                UserEntity user = userDao.getUser(current);
                if (user != null) {
                    runOnUiThread(() -> bindToViews(user.username));
                }
            });
        }
    }

    private void bindToViews(String u) {
        username.setText(u != null ? u : "");
    }
}