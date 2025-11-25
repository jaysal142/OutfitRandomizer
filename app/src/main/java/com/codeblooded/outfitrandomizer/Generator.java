package com.codeblooded.outfitrandomizer;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.codeblooded.outfitrandomizer.data.local.AppDatabase;
import com.codeblooded.outfitrandomizer.data.local.WardrobeDAO;
import com.codeblooded.outfitrandomizer.data.local.WardrobeEntity;
import com.codeblooded.outfitrandomizer.data.local.OutfitDao;
import com.codeblooded.outfitrandomizer.data.local.OutfitEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.Executors;

public class Generator extends AppCompatActivity {
    private AppDatabase database;
    private WardrobeDAO wardrobeDAO;
    private OutfitDao outfitDao;

    private ConstraintLayout saveOverlay;
    private TextInputLayout outfitNameInput;
    private ImageView jacketImage;
    private ImageView shirtImage;
    private ImageView pantsImage;
    private ImageView shoesImage;
    private Button randomButton;

    private WardrobeEntity selectedJacket;
    private WardrobeEntity selectedShirt;
    private WardrobeEntity selectedPants;
    private WardrobeEntity selectedShoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_generator);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_generator);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            int bottom = (insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom) + 30;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
            return insets;
        });

        bottomNav.setSelectedItemId(R.id.nav_generator);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_wardrobe) {
                startActivity(new Intent(this, Wardrobe.class));
                return true;
            } else if (id == R.id.nav_user) {
                startActivity(new Intent(this, UserProfile.class));
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

        database = AppDatabase.getInstance(getApplicationContext());
        wardrobeDAO = database.wardrobeDAO();
        outfitDao = database.outfitDao();

        jacketImage = findViewById(R.id.jacket_image_generator);
        shirtImage = findViewById(R.id.shirt_image_generator);
        pantsImage = findViewById(R.id.pants_image_generator);
        shoesImage = findViewById(R.id.shoes_image_generator);

        randomButton = findViewById(R.id.randomize_button_generator);
        randomButton.setOnClickListener(v -> { generateRandomOutfit(); });

        saveOverlay = findViewById(R.id.save_overlay_generator);
        saveOverlay.setVisibility(GONE);
        ImageButton saveOverlayButton = findViewById(R.id.saveOverlay_button_generator);
        saveOverlayButton.setOnClickListener(v -> { saveOverlay.setVisibility(VISIBLE); });
        outfitNameInput = findViewById(R.id.outfit_name_generator);
        Button saveButton = findViewById(R.id.save_button_generator);
        saveButton.setOnClickListener(v -> { saveOutfit(); });
        Button cancelButton = findViewById(R.id.cancel_button_generator);
        cancelButton.setOnClickListener(v -> { saveOverlay.setVisibility(GONE); });
    }

    private void generateRandomOutfit() {
        Executors.newSingleThreadExecutor().execute(() -> {
            selectedJacket = wardrobeDAO.getRandomByCategory("Jacket");
            selectedShirt = wardrobeDAO.getRandomByCategory("Shirt");
            selectedPants = wardrobeDAO.getRandomByCategory("Pants");
            selectedShoes = wardrobeDAO.getRandomByCategory("Shoes");

            runOnUiThread(() -> {
                if (selectedJacket != null) {
                    Bitmap bMap = loadBitmap(selectedJacket.imageUri);
                    if (bMap != null) jacketImage.setImageBitmap(bMap);
                } else {
                    Toast.makeText(this, "No Jackets Found", Toast.LENGTH_SHORT).show();
                    jacketImage.setImageResource(0);
                }
                if (selectedShirt != null) {
                    Bitmap bMap = loadBitmap(selectedShirt.imageUri);
                    if (bMap != null) shirtImage.setImageBitmap(bMap);
                } else {
                    Toast.makeText(this, "No Shirts Found", Toast.LENGTH_SHORT).show();
                    shirtImage.setImageResource(0);
                }
                if (selectedPants != null) {
                    Bitmap bMap = loadBitmap(selectedPants.imageUri);
                    if (bMap != null) pantsImage.setImageBitmap(bMap);
                } else {
                    Toast.makeText(this, "No Pants Found", Toast.LENGTH_SHORT).show();
                    pantsImage.setImageResource(0);
                }
                if (selectedShoes != null) {
                    Bitmap bMap = loadBitmap(selectedShoes.imageUri);
                    if (bMap != null) shoesImage.setImageBitmap(bMap);
                } else {
                    Toast.makeText(this, "No Shoes Found", Toast.LENGTH_SHORT).show();
                    shoesImage.setImageResource(0);
                }
            });
        });
        Toast.makeText(this, "Outfit Randomized.", Toast.LENGTH_SHORT).show();
    }

    private void saveOutfit() {
        if (selectedJacket == null || selectedShirt == null || selectedPants == null || selectedShoes == null) {
            Toast.makeText(this, "Generate an outfit to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = null;
        if (outfitNameInput.getEditText() != null && outfitNameInput.getEditText().getText() != null) {
            name = outfitNameInput.getEditText().getText().toString().trim();
        }

        if (name == null || name.isBlank()) {
            name = selectedJacket.name + " / " + selectedShirt.name + " / " + selectedPants.name + " / " + selectedShoes.name;
        }

        long time = System.currentTimeMillis();
        OutfitEntity outfit = new OutfitEntity(name, selectedJacket.imageUri, selectedShirt.imageUri, selectedPants.imageUri, selectedShoes.imageUri, time);

        Executors.newSingleThreadExecutor().execute(() -> {
            outfitDao.insert(outfit);
            runOnUiThread(() -> {
                Toast.makeText(this, "Outfit saved.", Toast.LENGTH_SHORT).show();
                outfitNameInput.getEditText().setText("");
            });
        });

        saveOverlay.setVisibility(GONE);
    }

    private Bitmap loadBitmap(String uriString) {
        try {
            Uri uri = Uri.parse(uriString);
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (Exception except) {
            except.printStackTrace();
            return null;
        }
    }
}