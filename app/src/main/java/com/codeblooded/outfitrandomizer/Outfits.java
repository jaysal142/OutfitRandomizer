package com.codeblooded.outfitrandomizer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codeblooded.outfitrandomizer.data.local.OutfitEntity;
import com.codeblooded.outfitrandomizer.ui.OutfitCardAdapter;
import com.codeblooded.outfitrandomizer.ui.OutfitViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class Outfits extends AppCompatActivity {
    private OutfitCardAdapter adapter;
    private TextView emptyMessage;

    private List<OutfitEntity> allOutfits = new ArrayList<>();
    private String currentSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_outfits);

        RecyclerView recycler = findViewById(R.id.outfits_recycler_outfits);
        emptyMessage = findViewById(R.id.empty_message_outfits);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);

        adapter = new OutfitCardAdapter(outfit -> {
            Intent intent = new Intent(this, OutfitDetails.class);
            intent.putExtra("outfit", outfit);
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        TextInputLayout searchLayout = findViewById(R.id.search_outfits);
        TextInputEditText searchText = (TextInputEditText) searchLayout.getEditText();
        if (searchText != null) {
            searchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    outfitsFilter(s != null ? s.toString() : "");
                }
            });
        }

        OutfitViewModel vm = new ViewModelProvider(this).get(OutfitViewModel.class);
        vm.getOutfits().observe(this, outfits -> {
            allOutfits = (outfits != null) ? outfits : new ArrayList<>();
            outfitsFilter(currentSearch);
        });

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

    private void outfitsFilter(String search) {
        currentSearch = (search != null) ? search.trim() : "";
        List <OutfitEntity> displayList = new ArrayList<>();
        if (currentSearch.isEmpty()) {
            displayList.addAll(allOutfits);
        } else {
            String lower = currentSearch.toLowerCase();
            for (OutfitEntity outfit : allOutfits) {
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
}