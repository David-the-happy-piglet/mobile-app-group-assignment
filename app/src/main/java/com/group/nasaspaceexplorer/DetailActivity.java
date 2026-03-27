package com.group.nasaspaceexplorer;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.group.nasaspaceexplorer.model.RoverPhoto;
import com.group.nasaspaceexplorer.util.ImageLoader;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO = "extra_photo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        RoverPhoto photo = (RoverPhoto) getIntent().getSerializableExtra(EXTRA_PHOTO);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(photo != null ? photo.getRoverName() : "");
        }

        if (photo == null) {
            finish();
            return;
        }

        ImageView imgFull       = findViewById(R.id.img_full);
        ProgressBar progressImg = findViewById(R.id.progress_image);
        TextView tvSol          = findViewById(R.id.tv_sol);
        TextView tvEarthDate    = findViewById(R.id.tv_earth_date);
        TextView tvCameraFull   = findViewById(R.id.tv_camera_full);
        TextView tvCameraAbbr   = findViewById(R.id.tv_camera_abbr);
        TextView tvRoverName    = findViewById(R.id.tv_rover_name);
        TextView tvRoverStatus  = findViewById(R.id.tv_rover_status);
        TextView tvPhotoId      = findViewById(R.id.tv_photo_id);

        // Populate metadata
        tvSol.setText(getString(R.string.detail_sol, photo.getSol()));
        tvEarthDate.setText(getString(R.string.detail_earth_date, photo.getEarthDate()));
        tvCameraFull.setText(photo.getCameraFullName());
        tvCameraAbbr.setText(photo.getCameraName());
        tvRoverName.setText(photo.getRoverName());
        tvPhotoId.setText(getString(R.string.detail_photo_id, photo.getId()));

        // Color-coded rover status badge
        String status = photo.getRoverStatus();
        tvRoverStatus.setText(status.toUpperCase());
        if ("active".equalsIgnoreCase(status)) {
            tvRoverStatus.setBackgroundResource(R.drawable.badge_active);
        } else {
            tvRoverStatus.setBackgroundResource(R.drawable.badge_complete);
        }

        // Load image
        ImageLoader.load(photo.getImgSrc(), imgFull, success -> {
            progressImg.setVisibility(View.GONE);
            if (!success) {
                // Show placeholder prompt when loading fails
                imgFull.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
