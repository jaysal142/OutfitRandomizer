package com.codeblooded.outfitrandomizer;

import android.app.AlertDialog;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.codeblooded.outfitrandomizer.data.local.AppDatabase;
import com.codeblooded.outfitrandomizer.data.local.OutfitDao;
import com.codeblooded.outfitrandomizer.data.local.OutfitEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OutfitDetails extends AppCompatActivity {

    private ImageView jacket, shirt, pants, shoes;
    private TextView outfitName, saveDate;

    private ImageButton layoutBtn, favBtn;
    private Button editBtn, deleteBtn;

    private OutfitEntity outfit;
    private OutfitDao outfitDao;
    private ExecutorService databaseExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_outfit_details);

        jacket = findViewById(R.id.jacket_image_outfitDetails);
        shirt = findViewById(R.id.shirt_image_outfitDetails);
        pants = findViewById(R.id.pants_image_outfitDetails);
        shoes = findViewById(R.id.shoes_image_outfitDetails);
        outfitName = findViewById(R.id.outfit_name_outfitDetails);
        saveDate = findViewById(R.id.outfit_date_outfitDetails);

        findViewById(R.id.back_button_outfitDetails).setOnClickListener(v -> finish());
        layoutBtn = findViewById(R.id.layout_button_outfitDetails);
        favBtn = findViewById(R.id.favorite_button_outfitDetails);
        favBtn.setOnClickListener(v -> toggleFavorite());
        editBtn = findViewById(R.id.editName_button_outfitDetails);
        editBtn.setOnClickListener(v -> showEdit());
        deleteBtn = findViewById(R.id.delete_button_outfitDetails);
        deleteBtn.setOnClickListener(v -> showDelete());

        outfitDao = AppDatabase.getInstance(this).outfitDao();
        outfit = (OutfitEntity) getIntent().getSerializableExtra("outfit");
        if (outfit != null) {
            bindOutfit();
        } else {
            finish();
            return;
        }

        databaseExecutor = Executors.newSingleThreadExecutor();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateFavoriteButton() {
        if (outfit.isFavorite) {
            favBtn.setImageResource(R.drawable.favorite_filled_24dp);
        } else {
            favBtn.setImageResource(R.drawable.favorite_24dp);
        }
    }

    private void toggleFavorite() {
        if (outfit == null) return;
        outfit.isFavorite = !outfit.isFavorite;
        updateFavoriteButton();
        new Thread(() -> {
            outfitDao.upsert(outfit);
        }).start();
    }

    private void loadImage(ImageView image, String uriString) {
        if (uriString == null || uriString.trim().isEmpty()) return;
        try {
            image.setImageURI(Uri.parse(uriString));
        } catch (Exception except) {
            except.printStackTrace();
        }
    }
    private void bindOutfit() {
        outfitName.setText(outfit.name);
        String formattedDate = android.text.format.DateFormat.format("MMM dd, yyyy h:mm a", outfit.createdAt).toString();
        saveDate.setText(formattedDate);

        loadImage(jacket, outfit.jacketImageUri);
        loadImage(shirt, outfit.shirtImageUri);
        loadImage(pants, outfit.pantsImageUri);
        loadImage(shoes, outfit.shoesImageUri);

        updateFavoriteButton();
    }

    private void showEdit() {
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(outfit.name != null ? outfit.name : "");

        new AlertDialog.Builder(this).setTitle("Rename Outfit").setView(input).setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Please enter a name.", Toast.LENGTH_SHORT).show();
                return;
            }

            outfit.name = newName;

            databaseExecutor.execute(() -> {
                outfitDao.upsert(outfit);
                runOnUiThread(() -> {
                    outfitName.setText(newName);
                    Toast.makeText(this, "Outfit Renamed.", Toast.LENGTH_SHORT).show();
                });
            });
        }).setNegativeButton("Cancel", null).show();
    }

    private void showDelete() {
        new AlertDialog.Builder(this).setTitle("Delete Outfit").setMessage("Are you sure you want to DELETE this outfit?").setPositiveButton("Delete", (dialog, which) -> {
            databaseExecutor.execute(() -> {
                outfitDao.delete(outfit);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Outfit Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }).setNegativeButton("Cancel", null).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }
}