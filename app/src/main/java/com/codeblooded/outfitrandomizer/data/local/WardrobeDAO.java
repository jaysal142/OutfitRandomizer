package com.codeblooded.outfitrandomizer.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WardrobeDAO {
    @Insert
    long insert(WardrobeEntity item);

    @Query("SELECT * FROM wardrobe_items ORDER BY id DESC")
    List<WardrobeEntity> getAll();

    @Query("SELECT * FROM wardrobe_items WHERE category = :category ORDER BY id DESC")
    List<WardrobeEntity> getByCategory(String category);

    @Query("SELECT * FROM wardrobe_items WHERE category = :category ORDER BY RANDOM() LIMIT 1")
    WardrobeEntity getRandomByCategory(String category);

    @Query("SELECT COUNT(*) FROM wardrobe_items WHERE category = :category ")
    int countByCategory(String category);
}
