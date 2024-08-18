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
    }

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

    private void addWeightEntryToDatabase(String date, int weight) {
        dbHelper.addWeightEntry(currentUserId, date, weight);
        checkAndNotifyGoalWeight(currentUserId, weight);
        loadWeightEntries();
        Toast.makeText(this, "Weight added successfully!", Toast.LENGTH_SHORT).show();
    }

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

    private void addRowToTable(String date, int weight, Integer previousWeight, int goalWeight) {
        final TableRow row = new TableRow(this);
        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        TextView dateView = new TextView(this);
        dateView.setText(date);
        dateView.setTextSize(14);
        dateView.setPadding(16,16,16,16);
        TableRow.LayoutParams paramsDate = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        dateView.setLayoutParams(paramsDate);

        final EditText weightEdit = new EditText(this);
        weightEdit.setId(View.generateViewId());
        weightEdit.setText(String.valueOf(weight));
        weightEdit.setTextSize(20);
        weightEdit.setPadding(16,16,16,16);
        TableRow.LayoutParams paramsWeight = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        weightEdit.setLayoutParams(paramsWeight);
        weightEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        weightEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                int newWeight = Integer.parseInt(weightEdit.getText().toString());
                dbHelper.updateWeightInDatabase(currentUserId, date, newWeight);
                loadWeightEntries();
                return true;
            }
            return false;
        });

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
        row.addView(weightEdit);
        row.addView(changeView);
        row.addView(goalView);

        tableLayout.addView(row);
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void checkAndNotifyGoalWeight(int userId, int newWeight) {
        int goalWeight = dbHelper.getGoalWeightByUserId(userId);
        if (newWeight == goalWeight) {
            sendSmsNotification();
        }
    }

    private void sendSmsNotification() {
        String phoneNumber = "5555555555";
        String message = "Congratulations! You've reached your goal weight!";

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }


}