# Food Waste Management System

A Java and MySQL-based Object-Oriented Programming (OOP) Mini-Project designed to track surplus food donations, streamline collections by NGOs and charities, reduce food wastage, and enforce food safety through real-time inventory and expiry tracking.

---

## 👥 Team Members

* **Ibrahim Tauheed**
* **Krishna Tripathi**
* **Arnav**
* **Ujjawal Saffar**

---

## 📌 Project Overview

Food waste is a major global issue, with tons of edible food discarded daily while many go hungry. The **Food Waste Management System** bridges the gap between food donors (such as restaurants, canteens, bakeries, and grocery stores) and recipient organizations (charities, NGOs, food banks, and volunteers).

The project applies core **Object-Oriented Programming (OOP)** principles—encapsulation, abstraction, inheritance, and modularity—along with robust relational database management and transactional integrity to manage the entire lifecycle of food donations.

---

## ✨ Key Features

1. **Executive Dashboard & Analytics**
   * Real-time metrics: Total donations registered, active available inventory, remaining quantity, expired food alerts, total registered providers, collections completed, and waste incidents logged.
   * Recent activity log displaying newly added donations.

2. **Food Inventory Management**
   * View all food items or filter specifically for available, non-expired food.
   * Food safety checks preventing the distribution of expired goods.
   * Categorization (e.g., Cooked Meals, Bakery, Produce, Dairy, Packaged).

3. **Donation Registration (Add Food)**
   * Input details including food name, category, quantity, unit (kg, servings, packets, etc.), preparation date, expiration date, and donor/provider linkage.

4. **Edit & Delete Operations**
   * Update details of existing food items.
   * Safe deletion controls preventing accidental removal of items already referenced in collection or waste audit records.

5. **Search & Filter**
   * Dynamic search by keyword across food items, categories, status, and provider.

6. **Provider / Donor Management**
   * Register and maintain donor profiles (contact number, address, business name).
   * Track donation history per provider with referential integrity safeguards.

7. **Collections & Distribution**
   * Allows NGOs, food banks, and collectors to log food collection.
   * **Atomic Database Transactions:** Automatically decrements inventory and updates status (`AVAILABLE` → `PARTIALLY_COLLECTED` → `COLLECTED`).
   * Built-in food safety verification preventing distribution of past expiry items.

8. **Waste & Spoilage Auditing**
   * Log spoiled, damaged, or expired food with specific reasons.
   * Automatically adjusts inventory levels and marks records as `WASTED` or `EXPIRED`.

9. **Secure Database Connectivity**
   * Centralized JDBC management with zero hardcoded credentials.
   * Flexible configuration using environment variables or in-app settings.

---

## 🛠️ Technology Stack

* **Programming Language:** Java (JDK 8 / 17 / 21 / 25 compatible)
* **GUI / Frontend:** Java AWT (Abstract Window Toolkit) using `CardLayout`
* **Database:** MySQL 8.x / 9.x
* **Database Driver:** MySQL Connector/J JDBC Driver (`9.1.0` / `8.0.33`)
* **Build Tool:** Apache Maven
* **Architecture:** Model-View-DAO (Data Access Object) Pattern

---

## 📂 Project Structure

```text
FoodWasteManagementSystem/
├── pom.xml                               # Maven project configuration (dependencies, build plugins)
├── README.md                             # Project documentation
├── .gitignore                            # Git ignore specifications
├── src/                                  # Modern Maven Source Code
│   └── main/
│       └── java/
│           └── com/
│               └── foodwaste/
│                   ├── Main.java         # Console database test & domain verification entrypoint
│                   ├── db/
│                   │   └── DBConnection.java  # Centralized JDBC connection manager
│                   └── model/
│                       └── FoodItem.java      # Encapsulated FoodItem domain model
└── applet-legacy/                        # Desktop GUI Module (AWT Applet / AppletViewer)
    ├── FoodWasteApplet.html              # HTML launcher for appletviewer
    ├── applet.policy                     # JVM security policy for JDBC & environment permissions
    ├── compile-applet.bat                # Windows compilation batch script
    ├── run-applet.bat                    # Windows launch batch script for appletviewer
    ├── lib/
    │   └── mysql-connector-j-8.0.33.jar  # Bundled MySQL JDBC driver
    └── src/                              # GUI source files & DAOs
        ├── FoodWasteApplet.java          # Main AWT GUI application (9 dashboard cards/screens)
        ├── AppletDB.java                 # Applet database connection helper
        ├── FoodDAO.java                  # Data access object for food items
        ├── ProviderDAO.java              # Data access object for donors/providers
        ├── CollectionDAO.java            # Transactional DAO for food collections
        ├── WasteDAO.java                 # Transactional DAO for food waste records
        ├── DashboardDAO.java             # Analytics & KPI aggregate query DAO
        ├── FoodItemModel.java            # Food item model class
        ├── Provider.java                 # Provider model class
        ├── CollectionRecord.java         # Collection record model class
        ├── WasteRecord.java              # Waste record model class
        ├── DashboardStats.java           # Dashboard statistics model class
        ├── AppletIntegrationTest.java    # Automated JDBC test harness
        └── AppletLifecycleTest.java      # Headless AWT lifecycle & data validation test
```

---

## 🗄️ Database Setup (MySQL)

Open your MySQL client (MySQL Workbench, MySQL CLI, or phpMyAdmin) and run the following script:

```sql
CREATE DATABASE IF NOT EXISTS food_waste_db;
USE food_waste_db;

-- 1. Providers / Donors table
CREATE TABLE IF NOT EXISTS providers (
    provider_id INT AUTO_INCREMENT PRIMARY KEY,
    provider_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Food Items inventory table
CREATE TABLE IF NOT EXISTS food_items (
    food_id INT AUTO_INCREMENT PRIMARY KEY,
    food_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) DEFAULT 'Other',
    quantity DOUBLE NOT NULL,
    unit VARCHAR(20) DEFAULT 'servings',
    preparation_date DATE,
    expiry_date DATE NOT NULL,
    status VARCHAR(30) DEFAULT 'AVAILABLE',
    provider_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (provider_id) REFERENCES providers(provider_id) ON DELETE RESTRICT
);

-- 3. Collections table (food claims by NGOs/charities)
CREATE TABLE IF NOT EXISTS collections (
    collection_id INT AUTO_INCREMENT PRIMARY KEY,
    food_id INT NOT NULL,
    collector_name VARCHAR(100) NOT NULL,
    quantity_collected DOUBLE NOT NULL,
    collection_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (food_id) REFERENCES food_items(food_id) ON DELETE CASCADE
);

-- 4. Waste Records table (spoilage / expiry audit log)
CREATE TABLE IF NOT EXISTS waste_records (
    waste_id INT AUTO_INCREMENT PRIMARY KEY,
    food_id INT NOT NULL,
    quantity_wasted DOUBLE NOT NULL,
    reason VARCHAR(255) DEFAULT 'Expired/Spoiled',
    waste_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (food_id) REFERENCES food_items(food_id) ON DELETE CASCADE
);

-- Sample Data (Optional)
INSERT INTO providers (provider_name, phone, address) VALUES
('Campus Central Canteen', '9876543210', 'Student Center, Block A'),
('Daily Fresh Bakery', '9812345678', 'Main Market Street');

INSERT INTO food_items (food_name, category, quantity, unit, preparation_date, expiry_date, status, provider_id) VALUES
('Vegetable Rice', 'Cooked Meals', 25.0, 'servings', '2026-10-28', '2026-10-29', 'EXPIRED', 1),
('Bread Loaves', 'Bakery', 15.0, 'packs', '2026-10-30', '2026-11-05', 'AVAILABLE', 2),
('Dal and Roti Combo', 'Cooked Meals', 30.0, 'servings', '2026-10-28', '2026-10-29', 'EXPIRED', 1);
```

---

## ⚙️ Environment Variables (Optional)

The application automatically reads configuration parameters from environment variables if set; otherwise, it falls back to sensible local defaults:

| Variable | Description | Default Value |
| :--- | :--- | :--- |
| `DB_HOST` | MySQL Server hostname | `localhost` |
| `DB_PORT` | MySQL Server port | `3306` |
| `DB_NAME` | Database schema name | `food_waste_db` |
| `DB_USER` / `MYSQL_USER` | Database username | `root` |
| `DB_PASSWORD` / `MYSQL_PWD` | Database password | `(blank)` / `root` |
| `DB_URL` | Direct JDBC Connection URL override | *(Generated from above parameters)* |

---

## 🚀 How to Run the Project

### Option 1: Run the Maven Application (CLI & DB Test)

This option verifies database connectivity, queries `food_items`, formats records in a formatted console table, and verifies OOP domain mapping.

```bash
# 1. Navigate to project root
cd FoodWasteManagementSystem

# 2. Compile the project
mvn clean compile

# 3. Run the Main application
mvn exec:java
```

Or package into an executable JAR:
```bash
mvn clean package
java -jar target/FoodWasteManagementSystem-1.0-SNAPSHOT.jar
```

---

### Option 2: Run the Desktop GUI Module (AWT Applet)

The desktop graphical interface provides the complete 9-screen management suite.

#### On Windows:
1. Open `applet-legacy/compile-applet.bat` to verify your JDK 8 path.
2. Run `compile-applet.bat` to compile all classes into `applet-legacy/`.
3. Run `run-applet.bat` to launch the GUI inside `appletviewer`.

#### Manual Command Line (JDK 8):
```bash
cd applet-legacy

# Compile with bundled MySQL driver in classpath
javac -encoding UTF-8 -cp "lib/mysql-connector-j-8.0.33.jar" -d . src/*.java

# Run using appletviewer
appletviewer -J-Djava.security.policy=applet.policy FoodWasteApplet.html
```

---

### Option 3: Run the Automated Verification Tests

The project includes headless test harnesses to verify database operations and UI lifecycle without opening an applet window:

```bash
cd applet-legacy

# Run JDBC DAO Integration Tests:
java -cp ".;lib/mysql-connector-j-8.0.33.jar" AppletIntegrationTest

# Run Applet Lifecycle & Food-Safety UI Verification Tests:
java -cp ".;lib/mysql-connector-j-8.0.33.jar" AppletLifecycleTest
```
*(On macOS / Linux, use `:` instead of `;` as the classpath separator).*

---

## 📜 License

This project is created as an academic mini-project for Object-Oriented Programming (OOP) coursework. Free to use and modify for learning purposes.
