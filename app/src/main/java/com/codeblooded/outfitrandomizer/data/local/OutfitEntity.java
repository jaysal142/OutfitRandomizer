package com.codeblooded.outfitrandomizer.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "outfits")
public class OutfitEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String jacketImageUri, shirtImageUri, pantsImageUri, shoesImageUri;
    public long createdAt;

    public OutfitEntity(String name, String jacketImageUri, String shirtImageUri, String pantsImageUri, String shoesImageUri, long createdAt) {
        this.name = name;
        this.jacketImageUri = jacketImageUri;
        this.shirtImageUri = shirtImageUri;
        this.pantsImageUri = pantsImageUri;
        this.shoesImageUri = shoesImageUri;
        this.createdAt = createdAt;
    }
}
