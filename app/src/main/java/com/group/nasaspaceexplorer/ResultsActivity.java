package com.group.nasaspaceexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group.nasaspaceexplorer.adapter.RoverPhotoAdapter;
import com.group.nasaspaceexplorer.model.RoverPhoto;
import com.group.nasaspaceexplorer.network.NasaApiClient;

import java.util.List;

public class ResultsActivity extends AppCompatActivity {

    public static final String EXTRA_ROVER  = "extra_rover";
    public static final String EXTRA_CAMERA = "extra_camera";
    public static final String EXTRA_SOL    = "extra_sol";

    private LinearLayout layoutLoading;
    private TextView tvLoadingText;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private TextView tvError;
    private TextView tvResultCount;

    private final Handler animationHandler = new Handler(Looper.getMainLooper());
    private Runnable animationRunnable;
    private int dotCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        String rover  = getIntent().getStringExtra(EXTRA_ROVER);
        String camera = getIntent().getStringExtra(EXTRA_CAMERA);
        int sol       = getIntent().getIntExtra(EXTRA_SOL, 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(rover + " – Sol " + sol);
        }

        layoutLoading  = findViewById(R.id.layout_loading);
        tvLoadingText  = findViewById(R.id.tv_loading_text);
        recyclerView   = findViewById(R.id.recycler_view);
        tvEmpty        = findViewById(R.id.tv_empty);
        tvError        = findViewById(R.id.tv_error);
        tvResultCount  = findViewById(R.id.tv_result_count);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        fetchPhotos(rover, camera, sol);
    }

    private void fetchPhotos(String rover, String camera, int sol) {
        // This activity is no longer used; Perseverance latest photos
        // are now loaded directly in MarsRoverActivity.
        finish();
    }

    // ── Loading animation ─────────────────────────────────────────────────────

    private void startLoadingAnimation() {
        dotCount = 0;
        animationRunnable = new Runnable() {
            @Override
            public void run() {
                dotCount = (dotCount % 3) + 1;
                StringBuilder sb = new StringBuilder(getString(R.string.loading_prefix));
                for (int i = 0; i < dotCount; i++) sb.append('.');
                tvLoadingText.setText(sb.toString());
                animationHandler.postDelayed(this, 500);
            }
        };
        animationHandler.post(animationRunnable);
    }

    private void stopLoadingAnimation() {
        if (animationRunnable != null) {
            animationHandler.removeCallbacks(animationRunnable);
            animationRunnable = null;
        }
    }

    // ── UI state ──────────────────────────────────────────────────────────────

    private enum UiState { LOADING, RESULTS, EMPTY, ERROR }

    private void setUiState(UiState state) {
        layoutLoading.setVisibility(state == UiState.LOADING  ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility( state == UiState.RESULTS  ? View.VISIBLE : View.GONE);
        tvEmpty.setVisibility(      state == UiState.EMPTY    ? View.VISIBLE : View.GONE);
        tvError.setVisibility(      state == UiState.ERROR    ? View.VISIBLE : View.GONE);
        tvResultCount.setVisibility(state == UiState.RESULTS  ? View.VISIBLE : View.GONE);
        if (state == UiState.LOADING) startLoadingAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLoadingAnimation();
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
