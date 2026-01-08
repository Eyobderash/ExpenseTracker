package com.example.expensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expense_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_TRANSACTIONS = "transactions";

    // Column names
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_TYPE = "type";

    public static final String COLUMN_TRANSACTION_ID = "transaction_id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_DATE = "date";

    // SQL statements
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + "(" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_FIRST_NAME + " TEXT NOT NULL," +
                    COLUMN_LAST_NAME + " TEXT," + // Last name is optional
                    COLUMN_USERNAME + " TEXT UNIQUE NOT NULL," +
                    COLUMN_PASSWORD + " TEXT NOT NULL" +
                    ")";

    private static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + "(" +
                    COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_CATEGORY_NAME + " TEXT NOT NULL," +
                    COLUMN_TYPE + " TEXT NOT NULL CHECK(" + COLUMN_TYPE + " IN ('Expense', 'Income'))" +
                    ")";

    private static final String CREATE_TABLE_TRANSACTIONS =
            "CREATE TABLE " + TABLE_TRANSACTIONS + "(" +
                    COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USER_ID + " INTEGER NOT NULL," +
                    COLUMN_CATEGORY_ID + " INTEGER NOT NULL," +
                    COLUMN_TYPE + " TEXT NOT NULL CHECK(" + COLUMN_TYPE + " IN ('Expense', 'Income'))," +
                    COLUMN_DESCRIPTION + " TEXT," +
                    COLUMN_AMOUNT + " REAL NOT NULL CHECK(" + COLUMN_AMOUNT + " > 0)," +
                    COLUMN_DATE + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE," +
                    "FOREIGN KEY(" + COLUMN_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID + ")" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        // NO DEFAULT DATA
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }



    public long registerUser(String firstName, String lastName, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        return db.insert(TABLE_USERS, null, values);
    }

    public boolean loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ? AND " +
                COLUMN_PASSWORD + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username});
        boolean taken = cursor.getCount() > 0;
        cursor.close();
        return taken;
    }

    // NEW METHOD: Get user's full name
    public String getUserFullName(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_FIRST_NAME + ", " + COLUMN_LAST_NAME +
                " FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username});
        String fullName = "";
        if (cursor.moveToFirst()) {
            String firstName = cursor.getString(0);
            String lastName = cursor.getString(1);
            fullName = firstName + (lastName != null && !lastName.isEmpty() ? " " + lastName : "");
        }
        cursor.close();
        return fullName;
    }



    public long addTransaction(int userId, int categoryId, String type,
                               String description, double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_CATEGORY_ID, categoryId);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DATE, date);

        return db.insert(TABLE_TRANSACTIONS, null, values);
    }

    public Cursor getAllTransactions(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT t.*, c." + COLUMN_CATEGORY_NAME +
                " FROM " + TABLE_TRANSACTIONS + " t" +
                " INNER JOIN " + TABLE_CATEGORIES + " c ON t." + COLUMN_CATEGORY_ID + " = c." + COLUMN_CATEGORY_ID +
                " WHERE t." + COLUMN_USER_ID + " = ?" +
                " ORDER BY t." + COLUMN_DATE + " DESC";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }
    public Cursor getCategoryByNameAndType(String name, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CATEGORIES,
                null,
                COLUMN_CATEGORY_NAME + " = ? AND " + COLUMN_TYPE + " = ?",
                new String[]{name, type},
                null, null, null);
    }

    public double[] getLifetimeTotals(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double[] totals = new double[2]; // [0] = income, [1] = expense

        // Get total income
        String incomeQuery = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_USER_ID + " = ?" +
                " AND " + COLUMN_TYPE + " = 'Income'";
        Cursor incomeCursor = db.rawQuery(incomeQuery, new String[]{String.valueOf(userId)});
        if (incomeCursor.moveToFirst()) {
            totals[0] = incomeCursor.getDouble(0);
        }
        incomeCursor.close();

        // Get total expense
        String expenseQuery = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_USER_ID + " = ?" +
                " AND " + COLUMN_TYPE + " = 'Expense'";
        Cursor expenseCursor = db.rawQuery(expenseQuery, new String[]{String.valueOf(userId)});
        if (expenseCursor.moveToFirst()) {
            totals[1] = expenseCursor.getDouble(0);
        }
        expenseCursor.close();

        return totals;
    }

    public Cursor getTransactionById(int transactionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT t.*, c." + COLUMN_CATEGORY_NAME +
                " FROM " + TABLE_TRANSACTIONS + " t" +
                " INNER JOIN " + TABLE_CATEGORIES + " c ON t." + COLUMN_CATEGORY_ID + " = c." + COLUMN_CATEGORY_ID +
                " WHERE t." + COLUMN_TRANSACTION_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(transactionId)});
    }

    public int deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TRANSACTIONS,
                COLUMN_TRANSACTION_ID + " = ?",
                new String[]{String.valueOf(transactionId)});
    }

    public int updateTransaction(int transactionId, int categoryId, String type,
                                 String description, double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_ID, categoryId);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DATE, date);

        return db.update(TABLE_TRANSACTIONS, values,
                COLUMN_TRANSACTION_ID + " = ?",
                new String[]{String.valueOf(transactionId)});
    }



    public long addCategory(String name, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        values.put(COLUMN_TYPE, type);

        return db.insert(TABLE_CATEGORIES, null, values);
    }

    public Cursor getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CATEGORIES,
                null, null, null, null, null,
                COLUMN_TYPE + ", " + COLUMN_CATEGORY_NAME);
    }

    public Cursor getCategoriesByType(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CATEGORIES,
                null,
                COLUMN_TYPE + " = ?",
                new String[]{type},
                null, null,
                COLUMN_CATEGORY_NAME);
    }



    public double getTotalIncomeLast30Days(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_USER_ID + " = ?" +
                " AND " + COLUMN_TYPE + " = 'Income'" +
                " AND " + COLUMN_DATE + " >= date('now', '-30 days')";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public double getTotalExpenseLast30Days(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_USER_ID + " = ?" +
                " AND " + COLUMN_TYPE + " = 'Expense'" +
                " AND " + COLUMN_DATE + " >= date('now', '-30 days')";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public Cursor getLatestTransactions(int userId, int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT t.*, c." + COLUMN_CATEGORY_NAME +
                " FROM " + TABLE_TRANSACTIONS + " t" +
                " INNER JOIN " + TABLE_CATEGORIES + " c ON t." + COLUMN_CATEGORY_ID + " = c." + COLUMN_CATEGORY_ID +
                " WHERE t." + COLUMN_USER_ID + " = ?" +
                " ORDER BY t." + COLUMN_DATE + " DESC LIMIT ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(limit)});
    }
}