package com.dakbrown.weighttrackerapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.View;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.util.Log;

public class GridActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private int currentUserId;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grid);
        currentUserId = getIntent().getIntExtra("USER_ID", -1);
        dbHelper = new DatabaseHelper(this);
        Button addWeightButton = findViewById(R.id.addWeight);
        Button updateGoalWeightButton = findViewById(R.id.goalWeight);
        Button toProfileButton = findViewById(R.id.toProfile);
        Button weightLossCalculatorButton = findViewById(R.id.weightLossCalc);
        Button deleteAllWeightsButton = findViewById(R.id.deleteAllWeights);
        tableLayout = findViewById(R.id.dataTable);
        loadWeightEntries();
        addWeightButton.setOnClickListener(v -> openAddWeightDialog());
        updateGoalWeightButton.setOnClickListener(v -> openUpdateGoalDialog());
        weightLossCalculatorButton.setOnClickListener(v -> {
            Intent intent = new Intent(GridActivity.this, WeightLossCalculator.class);
            intent.putExtra("USER_ID", currentUserId);
            startActivity(intent);
        });
        toProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(GridActivity.this, UserProfile.class);
            intent.putExtra("USER_ID", currentUserId);
            startActivity(intent);
        });
        deleteAllWeightsButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete All Weights")
                    .setMessage("This will permanently delete all weight entries. Continue?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbHelper.deleteAllWeightEntries(currentUserId);
                        loadWeightEntries(); // Refresh the table after deletion
                        Toast.makeText(GridActivity.this, "All weight entries deleted.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
    //Opens the dialog box for a user to add a weight entry.
    private void openAddWeightDialog() {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        new AlertDialog.Builder(this)
                .setTitle("Add Weight")
                .setMessage("Enter your weight:")
                .setView(input)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String weightStr = input.getText().toString();
                    if (!weightStr.isEmpty()) {
                        int weight = Integer.parseInt(weightStr);
                        addWeightEntryToDatabase(getCurrentDate(), weight);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }
    //Opens the dialog box for a user to change their goal weight.
    private void openUpdateGoalDialog() {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        new AlertDialog.Builder(this)
                .setTitle("Update Goal")
                .setMessage("Enter your goal weight:")
                .setView(input)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String goalWeightStr = input.getText().toString();
                    if (!goalWeightStr.isEmpty()) {
                        int goalWeight = Integer.parseInt(goalWeightStr);
                        dbHelper.updateGoalWeight(currentUserId, goalWeight);
                        Toast.makeText(this, "Goal weight updated successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }
    //Called by openAddWeightDialog to get their weight and insert it into the database.
    private void addWeightEntryToDatabase(String date, int weight) {
        dbHelper.addWeightEntry(currentUserId, date, weight);
        checkAndNotifyGoalWeight(currentUserId, weight);
        loadWeightEntries();
        Toast.makeText(this, "Weight added successfully!", Toast.LENGTH_SHORT).show();
    }
    //Loads the list of weights.
    private void loadWeightEntries() {
        Cursor cursor = dbHelper.getWeightsByUserId(currentUserId);
        tableLayout.removeAllViews();
        Integer previousWeight = null;
        int goalWeight = dbHelper.getGoalWeightByUserId(currentUserId);

        while (cursor.moveToNext()) {
            String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
            int weight = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_WEIGHT));
            addRowToTable(date, weight, previousWeight, goalWeight);
            previousWeight = weight;
        }
        cursor.close();
    }
    //Adds a new row to the list of weights when a user adds a weight.
    private void addRowToTable(String date, int weight, Integer previousWeight, int goalWeight) {
        final TableRow row = new TableRow(this);
        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        TextView dateView = new TextView(this);
        dateView.setText(date);
        dateView.setTextSize(14);
        dateView.setPadding(16, 16, 16, 16);
        TableRow.LayoutParams paramsDate = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        dateView.setLayoutParams(paramsDate);

        // Replace EditText with TextView for weight
        TextView weightView = new TextView(this);
        weightView.setText(String.valueOf(weight));
        weightView.setTextSize(20);
        weightView.setPadding(16, 16, 16, 16);
        TableRow.LayoutParams paramsWeight = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        weightView.setLayoutParams(paramsWeight);

        TextView changeView = new TextView(this);
        if (previousWeight != null) {
            int change = weight - previousWeight;
            String changeText;
            if (change > 0) {
                changeText = "+" + change;
            } else {
                changeText = String.valueOf(change);
            }
            changeView.setText(changeText);
        } else {
            changeView.setText("-");
        }
        changeView.setTextSize(20);
        changeView.setPadding(16, 16, 16, 16);
        changeView.setGravity(Gravity.CENTER);
        TableRow.LayoutParams paramsChange = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        changeView.setLayoutParams(paramsChange);

        TextView goalView = new TextView(this);
        if (goalWeight > 0) {
            int poundsToGoal = goalWeight - weight;
            goalView.setText(String.valueOf(poundsToGoal));
        } else {
            goalView.setText("-");
        }
        goalView.setTextSize(20);
        goalView.setPadding(16, 16, 16, 16);
        goalView.setGravity(Gravity.CENTER);
        TableRow.LayoutParams paramsGoal = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        goalView.setLayoutParams(paramsGoal);

        row.addView(dateView);
        row.addView(weightView);
        row.addView(changeView);
        row.addView(goalView);

        tableLayout.addView(row);
    }
    //Used to show the weight in the weight list.
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
    //Called by addWeightEntryToDatabase if the goal weight is entered as current weight.
    private void checkAndNotifyGoalWeight(int userId, int newWeight) {
        int goalWeight = dbHelper.getGoalWeightByUserId(userId);
        if (newWeight == goalWeight) {
            showGoalReachedDialog();
        }
    }
    //Called by checkAndNotifyGoalWeight to display the congratulatory message.
    private void showGoalReachedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Goal Reached!")
                .setMessage("Congratulations! You've reached your goal weight!")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Any additional actions can be handled here
                })
                .show();
    }


}