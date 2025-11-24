package com.codeblooded.outfitrandomizer.ui;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codeblooded.outfitrandomizer.R;
import com.codeblooded.outfitrandomizer.data.local.WardrobeEntity;

import java.util.ArrayList;
import java.util.List;

public class WardrobeAdapter extends RecyclerView.Adapter<WardrobeAdapter.WardrobeViewHolder> {
    private Context context;
    private List<WardrobeEntity> wardrobeList;

    public WardrobeAdapter(Context context) {
        this.context = context;
        this.wardrobeList = new ArrayList<>();
    }

    public static class WardrobeViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, category;

        public WardrobeViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image_wardrobeCard);
            name = itemView.findViewById(R.id.name_wardrobeCard);
            category = itemView.findViewById(R.id.category_wardrobeCard);
        }
    }

    public void setItems(List<WardrobeEntity> items) {
        if (items == null) {
            this.wardrobeList = new ArrayList<>();
        } else {
            this.wardrobeList = items;
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WardrobeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wardrobe_card, parent, false);
        return new WardrobeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WardrobeViewHolder holder, int position) {
        WardrobeEntity item = wardrobeList.get(position);

        holder.name.setText(item.name);
        holder.category.setText(item.category);

        Glide.with(context).load(Uri.parse(item.imageUri)).centerCrop().into(holder.image);
    }

    @Override
    public int getItemCount() {
        return wardrobeList.size();
    }
}
