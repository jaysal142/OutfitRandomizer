package com.codeblooded.outfitrandomizer;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codeblooded.outfitrandomizer.ui.OutfitCardAdapter;
import com.codeblooded.outfitrandomizer.ui.OutfitViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Outfits extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_outfits);

        RecyclerView recycler = findViewById(R.id.outfits_recycler_outfits);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);

        OutfitCardAdapter adapter = new OutfitCardAdapter(outfit -> {
            Intent intent = new Intent(this, OutfitDetails.class);
            intent.putExtra("outfit", outfit);
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        OutfitViewModel vm = new ViewModelProvider(this).get(OutfitViewModel.class);

        vm.getOutfits().observe(this, outfits -> adapter.submitList(outfits));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_outfits);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            int bottom = (insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom) + 30;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
            return insets;
        });

        bottomNav.setSelectedItemId(R.id.nav_outfits);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomePage.class));
                return true;
            } else if (id == R.id.nav_generator) {
                startActivity(new Intent(this, Generator.class));
                return true;
            } else if (id == R.id.nav_wardrobe) {
                startActivity(new Intent(this, Wardrobe.class));
                return true;
            } else if (id == R.id.nav_user) {
                startActivity(new Intent(this, UserProfile.class));
                return true;
            }
            return false;
        });
    }
}