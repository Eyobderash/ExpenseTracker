# 📱 Expense Tracker Android App

A complete **personal finance management** Android application built using **Java** and **SQLite**. This app helps users track income and expenses, view detailed financial reports, and manage transactions — all **offline** and securely on-device.

---

## 🎯 Features

### ✅ User Authentication

* Secure user **registration and login**
* **User-specific data isolation**
* Basic **user profile management**

---

### 💰 Transaction Management

* **Add Transactions**: Record income and expenses with custom categories
* **Edit Transactions**: Update existing records
* **Delete Transactions**: Remove unwanted entries
* **Categorization**: Organize transactions by custom categories

---

### 📊 Dashboard

* **30-Day Overview**: Income & expense totals for the last 30 days
* **Latest Transactions**: View recent financial activity
* **Real-Time Updates**: Automatically refreshes when data changes

---

### 📈 Reports & Analytics

* **Lifetime Totals**: Full income and expense history
* **Wallet Balance**: Net balance calculation *(Income − Expense)*
* **Transaction History**: Detailed tabular view of all transactions
* **Category Analysis**: Spending breakdown by category

---

### 💾 Data Management

* **Local SQLite Database**: Secure on-device storage
* **Offline Support**: No internet connection required
* **Persistent Data**: Financial records are preserved

---

## 🛠 Technical Stack

* **Language**: Java
* **Database**: SQLite
* **Architecture**: MVC (Model–View–Controller)
* **Minimum SDK**: API 21 (Android 5.0)
* **Target SDK**: Latest Android Version

---

## 📋 Database Schema

### 👤 Users Table

```sql
users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL
)
```

### 🗂 Categories Table

```sql
categories (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL CHECK(type IN ('Expense', 'Income'))
)
```

### 💳 Transactions Table

```sql
transactions (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    type TEXT NOT NULL CHECK(type IN ('Expense', 'Income')),
    description TEXT,
    amount REAL NOT NULL CHECK(amount > 0),
    date TEXT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY(category_id) REFERENCES categories(category_id)
)
```

---

## 🚀 Installation

### Prerequisites

* Android Studio (Latest Version)
* Android SDK (API 21+)
* Java Development Kit (JDK 11+)

### Build Instructions

1. Clone the repository
2. Open the project in **Android Studio**
3. Sync **Gradle dependencies**
4. Build the project: `Build → Make Project`
5. Run on an emulator or physical device

### Running the App

1. Launch the application
2. Register a new account or log in
3. Start adding income and expense transactions
4. Explore the dashboard and reports

---

## 📱 Screens

### 1️⃣ Login Screen

* User authentication
* Sign-up option for new users

### 2️⃣ Registration Screen

* First name, last name, username, password
* Password confirmation validation

### 3️⃣ Dashboard Screen

* Income & expense totals (last 30 days)
* Latest transactions
* Quick access buttons

### 4️⃣ Add Transaction Screen

* Category selection (Income / Expense)
* Amount input
* Description field
* Date picker

### 5️⃣ Modify Transactions Screen

* View transactions in table format
* Edit or delete selected transactions

### 6️⃣ Reports Screen

* Lifetime income & expense totals
* Wallet balance calculation
* Complete transaction history table

---

## 🔧 Key Features Implementation

### Database Operations

All database logic is handled in **`DatabaseHelper.java`**, including:

* User registration & authentication
* Transaction CRUD operations
* Category management
* Data aggregation for reports

### UI Components

* **TableLayout**: Dynamic transaction tables
* **RadioGroup**: Category type selection
* **EditText**: Validated user input fields
* **Buttons**: Navigation & actions

### Data Validation

* Input validation for all fields
* User-friendly error messages
* SQLite constraint enforcement

---

## 📊 Performance Optimizations

* **Efficient SQL Queries**
* **Database Indexing** for faster lookups
* **Proper Cursor Management** to avoid memory leaks
* **Optimized UI** for smooth scrolling

---

## 🧪 Testing

### Manual Testing

* User registration & login
* Add, edit, and delete transactions
* Data persistence across restarts
* Report accuracy

### Automated Testing Targets

* Database operations
* Input validation
* UI functionality
* Navigation flows

---

## 🔒 Security Features

* Local-only data storage (no cloud)
* Input validation to prevent SQL injection
* User data isolation
* Secure credential handling

---

## 📈 Future Enhancements

### Planned Features

* Budget management
* Data export (CSV / PDF)
* Charts & graphs
* Cloud backup & restore
* Multi-currency support
* Recurring transactions
* Receipt scanning (OCR)
* Advanced category customization

### Technical Improvements

* MVVM architecture
* Room database migration
* Kotlin migration
* Unit & integration testing
* CI/CD pipeline

---

## 🐛 Troubleshooting

### Common Issues

**App crashes on startup**

* Verify permissions
* Check database initialization
* Ensure layout files exist

**Transactions not saving**

* Validate inputs
* Confirm database write logic
* Review error handling

**Slow performance**

* Add database indexes
* Optimize queries
* Implement pagination

### Debugging Tips

* Enable logging in `DatabaseHelper.java`
* Use Android Studio Database Inspector
* Monitor Logcat
* Test with sample data

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to your branch
5. Open a Pull Request

### Contribution Guidelines

* Follow Java coding standards
* Comment complex logic
* Update documentation
* Write tests for new features

---

## 📚 Learning Resources

### For This Project

* Android SQLite Documentation
* Java Android Development Guide
* Material Design Components

### Related Topics

* Personal finance management
* Mobile app development
* Database design
* UI/UX principles

---

## 🏆 Project Achievements

This project demonstrates proficiency in:

* Android development with Java
* SQLite integration
* Authentication systems
* Financial data reporting
* End-to-end software development

---

## 📄 License

This project is licensed under the **MIT License**. See the `LICENSE` file for details.

---

## 👏 Acknowledgments

* Android Developer Community
* SQLite Documentation Team
* Open-source libraries used
* Test users for valuable feedback
