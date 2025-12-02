package com.codeblooded.outfitrandomizer.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface OutfitDao {
    @Query("SELECT * FROM outfits ORDER BY createdAt DESC")
    LiveData<List<OutfitEntity>> getAllOutfits();

    @Query("SELECT * FROM outfits WHERE isFavorite = 1 ORDER BY createdAt DESC")
    LiveData<List<OutfitEntity>> getFavoriteOutfits();

    @Query("SELECT COUNT(*) FROM outfits")
    int countAll();
    @Query("SELECT COUNT(*) FROM outfits WHERE isFavorite = 1")
    int countFavorites();

    @Insert
    long insert(OutfitEntity outfit);

    @Upsert
    void upsert(OutfitEntity outfit);

    @Update
    int update(OutfitEntity outfit);

    @Delete
    int delete(OutfitEntity outfit);

    @Query("DELETE FROM outfits")
    void clearAll();
}
