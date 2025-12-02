package com.codeblooded.outfitrandomizer.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WardrobeDAO {
    @Update
    void update(WardrobeEntity item);
    @Insert
    long insert(WardrobeEntity item);
    @Delete
    void delete(WardrobeEntity item);
    @Query("DELETE FROM wardrobe_items")
    void clearAll();

    @Query("SELECT * FROM wardrobe_items ORDER BY id DESC")
    List<WardrobeEntity> getAll();

    @Query("SELECT * FROM wardrobe_items WHERE category = :category ORDER BY id DESC")
    List<WardrobeEntity> getByCategory(String category);

    @Query("SELECT * FROM wardrobe_items WHERE category = :category ORDER BY RANDOM() LIMIT 1")
    WardrobeEntity getRandomByCategory(String category);
}
