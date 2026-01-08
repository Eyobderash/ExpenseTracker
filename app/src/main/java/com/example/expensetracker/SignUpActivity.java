package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etUsername, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvLogin;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        // Sign Up button click
        btnSignUp.setOnClickListener(v -> {
            handleSignUp();
        });

        // Login text click
        tvLogin.setOnClickListener(v -> {
            goToLogin();
        });
    }

    private void handleSignUp() {
        // Get input values
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate input
        if (!validateInput(firstName, username, password, confirmPassword)) {
            return;
        }

        // Check if username is taken
        if (databaseHelper.isUsernameTaken(username)) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register user with first name and last name
        long result = databaseHelper.registerUser(firstName, lastName, username, password);

        if (result != -1) {
            // Registration successful
            String displayName = firstName + (!lastName.isEmpty() ? " " + lastName : "");
            Toast.makeText(this, "Welcome " + displayName + "!", Toast.LENGTH_SHORT).show();
            goToLogin();
        } else {
            // Registration failed
            Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String firstName, String username,
                                  String password, String confirmPassword) {
        // Check required fields
        if (firstName.isEmpty()) {
            Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please confirm password", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check password length
        if (password.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check password match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void goToLogin() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
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