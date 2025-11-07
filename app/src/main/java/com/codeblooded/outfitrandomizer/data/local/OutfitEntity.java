package com.codeblooded.outfitrandomizer.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "outfits")
public class OutfitEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String imageUri;
    public long createdAt;

    public OutfitEntity(String name, String imageUri, long createdAt) {
        this.name = name;
        this.imageUri = imageUri;
        this.createdAt = createdAt;
    }
}
