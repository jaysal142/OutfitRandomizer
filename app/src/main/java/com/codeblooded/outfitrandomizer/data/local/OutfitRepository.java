package com.codeblooded.outfitrandomizer.data.local;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OutfitRepository {
    private final OutfitDao dao;
    private final LiveData<List<OutfitEntity>> outfits;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public OutfitRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        dao = db.outfitDao();
        outfits = dao.getAllOutfits();
    }

    public LiveData<List<OutfitEntity>> getOutfits() {
        return outfits;
    }

    public void insert(OutfitEntity e) {
        io.submit(() -> dao.insert(e));
    }
    public void update(OutfitEntity e) {
        io.submit(() -> dao.update(e));
    }
    public void delete(OutfitEntity e) {
        io.submit(() -> dao.delete(e));
    }
}
