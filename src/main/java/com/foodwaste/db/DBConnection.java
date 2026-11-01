package com.foodwaste.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection provides centralized, secure database connection management
 * for the Food Waste Management System.
 *
 * Credentials and configuration parameters are loaded dynamically from environment
 * variables to avoid hardcoded secrets, complying with secure coding practices:
 * - DB_URL: Full JDBC connection string (optional override)
 * - DB_HOST: Database host (default: localhost)
 * - DB_PORT: Database port (default: 3306)
 * - DB_NAME: Database schema name (default: food_waste_db)
 * - DB_USER / MYSQL_USER: Database username (default: root)
 * - DB_PASSWORD / MYSQL_PWD / MYSQL_PASSWORD: Database user password
 */
public final class DBConnection {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB_NAME = "food_waste_db";
    private static final String DEFAULT_USER = "root";

    // Private constructor prevents direct instantiation (Utility / Factory class pattern)
    private DBConnection() {
        throw novelUnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static UnsupportedOperationException novelUnsupportedOperationException(String msg) {
        return new UnsupportedOperationException(msg);
    }

    static {
        try {
            // Register MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    /**
     * Resolves the JDBC connection URL from environment variables or sensible defaults.
     *
     * @return JDBC URL string
     */
    public static String getJdbcUrl() {
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }

        String host = getEnvOrDefault("DB_HOST", DEFAULT_HOST);
        String port = getEnvOrDefault("DB_PORT", DEFAULT_PORT);
        String dbName = getEnvOrDefault("DB_NAME", DEFAULT_DB_NAME);

        // Parameters:
        // useSSL=false: avoids self-signed SSL handshake failures in local environments
        // allowPublicKeyRetrieval=true: allows RSA public key exchange with MySQL 8.x/9.x sha256_password
        // serverTimezone=UTC: prevents timezone discrepancy errors
        return String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
            host, port, dbName
        );
    }

    /**
     * Resolves the database username from environment variables.
     *
     * @return DB username
     */
    public static String getDbUser() {
        String user = System.getenv("DB_USER");
        if (user == null || user.trim().isEmpty()) {
            user = System.getenv("MYSQL_USER");
        }
        return (user != null && !user.trim().isEmpty()) ? user.trim() : DEFAULT_USER;
    }

    /**
     * Resolves the database password from environment variables.
     * Looks up DB_PASSWORD, then MYSQL_PWD, then MYSQL_PASSWORD.
     *
     * @return DB password or empty string if unset
     */
    public static String getDbPassword() {
        String pass = System.getenv("DB_PASSWORD");
        if (pass == null) {
            pass = System.getenv("MYSQL_PWD");
        }
        if (pass == null) {
            pass = System.getenv("MYSQL_PASSWORD");
        }
        return (pass != null) ? pass : "";
    }

    /**
     * Obtains a new database connection using credentials from environment variables.
     * Callers should manage the lifecycle using try-with-resources.
     *
     * @return java.sql.Connection instance
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        String url = getJdbcUrl();
        String user = getDbUser();
        String password = getDbPassword();

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests whether the database connection can be successfully established.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection Error] Connection failed: " + e.getMessage());
            return false;
        }
    }

    private static String getEnvOrDefault(String varName, String defaultValue) {
        String val = System.getenv(varName);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : defaultValue;
    }

    /**
     * Gracefully shuts down MySQL Connector/J's AbandonedConnectionCleanupThread.
     *
     * In container and plugin environments like Maven exec:java, the host JVM
     * outlives the application execution. The MySQL driver creates a background
     * daemon thread for cleaning up abandoned connections, which remains alive
     * unless explicitly terminated via AbandonedConnectionCleanupThread.checkedShutdown().
     *
     * Calling AbandonedConnectionCleanupThread.checkedShutdown() directly targets and
     * terminates this daemon thread cleanly without calling DriverManager.deregisterDriver(),
     * avoiding driver deregistration side-effects while preventing thread leakage warnings.
     */
    public static void shutdownCleanupThread() {
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception e) {
            System.err.println("[DBConnection] Note: Cleanup thread shutdown: " + e.getMessage());
        }
    }
}
