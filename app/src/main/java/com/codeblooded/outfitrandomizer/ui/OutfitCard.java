package com.codeblooded.outfitrandomizer.ui;

import androidx.annotation.Nullable;

public class OutfitCard {
    public final int id;
    public final String name;
    @Nullable
    public final String previewJacketUri, previewShirtUri, previewPantsUri, previewShoesUri;

    public OutfitCard(int id, String name, @Nullable String previewJacketUri, @Nullable String previewShirtUri, @Nullable String previewPantsUri, @Nullable String previewShoesUri) {
        this.id = id;
        this.name = name;
        this.previewJacketUri = previewJacketUri;
        this.previewShirtUri = previewShirtUri;
        this.previewPantsUri = previewPantsUri;
        this.previewShoesUri = previewShoesUri;
    }
}
