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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvIncome, tvExpense;
    private Button btnAdd, btnEditDelete, btnReports, btnAddNewTransaction;
    private TableLayout tableTransactions;
    private DatabaseHelper databaseHelper;
    private int userId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Get user ID from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("USER_ID", -1);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        btnAdd = findViewById(R.id.btnAdd);
        btnEditDelete = findViewById(R.id.btnEditDelete);
        btnReports = findViewById(R.id.btnReports);
        btnAddNewTransaction = findViewById(R.id.btnAddNewTransaction);
        tableTransactions = findViewById(R.id.tableTransactions);

        // Load data from database
        loadDashboardData();
        loadLatestTransactions();


        btnAdd.setOnClickListener(v -> {
            Intent addIntent = new Intent(this, AddTransactionActivity.class);
            addIntent.putExtra("USER_ID", userId); // Pass user ID
            startActivity(addIntent);
        });

        btnAddNewTransaction.setOnClickListener(v -> {
            Intent addIntent = new Intent(this, AddTransactionActivity.class);
            addIntent.putExtra("USER_ID", userId); // Pass user ID
            startActivity(addIntent);
        });

        btnEditDelete.setOnClickListener(v -> {
            Intent editIntent = new Intent(this, ModifyTransactionActivity.class);
            editIntent.putExtra("USER_ID", userId); // Pass user ID
            startActivity(editIntent);
        });

        btnReports.setOnClickListener(v -> {
            Intent reportIntent = new Intent(this, ReportActivity.class);
            reportIntent.putExtra("USER_ID", userId); // Pass user ID
            startActivity(reportIntent);
        });
    }

    private void loadDashboardData() {
        if (userId == -1) return;

        // Get totals from database
        double totalIncome = databaseHelper.getTotalIncomeLast30Days(userId);
        double totalExpense = databaseHelper.getTotalExpenseLast30Days(userId);

        // Format currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        // Update UI
        tvIncome.setText(currencyFormat.format(totalIncome));
        tvExpense.setText(currencyFormat.format(totalExpense));
    }

    private void loadLatestTransactions() {
        if (userId == -1) return;

        // Clear existing table rows (keep header)
        int childCount = tableTransactions.getChildCount();
        if (childCount > 1) { // Keep header row (index 0)
            tableTransactions.removeViews(1, childCount - 1);
        }

        // Get latest transactions from database
        Cursor cursor = databaseHelper.getLatestTransactions(userId, 3);

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
        // Refresh data when returning to dashboard
        if (userId != -1) {
            loadDashboardData();
            loadLatestTransactions();
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