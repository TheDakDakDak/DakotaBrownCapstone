package com.dakbrown.weighttrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import android.app.AlertDialog;
import android.widget.Toast;
import android.database.Cursor;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WeightLossCalculator extends AppCompatActivity {

    private EditText editTextWeight;
    private EditText editTextHeight;
    private EditText editTextAge;
    private Spinner spinnerSex;
    private Spinner spinnerActivityLevel;
    private Button calcSubmit;
    private TextView tdeeTextView;
    private TextView maxCaloriesTextView;
    private EditText editTextGoal;
    private EditText editTextWeeks;
    private Button backButton;
    private DatabaseHelper dbHelper;
    private int currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wlc);

        //Link to widgets in xml file
        editTextWeight = findViewById(R.id.calcWeight);
        editTextHeight = findViewById(R.id.calcHeight);
        editTextAge = findViewById(R.id.calcAge);
        spinnerSex = findViewById(R.id.calcSex);
        spinnerActivityLevel = findViewById(R.id.calcActivityLevel);
        calcSubmit = findViewById(R.id.calcSubmit);
        tdeeTextView = findViewById(R.id.resultTextView);
        maxCaloriesTextView = findViewById(R.id.resultTextView2);
        editTextGoal = findViewById(R.id.calcGoalWeight);
        editTextWeeks = findViewById(R.id.calcWeeks);
        backButton = findViewById(R.id.backToMain);
        dbHelper = new DatabaseHelper(this);

        currentUserId = getIntent().getIntExtra("USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        prefillUserProfileData();

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(WeightLossCalculator.this, GridActivity.class);
            intent.putExtra("USER_ID", currentUserId);
            startActivity(intent);
        });

        ArrayAdapter<CharSequence> adapterSex = ArrayAdapter.createFromResource(this,
                R.array.sex_array, android.R.layout.simple_spinner_item);
        adapterSex.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(adapterSex);

        ArrayAdapter<CharSequence> adapterActivityLevel = ArrayAdapter.createFromResource(this,
                R.array.activity_level_array, android.R.layout.simple_spinner_item);
        adapterActivityLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivityLevel.setAdapter(adapterActivityLevel);

        calcSubmit.setOnClickListener(v -> {
            //Get values from form
            double cWeightPounds = getDoubleFromEditText(editTextWeight); //Needed for calculation of caloriesToBurnPerDay
            double cWeight = cWeightPounds;
            double cHeight = getDoubleFromEditText(editTextHeight);
            int cAge = getIntFromEditText(editTextAge);
            String captureSex = spinnerSex.getSelectedItem().toString();
            String captureActivityLevel = spinnerActivityLevel.getSelectedItem().toString();
            int cGoalWeight = getIntFromEditText(editTextGoal);
            int cWeeks = getIntFromEditText(editTextWeeks);

            cWeight = cWeight/2.2046; //Convert pounds to kilograms
            cHeight = cHeight * 2.54; //Convert inches to centimeters


            int cSex; //Non-string sex value that will be used in the calculator
            double cActivityLevel = 1; //Non-string activity value that will be used in the calculator

            //Convert sex string to numerical value for use in calculator
            if (captureSex.equals("Male"))
                cSex = 5;
            else
                cSex = -161;

            //Convert activity string to numerical value for use in calculator
            switch (captureActivityLevel)
            {
                case "Sedentary (little or no exercise)":
                    cActivityLevel = 1.2;
                    break;
                case "Lightly active (light exercise 1-3 days a week)":
                    cActivityLevel = 1.375;
                    break;
                case "Moderately active (moderate exercise 3-5 days a week)":
                    cActivityLevel = 1.55;
                    break;
                case "Very active (hard exercise 6-7 days a week)":
                    cActivityLevel = 1.725;
                    break;
                case "Super active (very hard exercise 2x/day)":
                    cActivityLevel = 1.9;
                    break;
            }

            //Calculating TDEE and max number of calories user can eat per day to reach goal
            double tdee = ((10*cWeight) + (6.25*cHeight) - (5*cAge) + cSex) * cActivityLevel;
            double caloriesToBurnPerDay = (((cWeightPounds - cGoalWeight) * 3500)/cWeeks)/7;
            double maxCaloriesPerDay = tdee - caloriesToBurnPerDay;

            //Display warning if user is attempting to lose weight at an unhealthy rate
            if(1200 > maxCaloriesPerDay)
                calorieWarning("It is never recommended to eat less than 1200 calories per day. You may want to increase your number of weeks to goal.");

            //Output formatting to round to nearest integer
            String formattedTdee = String.format("%.0f", tdee);
            String formattedMaxCaloriesPerDay = String.format("%.0f", maxCaloriesPerDay);

            //Output
            tdeeTextView.setText("Total Daily Energy Expenditure: " + formattedTdee);
            if(maxCaloriesPerDay < 0) {
                maxCaloriesTextView.setText("It is not possible to lose the weight that quickly");}
            else {
            maxCaloriesTextView.setText("You should eat at max, " + formattedMaxCaloriesPerDay +
                    " calories per day to reach " + cGoalWeight + " pounds in " + cWeeks + " weeks.");}
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private double getDoubleFromEditText(EditText editText) {
        return Double.parseDouble(editText.getText().toString().trim());
    }

    private int getIntFromEditText(EditText editText) throws NumberFormatException {
        return Integer.parseInt(editText.getText().toString().trim());
    }

    private void calorieWarning(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("I acknowledge", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void prefillUserProfileData() {
        Cursor cursor = dbHelper.getUserProfile(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            int heightIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_HEIGHT);
            int ageIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_AGE);

            Log.d("WeightLossCalculator", "Cursor count: " + cursor.getCount());

            if (!cursor.isNull(heightIndex)) {
                int height = cursor.getInt(heightIndex);
                Log.d("WeightLossCalculator", "Height: " + height);
                editTextHeight.setText(String.valueOf(height));
            }

            if (!cursor.isNull(ageIndex)) {
                int age = cursor.getInt(ageIndex);
                Log.d("WeightLossCalculator", "Age: " + age);
                editTextAge.setText(String.valueOf(age));
            }

            cursor.close();
        } else {
            Log.d("WeightLossCalculator", "Cursor is null or empty");
        }
        int mostRecentWeight = dbHelper.getMostRecentWeight(currentUserId);
        if (mostRecentWeight != -1) {
            Log.d("WeightLossCalculator", "Most Recent Weight: " + mostRecentWeight);
            editTextWeight.setText(String.valueOf(mostRecentWeight));
        } else {
            Log.d("WeightLossCalculator", "No weight found for user");
        }
    }
}