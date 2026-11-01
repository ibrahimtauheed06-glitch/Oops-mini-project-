import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * AppletDB manages database connectivity for the Food Waste Management System Applet.
 *
 * Implements secure credential retrieval:
 * 1. Reads environment variables (DB_USER, DB_PASSWORD, DB_HOST, DB_PORT, DB_NAME).
 * 2. Allows runtime credential overrides from the Applet UI settings tab.
 * 3. Uses standard try-with-resources and PreparedStatement across all caller classes.
 * 4. Compatible with JDK 8 and MySQL Connector/J 8.0.33.
 */
public final class AppletDB {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB_NAME = "food_waste_db";
    private static final String DEFAULT_USER = "root";

    private static String customHost = null;
    private static String customPort = null;
    private static String customDbName = null;
    private static String customUser = null;
    private static String customPassword = null;

    private AppletDB() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Throwable e) {
            System.err.println("[AppletDB] MySQL Driver initialization notice: " + e.getMessage());
        }
    }

    public static String getHost() {
        if (customHost != null && !customHost.trim().isEmpty()) {
            return customHost.trim();
        }
        String env = readEnvSafe("DB_HOST");
        return (env != null && !env.trim().isEmpty()) ? env.trim() : DEFAULT_HOST;
    }

    public static String getPort() {
        if (customPort != null && !customPort.trim().isEmpty()) {
            return customPort.trim();
        }
        String env = readEnvSafe("DB_PORT");
        return (env != null && !env.trim().isEmpty()) ? env.trim() : DEFAULT_PORT;
    }

    public static String getDbName() {
        if (customDbName != null && !customDbName.trim().isEmpty()) {
            return customDbName.trim();
        }
        String env = readEnvSafe("DB_NAME");
        return (env != null && !env.trim().isEmpty()) ? env.trim() : DEFAULT_DB_NAME;
    }

    public static String getUser() {
        if (customUser != null && !customUser.trim().isEmpty()) {
            return customUser.trim();
        }
        String env = readEnvSafe("DB_USER");
        if (env == null || env.trim().isEmpty()) {
            env = readEnvSafe("MYSQL_USER");
        }
        return (env != null && !env.trim().isEmpty()) ? env.trim() : DEFAULT_USER;
    }

    public static String getPassword() {
        if (customPassword != null) {
            return customPassword;
        }
        String env = readEnvSafe("DB_PASSWORD");
        if (env == null) {
            env = readEnvSafe("MYSQL_PWD");
        }
        if (env == null) {
            env = readEnvSafe("MYSQL_PASSWORD");
        }
        // If no environment variable is set, default to "root" or empty string
        return (env != null) ? env : "root";
    }

    public static void setCredentials(String host, String port, String dbName, String user, String password) {
        customHost = host;
        customPort = port;
        customDbName = dbName;
        customUser = user;
        customPassword = password;
    }

    public static String getJdbcUrl() {
        String envUrl = readEnvSafe("DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }
        return String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
            getHost(), getPort(), getDbName()
        );
    }

    /**
     * Obtains a new database connection.
     * Callers must wrap in try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getJdbcUrl(), getUser(), getPassword());
    }

    /**
     * Safely reads an environment variable without crashing if an Applet security sandbox restricts access.
     */
    private static String readEnvSafe(String name) {
        try {
            return System.getenv(name);
        } catch (SecurityException se) {
            return null;
        }
    }
}
