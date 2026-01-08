package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewSignUp;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views using your existing XML IDs
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignUp = findViewById(R.id.textViewSignUp);

        // Login button click
        buttonLogin.setOnClickListener(v -> {
            handleLogin();
        });

        // Sign up text click
        textViewSignUp.setOnClickListener(v -> {
            goToSignUp();
        });
    }

    private void handleLogin() {
        String username = editTextEmail.getText().toString().trim(); // Using email field for username
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check credentials in database
        if (databaseHelper.loginUser(username, password)) {
            // Login successful
            int userId = databaseHelper.getUserId(username);
            String fullName = databaseHelper.getUserFullName(username);

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("FULL_NAME", fullName);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToSignUp() {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connection
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}