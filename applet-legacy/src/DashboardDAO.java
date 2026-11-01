import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for Dashboard analytics and aggregate statistics.
 */
public class DashboardDAO {

    public DashboardStats getStats() throws SQLException {
        DashboardStats stats = new DashboardStats();

        String foodSql = "SELECT COUNT(*) AS total_items, "
                       + "       COALESCE(SUM(CASE WHEN status IN ('AVAILABLE', 'PARTIALLY_COLLECTED') AND expiry_date >= CURRENT_DATE() THEN 1 ELSE 0 END), 0) AS avail_items, "
                       + "       COALESCE(SUM(CASE WHEN status IN ('AVAILABLE', 'PARTIALLY_COLLECTED') AND expiry_date >= CURRENT_DATE() THEN quantity ELSE 0 END), 0) AS avail_qty, "
                       + "       COALESCE(SUM(CASE WHEN status = 'EXPIRED' OR (status IN ('AVAILABLE', 'PARTIALLY_COLLECTED') AND expiry_date < CURRENT_DATE()) THEN 1 ELSE 0 END), 0) AS expired_items, "
                       + "       COALESCE(SUM(CASE WHEN status = 'EXPIRED' OR (status IN ('AVAILABLE', 'PARTIALLY_COLLECTED') AND expiry_date < CURRENT_DATE()) THEN quantity ELSE 0 END), 0) AS expired_qty "
                       + "FROM food_items";

        String collSql = "SELECT COUNT(*) AS total_coll, "
                       + "       COALESCE(SUM(quantity_collected), 0) AS total_collected_qty "
                       + "FROM collections";

        String wasteSql = "SELECT COUNT(*) AS total_waste, "
                        + "       COALESCE(SUM(quantity_wasted), 0) AS total_wasted_qty "
                        + "FROM waste_records";

        String provSql = "SELECT COUNT(*) AS total_prov FROM providers";

        try (Connection conn = AppletDB.getConnection()) {
            // Food items
            try (PreparedStatement stmt = conn.prepareStatement(foodSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalFoodItems(rs.getInt("total_items"));
                    stats.setAvailableFoodItems(rs.getInt("avail_items"));
                    stats.setTotalAvailableQuantity(rs.getDouble("avail_qty"));
                    stats.setExpiredFoodItems(rs.getInt("expired_items"));
                    stats.setTotalExpiredQuantity(rs.getDouble("expired_qty"));
                }
            }

            // Collections
            try (PreparedStatement stmt = conn.prepareStatement(collSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalCollections(rs.getInt("total_coll"));
                    stats.setTotalQuantityCollected(rs.getDouble("total_collected_qty"));
                }
            }

            // Waste
            try (PreparedStatement stmt = conn.prepareStatement(wasteSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalWasteRecords(rs.getInt("total_waste"));
                    stats.setTotalQuantityWasted(rs.getDouble("total_wasted_qty"));
                }
            }

            // Providers
            try (PreparedStatement stmt = conn.prepareStatement(provSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalProviders(rs.getInt("total_prov"));
                }
            }
        }

        return stats;
    }
}
