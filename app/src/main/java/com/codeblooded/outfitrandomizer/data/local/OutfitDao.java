package com.codeblooded.outfitrandomizer.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface OutfitDao {
    @Query("SELECT * FROM outfits ORDER BY createdAt DESC")
    LiveData<List<OutfitEntity>> getAllOutfits();

    @Insert
    long insert(OutfitEntity outfit);

    @Update
    int update(OutfitEntity outfit);

    @Delete
    int delete(OutfitEntity outfit);

    @Query("DELETE FROM outfits")
    void clearAll();
}
