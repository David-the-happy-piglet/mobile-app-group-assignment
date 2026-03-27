package com.group.nasaspaceexplorer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.nasaspaceexplorer.R;
import com.group.nasaspaceexplorer.model.EpicPhoto;
import com.group.nasaspaceexplorer.util.ImageLoader;

import java.util.List;

public class EpicPhotoAdapter extends RecyclerView.Adapter<EpicPhotoAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(EpicPhoto photo);
    }

    private final List<EpicPhoto> photos;
    private final OnItemClickListener listener;

    public EpicPhotoAdapter(List<EpicPhoto> photos, OnItemClickListener listener) {
        this.photos   = photos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_epic_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EpicPhoto photo = photos.get(position);
        holder.tvTime.setText(photo.getTimeOnly());
        holder.tvCoords.setText(String.format("%.1f°, %.1f°", photo.getLat(), photo.getLon()));
        ImageLoader.load(photo.getImageUrl(), holder.imgThumbnail);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(photo));
    }

    @Override
    public int getItemCount() { return photos.size(); }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.imgThumbnail.setTag(null);
        holder.imgThumbnail.setImageBitmap(null);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgThumbnail;
        final TextView tvTime;
        final TextView tvCoords;

        ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            tvTime       = itemView.findViewById(R.id.tv_time);
            tvCoords     = itemView.findViewById(R.id.tv_coords);
        }
    }
}