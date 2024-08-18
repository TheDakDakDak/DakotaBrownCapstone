package com.dakbrown.weighttrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText userName;
    private EditText password;
    private Button loginSubmit;
    private Button createAccount;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        userName = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        loginSubmit = findViewById(R.id.loginSubmit);
        createAccount = findViewById(R.id.createAccount);
        dbHelper = new DatabaseHelper(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginSubmit.setOnClickListener(v -> {
            String user = userName.getText().toString();
            String pass = password.getText().toString();
            int userId = dbHelper.checkUser(user, pass);
            if (userId != -1) {
                Intent intent = new Intent(MainActivity.this, GridActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Invalid login", Toast.LENGTH_SHORT).show();
            }
        });

        createAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateAccount.class);
            startActivity(intent);
        });
    }
}