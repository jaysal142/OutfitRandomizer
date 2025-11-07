package com.codeblooded.outfitrandomizer.ui;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacingDecoration extends RecyclerView.ItemDecoration {
    private final int spacePx;
    public SpacingDecoration(int spacePx) {
        this.spacePx = spacePx;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.set(spacePx, spacePx, spacePx, spacePx);
    }
}
