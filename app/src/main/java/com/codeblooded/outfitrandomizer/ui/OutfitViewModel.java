package com.codeblooded.outfitrandomizer.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.codeblooded.outfitrandomizer.data.local.OutfitEntity;
import com.codeblooded.outfitrandomizer.data.local.OutfitRepository;

import java.util.List;

public class OutfitViewModel extends AndroidViewModel {
    private final OutfitRepository repo;
    private final LiveData<List<OutfitEntity>> outfits;

    public OutfitViewModel(@NonNull Application application) {
        super(application);
        repo = new OutfitRepository(application);
        outfits = repo.getOutfits();
    }

    public LiveData<List<OutfitEntity>> getOutfits() {
        return outfits;
    }

    public void addOutfit(String name, String imageUri) {
        repo.insert(new OutfitEntity(name, imageUri, System.currentTimeMillis()));
    }

    public void delete(OutfitEntity e) {
        repo.delete(e);
    }
}
