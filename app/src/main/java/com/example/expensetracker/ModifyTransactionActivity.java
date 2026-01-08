package com.example.expensetracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModifyTransactionActivity extends AppCompatActivity {

    private Button btnDelete, btnEdit, btnSave;
    private TableLayout tableTransactions;
    private DatabaseHelper databaseHelper;
    private int userId;
    private int selectedTransactionId = -1;
    private List<Integer> transactionIds = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);

        try {
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

            // Initialize views with null checks
            btnDelete = findViewById(R.id.btnDelete);
            btnEdit = findViewById(R.id.btnEdit);
            btnSave = findViewById(R.id.btnSave);
            tableTransactions = findViewById(R.id.tableTransactions);

            // Check if views were found
            if (btnDelete == null) Log.e("ModifyActivity", "btnDelete is null!");
            if (btnEdit == null) Log.e("ModifyActivity", "btnEdit is null!");
            if (btnSave == null) Log.e("ModifyActivity", "btnSave is null!");
            if (tableTransactions == null) Log.e("ModifyActivity", "tableTransactions is null!");

            // Load transactions from database
            loadAllTransactions();

            // Set button listeners with null checks
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (selectedTransactionId != -1) {
                        deleteSelectedTransaction();
                    } else {
                        Toast.makeText(this, "Please select a transaction first", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    if (selectedTransactionId != -1) {
                        editSelectedTransaction();
                    } else {
                        Toast.makeText(this, "Please select a transaction first", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (btnSave != null) {
                btnSave.setOnClickListener(v -> {
                    Toast.makeText(this, "Returning to dashboard", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

        } catch (Exception e) {
            Log.e("ModifyActivity", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadAllTransactions() {
        try {
            if (userId == -1) return;

            // Clear existing table rows (keep header)
            if (tableTransactions != null) {
                int childCount = tableTransactions.getChildCount();
                if (childCount > 1) { // Keep header row (index 0)
                    tableTransactions.removeViews(1, childCount - 1);
                }
            }

            // Clear transaction IDs list
            transactionIds.clear();

            // Get all transactions from database
            Cursor cursor = databaseHelper.getAllTransactions(userId);

            if (cursor == null) {
                Toast.makeText(this, "No transactions found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create table rows for each transaction
            int rowColor = 0;
            while (cursor.moveToNext()) {
                int transactionId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_ID));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));

                // Store transaction ID
                transactionIds.add(transactionId);

                // Create table row
                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));
                row.setTag(transactionId); // Use tag to store transaction ID

                // Set alternating row colors
                int backgroundColor;
                try {
                    backgroundColor = (rowColor % 2 == 0)
                            ? ContextCompat.getColor(this, android.R.color.white)
                            : ContextCompat.getColor(this, R.color.light_gray);
                } catch (Exception e) {
                    backgroundColor = (rowColor % 2 == 0) ? 0xFFFFFFFF : 0xFFF8F8F8;
                }
                row.setBackgroundColor(backgroundColor);
                rowColor++;

                // Add cells to row
                row.addView(createTableCell(type));
                row.addView(createTableCell(categoryName));
                row.addView(createTableCell(description.length() > 10 ? description.substring(0, 10) + "..." : description));
                row.addView(createTableCell(formatDate(date)));
                row.addView(createTableCell("$" + String.format(Locale.US, "%.2f", amount)));

                // Set click listener for row selection
                final int currentTransactionId = transactionId;
                row.setOnClickListener(v -> {
                    // Reset all rows to default colors
                    resetRowColors();

                    // Highlight selected row
                    try {
                        row.setBackgroundColor(ContextCompat.getColor(ModifyTransactionActivity.this, R.color.selected_color));
                    } catch (Exception e) {
                        row.setBackgroundColor(0xFFE3F2FD); // Light blue
                    }
                    selectedTransactionId = currentTransactionId;

                    Toast.makeText(ModifyTransactionActivity.this,
                            "Selected: " + categoryName,
                            Toast.LENGTH_SHORT).show();
                });

                // Add row to table
                if (tableTransactions != null) {
                    tableTransactions.addView(row);
                }
            }

            cursor.close();

            if (transactionIds.isEmpty()) {
                Toast.makeText(this, "No transactions to display", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("ModifyActivity", "Error loading transactions: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading transactions", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView createTableCell(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 12, 16, 12);
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
        ));
        return textView;
    }

    private String formatDate(String date) {
        try {
            String[] parts = date.split("-");
            if (parts.length == 3) {
                return parts[1] + "/" + parts[2] + "/" + parts[0].substring(2); // MM/DD/YY
            }
        } catch (Exception e) {
            Log.e("ModifyActivity", "Error formatting date: " + date, e);
        }
        return date;
    }

    private void resetRowColors() {
        try {
            if (tableTransactions == null) return;

            int childCount = tableTransactions.getChildCount();
            for (int i = 1; i < childCount; i++) { // Start from 1 to skip header
                View child = tableTransactions.getChildAt(i);
                if (child instanceof TableRow) {
                    TableRow row = (TableRow) child;
                    int backgroundColor;
                    try {
                        backgroundColor = ((i - 1) % 2 == 0) // Adjust for header offset
                                ? ContextCompat.getColor(this, android.R.color.white)
                                : ContextCompat.getColor(this, R.color.light_gray);
                    } catch (Exception e) {
                        backgroundColor = ((i - 1) % 2 == 0) ? 0xFFFFFFFF : 0xFFF8F8F8;
                    }
                    row.setBackgroundColor(backgroundColor);
                }
            }
        } catch (Exception e) {
            Log.e("ModifyActivity", "Error resetting row colors: " + e.getMessage(), e);
        }
    }

    private void deleteSelectedTransaction() {
        try {
            if (selectedTransactionId == -1) {
                Toast.makeText(this, "Please select a transaction first", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show confirmation dialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Delete Transaction");
            builder.setMessage("Are you sure you want to delete this transaction?");
            builder.setPositiveButton("Delete", (dialog, which) -> {
                // Delete transaction from database
                int rowsDeleted = databaseHelper.deleteTransaction(selectedTransactionId);

                if (rowsDeleted > 0) {
                    Toast.makeText(this, "Transaction deleted successfully!", Toast.LENGTH_SHORT).show();

                    // Clear selection
                    selectedTransactionId = -1;

                    // Refresh the table
                    loadAllTransactions();
                } else {
                    Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();

        } catch (Exception e) {
            Log.e("ModifyActivity", "Error deleting transaction: " + e.getMessage(), e);
            Toast.makeText(this, "Error deleting transaction", Toast.LENGTH_SHORT).show();
        }
    }

    private void editSelectedTransaction() {
        try {
            if (selectedTransactionId == -1) {
                Toast.makeText(this, "Please select a transaction first", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start EditTransactionActivity with selected transaction ID
            Intent editIntent = new Intent(this, EditTransactionActivity.class);
            editIntent.putExtra("USER_ID", userId);
            editIntent.putExtra("TRANSACTION_ID", selectedTransactionId);
            startActivity(editIntent);

        } catch (Exception e) {
            Log.e("ModifyActivity", "Error editing transaction: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening edit screen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        if (userId != -1) {
            loadAllTransactions();
            selectedTransactionId = -1; // Reset selection
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}