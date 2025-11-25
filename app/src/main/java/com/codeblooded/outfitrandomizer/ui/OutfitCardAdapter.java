package com.codeblooded.outfitrandomizer.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codeblooded.outfitrandomizer.R;
import com.codeblooded.outfitrandomizer.data.local.OutfitEntity;

import java.io.IOException;

public class OutfitCardAdapter extends ListAdapter<OutfitEntity, OutfitCardAdapter.VH> {

    public interface OnItemClick {
        void onClick(OutfitEntity item);
    }

    private final OnItemClick onItemClick;

    public OutfitCardAdapter(OnItemClick onItemClick) {
        super(DIFF);
        this.onItemClick = onItemClick;
    }

    private static final DiffUtil.ItemCallback<OutfitEntity> DIFF =
            new DiffUtil.ItemCallback<OutfitEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull OutfitEntity oldItem, @NonNull OutfitEntity newItem) {
                    return oldItem.id == newItem.id;
                }
                private boolean safeEquals(String a, String b) {
                    if (a == null && b == null) return true;
                    if (a == null || b == null) return false;
                    return a.equals(b);
                }
                @Override
                public boolean areContentsTheSame(@NonNull OutfitEntity oldItem, @NonNull OutfitEntity newItem) {
                    return oldItem.name.equals(newItem.name)
                            && safeEquals(oldItem.jacketImageUri, newItem.jacketImageUri)
                            && safeEquals(oldItem.shirtImageUri, newItem.shirtImageUri)
                            && safeEquals(oldItem.pantsImageUri, newItem.pantsImageUri)
                            && safeEquals(oldItem.shoesImageUri, newItem.shoesImageUri);
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.outfit_card, parent, false);
        return new VH(v);
    }

    private void bindImage(ImageView view, String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            view.setImageDrawable(null);
            view.setVisibility(View.GONE);
            return;
        }

        view.setVisibility(View.VISIBLE);
        Glide.with(view.getContext()).load(Uri.parse(uriString)).fitCenter().into(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OutfitEntity item = getItem(position);
        if (item == null) return;

        // Title
        holder.title.setText(item.name != null ? item.name : "");

        // Load each clothing image
        bindImage(holder.jacketImage, item.jacketImageUri);
        bindImage(holder.shirtImage, item.shirtImageUri);
        bindImage(holder.pantsImage, item.pantsImageUri);
        bindImage(holder.shoesImage, item.shoesImageUri);

        // Click callback for the whole card
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.onClick(item);
            }
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        ImageView jacketImage, shirtImage, pantsImage, shoesImage;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.name_outfitCard);
            jacketImage = itemView.findViewById(R.id.jacket_outfitCard);
            shirtImage = itemView.findViewById(R.id.shirt_outfitCard);
            pantsImage = itemView.findViewById(R.id.pants_outfitCard);
            shoesImage = itemView.findViewById(R.id.shoes_outfitCard);
        }
    }
}
