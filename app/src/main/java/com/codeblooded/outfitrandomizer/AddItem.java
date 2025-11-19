package com.codeblooded.outfitrandomizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
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
    private ImageView previewImage;

    private static final int REQUEST_CODE_CAMERA = 2001;
    private String capturedImageUriString = null;

    private AppDatabase database;
    private WardrobeDAO wardrobeDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);

        categorySpinner = findViewById(R.id.category_spinner_addItem);
        itemName = findViewById(R.id.item_name_addItem);
        previewImage = findViewById(R.id.image_preview_addItem);

        database = AppDatabase.getInstance(getApplicationContext());
        wardrobeDAO = database.wardrobeDAO();

        setupCategorySpinner();

        findViewById(R.id.takePhoto_button_addItem).setOnClickListener(v -> openCamera());
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
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK && data != null) {
            capturedImageUriString = data.getStringExtra("image_uri");

            if (capturedImageUriString != null) {
                Uri uri = Uri.parse(capturedImageUriString);
                previewImage.setImageURI(uri);
            }
        }
    }
}