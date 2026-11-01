package com.foodwaste;

import com.foodwaste.db.DBConnection;
import com.foodwaste.model.FoodItem;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application entry point for testing the Java-MySQL database connection
 * and verifying records in the food_items table.
 */
public class Main {

    public static void main(String[] args) {
        try {
            runDatabaseTest();
        } finally {
            // Cleanly shutdown MySQL AbandonedConnectionCleanupThread to prevent
            // lingering thread warnings in Maven exec:java and container environments.
            DBConnection.shutdownCleanupThread();
        }
    }

    private static void runDatabaseTest() {
        System.out.println("===============================================================");
        System.out.println("    FOOD WASTE MANAGEMENT SYSTEM - DATABASE CONNECTION TEST    ");
        System.out.println("===============================================================");

        String url = DBConnection.getJdbcUrl();
        String user = DBConnection.getDbUser();
        String password = DBConnection.getDbPassword();
        String maskedPassword = password.isEmpty() ? "(none / blank)" : "********";

        System.out.println("[Config] Target JDBC URL : " + url);
        System.out.println("[Config] Database User   : " + user);
        System.out.println("[Config] Password Status : " + maskedPassword);
        System.out.println("---------------------------------------------------------------");
        System.out.println("Connecting to MySQL Database...");

        // Try-with-resources ensures automatic cleanup of JDBC resources
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null || conn.isClosed()) {
                System.err.println("[ERROR] Failed to obtain an open database connection.");
                return;
            }

            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("[SUCCESS] Connected successfully!");
            System.out.println("[DB Info] Product Name    : " + meta.getDatabaseProductName());
            System.out.println("[DB Info] Product Version : " + meta.getDatabaseProductVersion());
            System.out.println("[DB Info] Driver Name     : " + meta.getDriverName());
            System.out.println("[DB Info] Driver Version  : " + meta.getDriverVersion());
            System.out.println("===============================================================");
            System.out.println("Executing SELECT query on 'food_items' table...");
            System.out.println("---------------------------------------------------------------");

            fetchAndDisplayFoodItems(conn);

        } catch (SQLException ex) {
            handleSQLException(ex);
        } catch (Exception ex) {
            System.err.println("[UNEXPECTED ERROR] An unexpected exception occurred:");
            ex.printStackTrace();
        }
    }

    /**
     * Executes SELECT * FROM food_items, prints records in a tabular format,
     * and maps rows into FoodItem domain objects.
     *
     * @param conn Open database connection
     * @throws SQLException on query execution error
     */
    private static void fetchAndDisplayFoodItems(Connection conn) throws SQLException {
        String sql = "SELECT * FROM food_items";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            if (columnCount == 0) {
                System.out.println("No columns returned by table 'food_items'.");
                return;
            }

            // Collect column names
            List<String> colNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                colNames.add(rsmd.getColumnLabel(i));
            }

            // Cache rows to calculate dynamic column widths
            List<List<String>> rows = new ArrayList<>();
            List<Integer> colWidths = new ArrayList<>();
            for (String name : colNames) {
                colWidths.add(name.length());
            }

            int recordCount = 0;
            List<FoodItem> items = new ArrayList<>();

            while (rs.next()) {
                recordCount++;
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String val = rs.getString(i);
                    val = (val == null) ? "NULL" : val;
                    row.add(val);
                    if (val.length() > colWidths.get(i - 1)) {
                        colWidths.set(i - 1, val.length());
                    }
                }
                rows.add(row);

                // OOP Mapping to FoodItem model object (best-effort based on common column names)
                FoodItem item = mapResultSetToFoodItem(rs, rsmd);
                if (item != null) {
                    items.add(item);
                }
            }

            if (recordCount == 0) {
                System.out.println("[INFO] Query executed successfully, but table 'food_items' is currently empty.");
                return;
            }

            // Build format strings for table output
            StringBuilder formatBuilder = new StringBuilder();
            StringBuilder separatorBuilder = new StringBuilder("+");
            for (int width : colWidths) {
                formatBuilder.append("| %-").append(width).append("s ");
                separatorBuilder.append("-".repeat(width + 2)).append("+");
            }
            formatBuilder.append("|\n");
            String rowFormat = formatBuilder.toString();
            String separator = separatorBuilder.toString();

            // Print table header
            System.out.println(separator);
            System.out.printf(rowFormat, colNames.toArray());
            System.out.println(separator);

            // Print rows
            for (List<String> row : rows) {
                System.out.printf(rowFormat, row.toArray());
            }
            System.out.println(separator);
            System.out.printf("[SUMMARY] Successfully retrieved %d record(s) from 'food_items'.\n", recordCount);

            if (!items.isEmpty()) {
                System.out.println("---------------------------------------------------------------");
                System.out.println("[OOP Domain Model Verification]");
                System.out.printf("Instantiated %d FoodItem model object(s):\n", items.size());
                for (FoodItem item : items) {
                    System.out.println("  -> " + item);
                }
            }
            System.out.println("===============================================================");
        }
    }

    /**
     * Maps the current row of a ResultSet to a FoodItem OOP instance.
     */
    private static FoodItem mapResultSetToFoodItem(ResultSet rs, ResultSetMetaData rsmd) {
        try {
            FoodItem item = new FoodItem();
            int cols = rsmd.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                String label = rsmd.getColumnLabel(i).toLowerCase();
                String name = rsmd.getColumnName(i).toLowerCase();
                boolean isProvider = label.contains("provider") || name.contains("provider");
                boolean isId = label.contains("id") || name.contains("id");

                if (isProvider && isId) {
                    item.setProviderId(rs.getInt(i));
                } else if (isId && !isProvider) {
                    item.setItemId(rs.getInt(i));
                } else if (label.contains("name") || label.contains("title") || name.contains("name")) {
                    item.setName(rs.getString(i));
                } else if (label.contains("category") || name.contains("category") || label.contains("type")) {
                    item.setCategory(rs.getString(i));
                } else if (label.contains("quantity") || label.contains("qty") || name.contains("quantity")) {
                    item.setQuantity(rs.getDouble(i));
                } else if (label.contains("unit") || name.contains("unit")) {
                    item.setUnit(rs.getString(i));
                } else if (label.contains("expir") || label.contains("expiry") || name.contains("expir")) {
                    item.setExpirationDate(rs.getString(i));
                } else if (label.contains("status") || name.contains("status")) {
                    item.setStatus(rs.getString(i));
                }
            }

            // Fallback: If itemId wasn't assigned (e.g. column 1 is primary key without 'id' in its label),
            // assign from column 1 if numeric.
            if (item.getItemId() == 0 && cols >= 1) {
                try {
                    item.setItemId(rs.getInt(1));
                } catch (Exception ignored) {}
            }

            return item;
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Handles SQL exceptions with informative diagnostics and user guidance.
     */
    private static void handleSQLException(SQLException ex) {
        System.err.println("\n[DATABASE ERROR] A SQLException occurred while connecting to or querying MySQL:");
        System.err.println("  Message   : " + ex.getMessage());
        System.err.println("  SQLState  : " + ex.getSQLState());
        System.err.println("  ErrorCode : " + ex.getErrorCode());
        System.err.println("\nTroubleshooting Guidance:");
        
        switch (ex.getErrorCode()) {
            case 1045: // Access Denied
                System.err.println("  -> Cause: Access denied. MySQL user/password credentials are invalid.");
                System.err.println("  -> Fix  : Set the DB_PASSWORD environment variable with your MySQL password.");
                System.err.println("            PowerShell: $env:DB_PASSWORD=\"your_password\"");
                System.err.println("            CMD       : set DB_PASSWORD=your_password");
                break;
            case 1049: // Unknown database
                System.err.println("  -> Cause: Database 'food_waste_db' was not found.");
                System.err.println("  -> Fix  : Verify database exists or override with $env:DB_NAME=\"your_db_name\".");
                break;
            case 0: // Communications link failure
                System.err.println("  -> Cause: Cannot connect to MySQL server on localhost:3306.");
                System.err.println("  -> Fix  : Ensure MySQL service is running (e.g. via Windows Services or MySQL Workbench).");
                break;
            default:
                System.err.println("  -> Check MySQL server status, network port 3306, and credential environment variables.");
                break;
        }
        System.err.println("===============================================================");
    }
}
