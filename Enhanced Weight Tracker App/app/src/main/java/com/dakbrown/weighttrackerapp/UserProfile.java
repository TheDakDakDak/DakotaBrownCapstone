package com.dakbrown.weighttrackerapp;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;

public class UserProfile extends AppCompatActivity {

    private Spinner spinnerSex;
    private EditText editTextName, editTextAge, editTextHeight;
    private Button updateProfileButton;

    private Button backToMainButton;
    private DatabaseHelper dbHelper;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        spinnerSex = findViewById(R.id.spinnerSex);
        editTextName = findViewById(R.id.updateName);
        editTextAge = findViewById(R.id.updateAge);
        editTextHeight = findViewById(R.id.updateHeight);
        updateProfileButton = findViewById(R.id.updateProfile);
        backToMainButton = findViewById(R.id.backToMain);
        dbHelper = new DatabaseHelper(this);

        ArrayAdapter<CharSequence> adapterSex = ArrayAdapter.createFromResource(this,
                R.array.sex_array, android.R.layout.simple_spinner_item);
        adapterSex.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(adapterSex);

        currentUserId = getIntent().getIntExtra("USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        loadUserProfile();

        backToMainButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, GridActivity.class);
            intent.putExtra("USER_ID", currentUserId);
            startActivity(intent);
        });

        updateProfileButton.setOnClickListener(v -> {
            String preferredName = editTextName.getText().toString();
            String ageStr = editTextAge.getText().toString();
            String heightStr = editTextHeight.getText().toString();
            String gender = spinnerSex.getSelectedItem().toString();


            if (!ageStr.isEmpty() && !heightStr.isEmpty()) {
                int age = Integer.parseInt(ageStr);
                int height = Integer.parseInt(heightStr);

                if (age >= 12 && age <= 100 && height >= 36 && height <= 100) {
                    dbHelper.updateUserProfile(currentUserId, preferredName, age, height, gender);
                    showUpdatedProfilePopup();
                } else {
                    Toast.makeText(this, "Please enter valid values for age and height", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter valid values for age and height", Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserProfile() {
        Cursor cursor = dbHelper.getUserProfile(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            String preferredName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PREFERRED_NAME));
            int age = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_AGE));
            int height = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_HEIGHT));
            String gender = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_GENDER));

            editTextName.setText(preferredName);
            editTextAge.setText(String.valueOf(age));
            editTextHeight.setText(String.valueOf(height));
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerSex.getAdapter();
            if (gender != null) {
                int spinnerPosition = adapter.getPosition(gender);
                spinnerSex.setSelection(spinnerPosition);
            }
            cursor.close();
        }
    }

    private void showUpdatedProfilePopup() {
        Cursor cursor = dbHelper.getUserProfile(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            String preferredName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PREFERRED_NAME));
            int age = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_AGE));
            int height = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_HEIGHT));
            String gender = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_GENDER));
            cursor.close();

            String message = "Preferred Name: " + preferredName +
                    "\nAge: " + age +
                    "\nHeight: " + height +
                    "\nGender: " + gender;

            new AlertDialog.Builder(this)
                    .setTitle("Profile Updated")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
}