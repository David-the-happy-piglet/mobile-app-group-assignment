package com.group.nasaspaceexplorer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group.nasaspaceexplorer.adapter.EpicPhotoAdapter;
import com.group.nasaspaceexplorer.model.EpicPhoto;
import com.group.nasaspaceexplorer.network.NasaApiClient;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MarsRoverActivity extends AppCompatActivity {

    private Button btnSelectDate;
    private Button btnSearch;

    private int selectedYear, selectedMonth, selectedDay;

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
        setContentView(R.layout.activity_mars_rover);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("EPIC Earth Photos");
        }

        btnSelectDate = findViewById(R.id.btn_select_date);
        btnSearch     = findViewById(R.id.btn_search);
        layoutLoading = findViewById(R.id.layout_loading);
        tvLoadingText = findViewById(R.id.tv_loading_text);
        recyclerView  = findViewById(R.id.recycler_view);
        tvEmpty       = findViewById(R.id.tv_empty);
        tvError       = findViewById(R.id.tv_error);
        tvResultCount = findViewById(R.id.tv_result_count);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 默认日期：今天
        Calendar today = Calendar.getInstance();
        selectedYear  = today.get(Calendar.YEAR);
        selectedMonth = today.get(Calendar.MONTH);
        selectedDay   = today.get(Calendar.DAY_OF_MONTH);
        updateDateButton();

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSearch.setOnClickListener(v -> fetchPhotos());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedYear  = year;
                    selectedMonth = month;
                    selectedDay   = day;
                    updateDateButton();
                },
                selectedYear, selectedMonth, selectedDay);

        // EPIC 数据从 2015-09-01 开始
        Calendar minDate = Calendar.getInstance();
        minDate.set(2015, Calendar.SEPTEMBER, 1);
        dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void updateDateButton() {
        String label = String.format(Locale.US, "%d-%02d-%02d",
                selectedYear, selectedMonth + 1, selectedDay);
        btnSelectDate.setText(label);
    }

    private void fetchPhotos() {
        String date = String.format(Locale.US, "%d-%02d-%02d",
                selectedYear, selectedMonth + 1, selectedDay);

        setUiState(UiState.LOADING);

        NasaApiClient.fetchEpicPhotos(date, new NasaApiClient.ApiCallback<List<EpicPhoto>>() {
            @Override
            public void onSuccess(List<EpicPhoto> photos) {
                stopLoadingAnimation();
                if (photos.isEmpty()) {
                    setUiState(UiState.EMPTY);
                } else {
                    setUiState(UiState.RESULTS);
                    tvResultCount.setText(getString(R.string.result_count, photos.size()));
                    EpicPhotoAdapter adapter = new EpicPhotoAdapter(photos, photo -> {
                        Intent intent = new Intent(MarsRoverActivity.this, DetailActivity.class);
                        intent.putExtra("extra_epic_photo", photo);
                        startActivity(intent);
                    });
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String errorMessage) {
                stopLoadingAnimation();
                setUiState(UiState.ERROR);
                tvError.setText(errorMessage);
            }
        });
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

    private enum UiState { IDLE, LOADING, RESULTS, EMPTY, ERROR }

    private void setUiState(UiState state) {
        layoutLoading.setVisibility(state == UiState.LOADING  ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility( state == UiState.RESULTS  ? View.VISIBLE : View.GONE);
        tvEmpty.setVisibility(      state == UiState.EMPTY    ? View.VISIBLE : View.GONE);
        tvError.setVisibility(      state == UiState.ERROR    ? View.VISIBLE : View.GONE);
        tvResultCount.setVisibility(state == UiState.RESULTS  ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(state != UiState.LOADING);
        btnSelectDate.setEnabled(state != UiState.LOADING);
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