package com.group.nasaspaceexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MarsRoverActivity extends AppCompatActivity {

    // Camera lists per rover
    private static final String[] CAMERAS_CURIOSITY = {
            "ALL", "FHAZ", "RHAZ", "MAST", "CHEMCAM", "MAHLI", "MARDI", "NAVCAM"
    };
    private static final String[] CAMERAS_OPPORTUNITY = {
            "ALL", "FHAZ", "RHAZ", "NAVCAM", "PANCAM", "MINITES"
    };
    private static final String[] CAMERAS_SPIRIT = {
            "ALL", "FHAZ", "RHAZ", "NAVCAM", "PANCAM", "MINITES"
    };

    // Approximate max sols per rover
    private static final int MAX_SOL_CURIOSITY   = 4000;
    private static final int MAX_SOL_OPPORTUNITY = 5111;
    private static final int MAX_SOL_SPIRIT       = 2208;

    private RadioGroup radioGroupRover;
    private RadioButton radioCuriosity, radioOpportunity, radioSpirit;
    private Spinner spinnerCamera;
    private EditText etSol;
    private TextView tvSolHint;
    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mars_rover);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.mars_title);
        }

        radioGroupRover = findViewById(R.id.radio_group_rover);
        radioCuriosity  = findViewById(R.id.radio_curiosity);
        radioOpportunity = findViewById(R.id.radio_opportunity);
        radioSpirit     = findViewById(R.id.radio_spirit);
        spinnerCamera   = findViewById(R.id.spinner_camera);
        etSol           = findViewById(R.id.et_sol);
        tvSolHint       = findViewById(R.id.tv_sol_hint);
        btnSearch       = findViewById(R.id.btn_search);

        // Default: Curiosity selected
        radioCuriosity.setChecked(true);
        updateCameraSpinner("Curiosity");
        updateSolHint("Curiosity");

        radioGroupRover.setOnCheckedChangeListener((group, checkedId) -> {
            String rover = getRoverName(checkedId);
            updateCameraSpinner(rover);
            updateSolHint(rover);
        });

        btnSearch.setOnClickListener(v -> attemptSearch());
    }

    private String getRoverName(int checkedId) {
        if (checkedId == R.id.radio_curiosity)   return "Curiosity";
        if (checkedId == R.id.radio_opportunity) return "Opportunity";
        if (checkedId == R.id.radio_spirit)      return "Spirit";
        return "Curiosity";
    }

    private void updateCameraSpinner(String rover) {
        String[] cameras;
        switch (rover) {
            case "Opportunity": cameras = CAMERAS_OPPORTUNITY; break;
            case "Spirit":      cameras = CAMERAS_SPIRIT;      break;
            default:            cameras = CAMERAS_CURIOSITY;   break;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, cameras);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCamera.setAdapter(adapter);
    }

    private void updateSolHint(String rover) {
        int max;
        switch (rover) {
            case "Opportunity": max = MAX_SOL_OPPORTUNITY; break;
            case "Spirit":      max = MAX_SOL_SPIRIT;      break;
            default:            max = MAX_SOL_CURIOSITY;   break;
        }
        tvSolHint.setText(getString(R.string.sol_hint, max));
    }

    private void attemptSearch() {
        String solStr = etSol.getText().toString().trim();
        if (solStr.isEmpty()) {
            Toast.makeText(this, R.string.error_sol_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        int sol;
        try {
            sol = Integer.parseInt(solStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_sol_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        if (sol < 0) {
            Toast.makeText(this, R.string.error_sol_negative, Toast.LENGTH_SHORT).show();
            return;
        }

        String rover  = getRoverName(radioGroupRover.getCheckedRadioButtonId());
        String camera = spinnerCamera.getSelectedItem().toString();

        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(ResultsActivity.EXTRA_ROVER,  rover);
        intent.putExtra(ResultsActivity.EXTRA_CAMERA, camera);
        intent.putExtra(ResultsActivity.EXTRA_SOL,    sol);
        startActivity(intent);
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
