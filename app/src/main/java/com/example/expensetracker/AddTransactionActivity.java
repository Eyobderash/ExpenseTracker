package com.example.expensetracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText etCategoryName, etDescription, etAmount;
    private RadioGroup rgCategoryType;
    private TextView tvDate;
    private Button btnSave;
    private DatabaseHelper databaseHelper;
    private int userId;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        // Get user ID from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        etCategoryName = findViewById(R.id.etCategoryName);
        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        rgCategoryType = findViewById(R.id.rgCategoryType);
        tvDate = findViewById(R.id.tvDate); // TextView showing date
        btnSave = findViewById(R.id.btnSave);

        // Set current date
        setCurrentDate();

        // Set default: Expense radio button selected
        RadioButton rbExpense = findViewById(R.id.rbExpense);
        rbExpense.setChecked(true);

        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                addTransactionToDatabase();
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private void setCurrentDate() {
        // Get current date in format: "Tuesday, Dec 9, 2025"
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvDate.setText(currentDate);
    }

    private boolean validateInput() {
        String categoryName = etCategoryName.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        int selectedId = rgCategoryType.getCheckedRadioButtonId();

        // Check if category name is empty
        if (categoryName.isEmpty()) {
            etCategoryName.setError("Please enter category name");
            etCategoryName.requestFocus();
            return false;
        }

        // Check if amount is empty
        if (amountStr.isEmpty()) {
            etAmount.setError("Please enter amount");
            etAmount.requestFocus();
            return false;
        }

        // Check if amount is valid number
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than 0");
                etAmount.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Please enter a valid number");
            etAmount.requestFocus();
            return false;
        }

        // Check if category type is selected
        if (selectedId == -1) {
            Toast.makeText(this, "Please select category type", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addTransactionToDatabase() {
        String categoryName = etCategoryName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();


        String categoryType = getSelectedCategoryType();


        double amount = Double.parseDouble(amountStr);

        // Get current date in database format (YYYY-MM-DD)
        String dbDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        try {

            int categoryId = getOrCreateCategory(categoryName, categoryType);


            long result = databaseHelper.addTransaction(userId, categoryId, categoryType,
                    description, amount, dbDate);

            if (result != -1) {
                Toast.makeText(this, "Transaction added successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Return to dashboard
            } else {
                Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String getSelectedCategoryType() {
        int selectedId = rgCategoryType.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        // Convert "EXPENSE" to "Expense" and "INCOME" to "Income"
        String radioText = selectedRadioButton.getText().toString();
        if (radioText.equals("EXPENSE")) {
            return "Expense";
        } else if (radioText.equals("INCOME")) {
            return "Income";
        } else {
            return radioText; // Fallback
        }
    }

    private int getOrCreateCategory(String categoryName, String categoryType) {

        Cursor cursor = databaseHelper.getAllCategories();

        int categoryId = -1;


        if (cursor != null && cursor.moveToFirst()) {
            do {
                String existingName = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
                String existingType = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));

                if (existingName.equalsIgnoreCase(categoryName) &&
                        existingType.equalsIgnoreCase(categoryType)) {
                    categoryId = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));
                    break;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }


        if (categoryId == -1) {
            long newCategoryId = databaseHelper.addCategory(categoryName, categoryType);

            if (newCategoryId == -1) {
                throw new RuntimeException("Failed to create category");
            }

            categoryId = (int) newCategoryId;
        }

        return categoryId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}