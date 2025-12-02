package com.codeblooded.outfitrandomizer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.codeblooded.outfitrandomizer.data.local.AppDatabase;
import com.codeblooded.outfitrandomizer.data.local.WardrobeDAO;
import com.codeblooded.outfitrandomizer.data.local.WardrobeEntity;
import com.google.android.material.textfield.TextInputLayout;

public class AddItem extends AppCompatActivity {
    private Spinner categorySpinner;
    private TextInputLayout itemName;
    private String capturedImageUriString = null;
    private WardrobeDAO wardrobeDAO;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);

        categorySpinner = findViewById(R.id.category_spinner_addItem);
        itemName = findViewById(R.id.item_name_addItem);
        ImageView previewImage = findViewById(R.id.image_preview_addItem);

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                capturedImageUriString = data.getStringExtra("image_uri");

                if (capturedImageUriString != null) {
                    Uri uri = Uri.parse(capturedImageUriString);
                    previewImage.setImageURI(uri);
                }
            }
        });

        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        wardrobeDAO = database.wardrobeDAO();

        setupCategorySpinner();

        findViewById(R.id.takePhoto_button_addItem).setOnClickListener(v -> openCamera());
        findViewById(R.id.cancel_button_addItem).setOnClickListener(v -> finish());
        findViewById(R.id.save_button_addItem).setOnClickListener(v -> saveWardrobeItem());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupCategorySpinner() {
        String[] categories = new String[] { "Jacket", "Shirt", "Pants", "Shoes" };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void saveWardrobeItem() {
        assert itemName.getEditText() != null;
        String name = itemName.getEditText().getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (capturedImageUriString == null) {
            Toast.makeText(this, "Please add a photo first.", Toast.LENGTH_SHORT).show();
            return;
        }

        WardrobeEntity item = new WardrobeEntity();
        item.name = name;
        item.category = category;
        item.imageUri = capturedImageUriString;

        new Thread(() -> {
            wardrobeDAO.insert(item);
            runOnUiThread(this::finish);
        }).start();
    }

    private void openCamera() {
        Intent intent = new Intent(this, Camera.class);
        cameraLauncher.launch(intent);
    }
}