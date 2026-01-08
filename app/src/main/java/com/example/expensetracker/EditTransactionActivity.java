package com.example.expensetracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditTransactionActivity extends AppCompatActivity {

    private EditText etCategoryName, etDescription, etAmount;
    private RadioGroup rgCategoryType;
    private Button btnSave;
    private DatabaseHelper databaseHelper;
    private int userId;
    private int transactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);


        Intent intent = getIntent();
        userId = intent.getIntExtra("USER_ID", -1);
        transactionId = intent.getIntExtra("TRANSACTION_ID", -1);

        if (userId == -1 || transactionId == -1) {
            Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show();
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
        btnSave = findViewById(R.id.btnSave);
        btnSave.setText("UPDATE"); // Change button text

        // Load transaction data
        loadTransactionData();

        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                updateTransaction();
            }
        });
    }

    private void loadTransactionData() {

        Cursor cursor = databaseHelper.getTransactionById(transactionId);

        if (cursor != null && cursor.moveToFirst()) {
            String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
            String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));

            // Populate fields
            etCategoryName.setText(categoryName);
            etDescription.setText(description);
            etAmount.setText(String.valueOf(amount));

            // Select radio button
            RadioButton rbExpense = findViewById(R.id.rbExpense);
            RadioButton rbIncome = findViewById(R.id.rbIncome);
            if (type.equals("Expense")) {
                rbExpense.setChecked(true);
            } else {
                rbIncome.setChecked(true);
            }
        } else {
            Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (cursor != null) {
            cursor.close();
        }
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

    private void updateTransaction() {
        String categoryName = etCategoryName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String categoryType = getSelectedCategoryType();
        double amount = Double.parseDouble(amountStr);

        // Get category ID
        int categoryId = getOrCreateCategory(categoryName, categoryType);

        // Update transaction
        int rowsUpdated = databaseHelper.updateTransaction(transactionId, categoryId,
                categoryType, description, amount, getCurrentDate());

        if (rowsUpdated > 0) {
            Toast.makeText(this, "Transaction updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
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

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private int getOrCreateCategory(String categoryName, String categoryType) {
        // First, check if category already exists
        Cursor cursor = databaseHelper.getAllCategories();

        int categoryId = -1;

        // Check each category to find match
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

        // If category not found, create it
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