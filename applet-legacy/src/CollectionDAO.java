import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Collections. Handles atomic donation collections and inventory updates.
 */
public class CollectionDAO {

    public List<CollectionRecord> getAllCollections() throws SQLException {
        List<CollectionRecord> list = new ArrayList<CollectionRecord>();
        String sql = "SELECT c.collection_id, c.food_id, f.food_name, c.collector_name, "
                   + "       c.quantity_collected, f.unit, c.collection_date "
                   + "FROM collections c "
                   + "JOIN food_items f ON c.food_id = f.food_id "
                   + "ORDER BY c.collection_id DESC";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                CollectionRecord r = new CollectionRecord();
                r.setCollectionId(rs.getInt("collection_id"));
                r.setFoodId(rs.getInt("food_id"));
                r.setFoodName(rs.getString("food_name"));
                r.setCollectorName(rs.getString("collector_name"));
                r.setQuantityCollected(rs.getDouble("quantity_collected"));
                r.setUnit(rs.getString("unit"));
                r.setCollectionDate(rs.getString("collection_date"));
                list.add(r);
            }
        }
        return list;
    }

    /**
     * Atomically records a collection and decrements available inventory.
     */
    public boolean recordCollection(int foodId, String collectorName, double quantityCollected) throws SQLException {
        if (quantityCollected <= 0) {
            throw new SQLException("Collected quantity must be greater than zero.");
        }
        if (collectorName == null || collectorName.trim().isEmpty()) {
            throw new SQLException("Collector / Organization name cannot be blank.");
        }

        String selectSql = "SELECT quantity, status, expiry_date, CURRENT_DATE() AS current_sys_date FROM food_items WHERE food_id = ? FOR UPDATE";
        String insertSql = "INSERT INTO collections (food_id, collector_name, quantity_collected) VALUES (?, ?, ?)";
        String updateSql = "UPDATE food_items SET quantity = ?, status = ? WHERE food_id = ?";

        try (Connection conn = AppletDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                double currentQty = 0;
                String currentStatus = "";
                java.sql.Date expiryDate = null;
                java.sql.Date sysDate = null;

                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, foodId);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Food item #" + foodId + " not found.");
                        }
                        currentQty = rs.getDouble("quantity");
                        currentStatus = rs.getString("status");
                        expiryDate = rs.getDate("expiry_date");
                        sysDate = rs.getDate("current_sys_date");
                    }
                }

                // Food Safety check: Never collect expired food
                if (expiryDate != null && sysDate != null && expiryDate.before(sysDate)) {
                    throw new SQLException(
                        "Food safety alert: Food item #" + foodId + " expired on " + expiryDate + " and cannot be collected for consumption."
                    );
                }

                if (!"AVAILABLE".equals(currentStatus) && !"PARTIALLY_COLLECTED".equals(currentStatus)) {
                    throw new SQLException("Food item is marked as " + currentStatus + " and cannot be collected.");
                }

                if (quantityCollected > currentQty) {
                    throw new SQLException(
                        "Cannot collect " + quantityCollected + ". Only " + currentQty + " remaining in inventory."
                    );
                }

                // Insert into collections
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, foodId);
                    insertStmt.setString(2, collectorName.trim());
                    insertStmt.setDouble(3, quantityCollected);
                    insertStmt.executeUpdate();
                }

                // Update food_items inventory
                double remaining = currentQty - quantityCollected;
                String newStatus = (remaining <= 0.001) ? "COLLECTED" : "PARTIALLY_COLLECTED";

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, Math.max(0.0, remaining));
                    updateStmt.setString(2, newStatus);
                    updateStmt.setInt(3, foodId);
                    updateStmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
