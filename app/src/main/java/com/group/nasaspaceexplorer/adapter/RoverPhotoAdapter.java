package com.group.nasaspaceexplorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.nasaspaceexplorer.R;
import com.group.nasaspaceexplorer.model.RoverPhoto;
import com.group.nasaspaceexplorer.util.ImageLoader;

import java.util.List;

public class RoverPhotoAdapter extends RecyclerView.Adapter<RoverPhotoAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(RoverPhoto photo);
    }

    private final List<RoverPhoto> photos;
    private final OnItemClickListener listener;

    public RoverPhotoAdapter(List<RoverPhoto> photos, OnItemClickListener listener) {
        this.photos = photos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rover_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoverPhoto photo = photos.get(position);

        holder.tvCamera.setText(photo.getCameraFullName());
        holder.tvDate.setText(photo.getEarthDate());
        holder.tvSol.setText("Sol " + photo.getSol());

        // Load thumbnail asynchronously (no third-party library)
        ImageLoader.load(photo.getImgSrc(), holder.imgThumbnail);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(photo));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // Clear tag so stale callbacks from ImageLoader are ignored
        holder.imgThumbnail.setTag(null);
        holder.imgThumbnail.setImageBitmap(null);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgThumbnail;
        final TextView tvCamera;
        final TextView tvDate;
        final TextView tvSol;

        ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            tvCamera = itemView.findViewById(R.id.tv_camera);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvSol = itemView.findViewById(R.id.tv_sol);
        }
    }
}
