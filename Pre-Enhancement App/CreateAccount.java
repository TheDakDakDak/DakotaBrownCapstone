package com.dakbrown.weighttrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccount extends AppCompatActivity {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private Button signUpButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        userNameEditText = findViewById(R.id.userName);
        passwordEditText = findViewById(R.id.password);
        signUpButton = findViewById(R.id.loginSubmit);

        dbHelper = new DatabaseHelper(this);

        signUpButton.setOnClickListener(v -> {
            String username = userNameEditText.getText().toString();
            String password = passwordEditText.getText().toString();


            long userId = dbHelper.addUser(username, password);

            if (userId == -1) {
                Toast.makeText(CreateAccount.this, "Signup failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CreateAccount.this, "Signup successful", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CreateAccount.this, GridActivity.class);
                intent.putExtra("USER_ID", (int) userId);
                startActivity(intent);
            }

            userNameEditText.setText("");
            passwordEditText.setText("");
        });
    }
}