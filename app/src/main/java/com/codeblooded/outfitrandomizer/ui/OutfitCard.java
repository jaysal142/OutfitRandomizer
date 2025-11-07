package com.codeblooded.outfitrandomizer.ui;

import androidx.annotation.Nullable;

public class OutfitCard {
    public final int id;
    public final String name;
    @Nullable
    public final String previewImageUri;

    public OutfitCard(int id, String name, @Nullable String previewImageUri) {
        this.id = id;
        this.name = name;
        this.previewImageUri = previewImageUri;
    }
}
