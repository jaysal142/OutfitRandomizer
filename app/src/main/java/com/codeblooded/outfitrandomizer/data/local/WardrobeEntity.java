package com.codeblooded.outfitrandomizer.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "wardrobe_items")
public class WardrobeEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;

    @NonNull
    public String category;

    // URI string for cutout PNG
    public String imageUri;

    public WardrobeEntity() {
        name = "";
        category = "";
    }
}
