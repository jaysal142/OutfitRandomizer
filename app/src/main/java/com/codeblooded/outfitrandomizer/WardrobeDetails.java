package com.codeblooded.outfitrandomizer;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.io.File;
import java.util.Objects;

public class WardrobeDetails extends AppCompatActivity {
    private static final int REQ_CAMERA_EDIT_ITEM = 4001;

    private ImageView itemImage;
    private TextView itemName, itemCategory;

    private WardrobeEntity item;
    private WardrobeDAO wardrobeDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wardrobe_details);

        itemImage = findViewById(R.id.item_image_wardrobeDetails);
        itemName = findViewById(R.id.item_name_wardrobeDetails);
        itemCategory = findViewById(R.id.item_category_wardrobeDetails);
        Button editButton = findViewById(R.id.edit_button_wardrobeDetails);
        Button deletebutton = findViewById(R.id.delete_button_wardrobeDetails);
        ImageButton backButton = findViewById(R.id.back_button_wardrobeDetails);

        wardrobeDAO = AppDatabase.getInstance(this).wardrobeDAO();

        item = (WardrobeEntity) getIntent().getSerializableExtra("wardrobe_item");
        if (item == null) {
            Toast.makeText(this, "No wardrobe item found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindItem();

        backButton.setOnClickListener(v -> finish());
        editButton.setOnClickListener(v -> showEditDialog());
        deletebutton.setOnClickListener(v -> showDeleteDialog());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void bindItem() {
        itemName.setText(item.name);
        itemCategory.setText(item.category);
        if (item.imageUri != null && !item.imageUri.isEmpty()) {
            itemImage.setImageURI(Uri.parse(item.imageUri));
        } else {
            itemImage.setImageResource(R.drawable.hanger_24dp);
        }
    }

    private void showEditDialog() {
        String[] options = {"Rename Item", "Change Category", "Retake / Recrop Image", "Cancel"};
        new AlertDialog.Builder(this).setTitle("Edit Item").setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showRenameDialog();
                    break;
                case 1:
                    showCategoryDialog();
                    break;
                case 2:
                    launchCamera();
                    break;
                default:
                    dialog.dismiss();
                    break;
            }
        }).show();
    }

    private void showRenameDialog() {
        final EditText input = new EditText(this);
        input.setHint("Item Name");
        input.setText(item.name);

        new AlertDialog.Builder(this).setTitle("Rename Item").setView(input)
            .setPositiveButton("Save", (dialog , which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    item.name = newName;
                    itemName.setText(newName);
                    saveItem();
                }
            }).setNegativeButton("Cancel", null).show();
    }

    private void showCategoryDialog() {
        String[] categories = {"Jacket", "Shirt", "Pants", "Shoes"};

        int checked = -1;
        for (int i = 0; i < categories.length; i++) {
            if (item.category.equalsIgnoreCase(categories[i])) {
                checked = i;
                break;
            }
        }

        new AlertDialog.Builder(this).setTitle("Change Category").setSingleChoiceItems(categories, checked, null)
            .setPositiveButton("Save", (dialog, whichButton) -> {
                AlertDialog alert = (AlertDialog) dialog;
                int selectedPosition = alert.getListView().getCheckedItemPosition();
                if (selectedPosition >= 0) {
                    String newCategory = categories[selectedPosition];
                    item.category = newCategory;
                    itemCategory.setText(newCategory);
                    saveItem();
                }
            }).setNegativeButton("Cancel", null).show();
    }

    private void launchCamera() {
        Intent intent = new Intent(this, Camera.class);
        startActivityForResult(intent, REQ_CAMERA_EDIT_ITEM);
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this).setTitle("Delete Item").setMessage("Are you sure you want to DELETE this Item?")
                .setPositiveButton("Delete", (dialog, which) -> deleteItem())
                .setNegativeButton("Cancel", null).show();
    }

    private void deleteItem() {
        new Thread(() -> {
            deleteImageFile(item.imageUri);
            wardrobeDAO.delete(item);
            runOnUiThread(() -> {
                Toast.makeText(this, "Item Deleted.", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void saveItem() {
        new Thread(() -> wardrobeDAO.update(item)).start();
    }

    private void deleteImageFile(String uriString) {
        if (uriString == null || uriString.isEmpty()) return;
        try {
            Uri uri = Uri.parse(uriString);
            try {
                getContentResolver().delete(uri, null, null);
            } catch (Exception ignored) {}
            try {
                File file = new File(Objects.requireNonNull(uri.getPath()));
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception ignored) {}
        } catch (Exception except) {
            except.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCOde, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCOde, data);

        if (requestCode == REQ_CAMERA_EDIT_ITEM && resultCOde == RESULT_OK && data != null) {
            String uriString = data.getStringExtra("image_uri");
            if (uriString != null && !uriString.isEmpty()) {
                item.imageUri = uriString;
                itemImage.setImageURI(Uri.parse(uriString));
                saveItem();
            }
        }
    }
}