package com.group.nasaspaceexplorer;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.group.nasaspaceexplorer.model.ApodData;
import com.group.nasaspaceexplorer.network.NasaApiClient;
import com.group.nasaspaceexplorer.util.ImageLoader;

import java.util.Calendar;
import java.util.Locale;

public class ApodActivity extends AppCompatActivity {

    private Button btnSelectDate;
    private Button btnFetch;
    private LinearLayout layoutLoading;
    private TextView tvLoadingText;
    private LinearLayout layoutResult;
    private ImageView imgApod;
    private TextView tvApodTitle;
    private TextView tvApodDate;
    private TextView tvApodCopyright;
    private TextView tvApodExplanation;
    private LinearLayout layoutVideoNotice;
    private TextView tvVideoUrl;
    private TextView tvError;

    private int selectedYear, selectedMonth, selectedDay;
    private final Handler animationHandler = new Handler(Looper.getMainLooper());
    private Runnable animationRunnable;
    private int dotCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apod);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.apod_title);
        }

        btnSelectDate = findViewById(R.id.btn_select_date);
        btnFetch = findViewById(R.id.btn_fetch_apod);
        layoutLoading = findViewById(R.id.layout_loading);
        tvLoadingText = findViewById(R.id.tv_loading_text);
        layoutResult = findViewById(R.id.layout_result);
        imgApod = findViewById(R.id.img_apod);
        tvApodTitle = findViewById(R.id.tv_apod_title);
        tvApodDate = findViewById(R.id.tv_apod_date);
        tvApodCopyright = findViewById(R.id.tv_apod_copyright);
        tvApodExplanation = findViewById(R.id.tv_apod_explanation);
        layoutVideoNotice = findViewById(R.id.layout_video_notice);
        tvVideoUrl = findViewById(R.id.tv_video_url);
        tvError = findViewById(R.id.tv_error);

        // Default to today
        Calendar today = Calendar.getInstance();
        selectedYear = today.get(Calendar.YEAR);
        selectedMonth = today.get(Calendar.MONTH);
        selectedDay = today.get(Calendar.DAY_OF_MONTH);
        updateDateButton();

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnFetch.setOnClickListener(v -> fetchApod());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedYear = year;
                    selectedMonth = month;
                    selectedDay = day;
                    updateDateButton();
                },
                selectedYear, selectedMonth, selectedDay);

        // APOD started on June 16, 1995
        Calendar minDate = Calendar.getInstance();
        minDate.set(1995, Calendar.JUNE, 16);
        dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        // Can't pick future dates
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void updateDateButton() {
        String label = String.format(Locale.US, "%d-%02d-%02d",
                selectedYear, selectedMonth + 1, selectedDay);
        btnSelectDate.setText(label);
    }

    private void fetchApod() {
        String date = String.format(Locale.US, "%d-%02d-%02d",
                selectedYear, selectedMonth + 1, selectedDay);

        setUiState(UiState.LOADING);

        NasaApiClient.fetchApod(date, new NasaApiClient.ApiCallback<ApodData>() {
            @Override
            public void onSuccess(ApodData result) {
                stopLoadingAnimation();
                displayApod(result);
            }

            @Override
            public void onError(String errorMessage) {
                stopLoadingAnimation();
                setUiState(UiState.ERROR);
                tvError.setText(errorMessage);
            }
        });
    }

    private void displayApod(ApodData apod) {
        setUiState(UiState.RESULT);

        tvApodTitle.setText(apod.getTitle());
        tvApodDate.setText(apod.getDate());

        String copyright = apod.getCopyright();
        if (copyright != null && !copyright.isEmpty()) {
            tvApodCopyright.setVisibility(View.VISIBLE);
            tvApodCopyright.setText(getString(R.string.copyright_prefix, copyright));
        } else {
            tvApodCopyright.setVisibility(View.GONE);
        }

        tvApodExplanation.setText(apod.getExplanation());

        if (apod.isImage()) {
            imgApod.setVisibility(View.VISIBLE);
            layoutVideoNotice.setVisibility(View.GONE);
            // Prefer HD URL if available
            String imageUrl = (apod.getHdUrl() != null && !apod.getHdUrl().isEmpty())
                    ? apod.getHdUrl() : apod.getUrl();
            ImageLoader.load(imageUrl, imgApod);
        } else {
            // It's a video (usually YouTube) – show notice and URL
            imgApod.setVisibility(View.GONE);
            layoutVideoNotice.setVisibility(View.VISIBLE);
            tvVideoUrl.setText(apod.getUrl());
        }
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

    private enum UiState { IDLE, LOADING, RESULT, ERROR }

    private void setUiState(UiState state) {
        layoutLoading.setVisibility(state == UiState.LOADING ? View.VISIBLE : View.GONE);
        layoutResult.setVisibility(state == UiState.RESULT ? View.VISIBLE : View.GONE);
        tvError.setVisibility(state == UiState.ERROR ? View.VISIBLE : View.GONE);
        btnFetch.setEnabled(state != UiState.LOADING);
        btnSelectDate.setEnabled(state != UiState.LOADING);

        if (state == UiState.LOADING) {
            startLoadingAnimation();
        }
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
