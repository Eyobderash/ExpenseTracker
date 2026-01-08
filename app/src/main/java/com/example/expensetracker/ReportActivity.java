package com.example.expensetracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.text.NumberFormat;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private TextView tvTotalIncome, tvTotalExpense, tvWallet;
    private Button btnDone;
    private TableLayout tableTransactions;
    private DatabaseHelper databaseHelper;
    private int userId;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

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
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvWallet = findViewById(R.id.tvWallet);
        btnDone = findViewById(R.id.btnDone);
        tableTransactions = findViewById(R.id.tableTransactions);

        // Load report data
        loadReportData();
        loadAllTransactions();

        btnDone.setOnClickListener(v -> finish());
    }

    private void loadReportData() {
        if (userId == -1) return;

        try {
            // Get ALL transactions (lifetime) for this user
            Cursor cursor = databaseHelper.getAllTransactions(userId);

            double totalIncome = 0;
            double totalExpense = 0;

            // Calculate totals from ALL transactions
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));

                    if (type.equals("Income")) {
                        totalIncome += amount;
                    } else if (type.equals("Expense")) {
                        totalExpense += amount;
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }

            // Calculate wallet (lifetime balance)
            double walletBalance = totalIncome - totalExpense;

            // Format currency
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

            // Update UI
            tvTotalIncome.setText(currencyFormat.format(totalIncome));
            tvTotalExpense.setText(currencyFormat.format(totalExpense));

            // Set wallet text with color coding
            tvWallet.setText(currencyFormat.format(walletBalance));
            if (walletBalance >= 0) {
                tvWallet.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            } else {
                tvWallet.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading report data", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void loadAllTransactions() {
        if (userId == -1) return;

        try {
            // Clear existing table rows (keep header)
            int childCount = tableTransactions.getChildCount();
            if (childCount > 1) { // Keep header row (index 0)
                tableTransactions.removeViews(1, childCount - 1);
            }

            // Get ALL transactions from database
            Cursor cursor = databaseHelper.getAllTransactions(userId);

            // Create table rows for each transaction
            int rowColor = 0;
            while (cursor.moveToNext()) {
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));

                // Create table row
                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                // Set alternating row colors
                int backgroundColor = (rowColor % 2 == 0)
                        ? ContextCompat.getColor(this, android.R.color.white)
                        : ContextCompat.getColor(this, R.color.light_gray);
                row.setBackgroundColor(backgroundColor);
                rowColor++;

                // Add cells to row
                row.addView(createTableCell(type));
                row.addView(createTableCell(categoryName));
                row.addView(createTableCell(description.length() > 10 ? description.substring(0, 10) + "..." : description));
                row.addView(createTableCell(formatDate(date)));
                row.addView(createTableCell("$" + String.format(Locale.US, "%.2f", amount)));

                // Add row to table
                tableTransactions.addView(row);
            }

            cursor.close();

            // Show message if no transactions
            if (rowColor == 0) {
                TableRow emptyRow = new TableRow(this);
                TextView emptyText = new TextView(this);
                emptyText.setText("No transactions found");
                emptyText.setGravity(Gravity.CENTER);
                emptyText.setPadding(16, 12, 16, 12);
                emptyText.setTextSize(14);
                emptyRow.addView(emptyText);
                tableTransactions.addView(emptyRow);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading transactions", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private TextView createTableCell(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 12, 16, 12);
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new TableRow.LayoutParams(
                0, // 0dp for weight
                TableRow.LayoutParams.WRAP_CONTENT,
                1f // weight = 1
        ));
        return textView;
    }

    private String formatDate(String date) {
        // Format date from database (assuming format: YYYY-MM-DD)
        try {
            String[] parts = date.split("-");
            if (parts.length == 3) {
                return parts[1] + "/" + parts[2] + "/" + parts[0].substring(2); // MM/DD/YY
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to report
        if (userId != -1) {
            loadReportData();
            loadAllTransactions();
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