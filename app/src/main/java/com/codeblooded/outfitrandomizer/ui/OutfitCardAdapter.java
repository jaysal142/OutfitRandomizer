package com.codeblooded.outfitrandomizer.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codeblooded.outfitrandomizer.R;
import com.codeblooded.outfitrandomizer.data.local.OutfitEntity;

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
                public boolean areItemsTheSame(@NonNull OutfitEntity a, @NonNull OutfitEntity b) {
                    return a.id == b.id;
                }
                @Override
                public boolean areContentsTheSame(@NonNull OutfitEntity a, @NonNull OutfitEntity b) {
                    return a.name.equals(b.name)
                            && ((a.imageUri == null && b.imageUri == null)
                            || (a.imageUri != null && a.imageUri.equals(b.imageUri)))
                            && a.createdAt == b.createdAt;
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.outfit_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OutfitEntity item = getItem(position);
        holder.title.setText(item.name);

        // If you load images, plug your loader here (Glide/Picasso). Placeholder only:
        // Glide.with(holder.image.getContext()).load(item.imageUri).into(holder.image);

        holder.itemView.setOnClickListener(v -> onItemClick.onClick(item));
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.name_outfitCard);
            image = itemView.findViewById(R.id.image_outfitCard);
        }
    }
}
