import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Waste Records. Handles food spoilage / expiration logging.
 */
public class WasteDAO {

    public List<WasteRecord> getAllWasteRecords() throws SQLException {
        List<WasteRecord> list = new ArrayList<WasteRecord>();
        String sql = "SELECT w.waste_id, w.food_id, f.food_name, w.quantity_wasted, "
                   + "       f.unit, w.reason, w.waste_date "
                   + "FROM waste_records w "
                   + "JOIN food_items f ON w.food_id = f.food_id "
                   + "ORDER BY w.waste_id DESC";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                WasteRecord r = new WasteRecord();
                r.setWasteId(rs.getInt("waste_id"));
                r.setFoodId(rs.getInt("food_id"));
                r.setFoodName(rs.getString("food_name"));
                r.setQuantityWasted(rs.getDouble("quantity_wasted"));
                r.setUnit(rs.getString("unit"));
                r.setReason(rs.getString("reason"));
                r.setWasteDate(rs.getString("waste_date"));
                list.add(r);
            }
        }
        return list;
    }

    /**
     * Atomically logs wasted food and updates the remaining food item status and quantity.
     */
    public boolean recordWaste(int foodId, double quantityWasted, String reason) throws SQLException {
        if (quantityWasted <= 0) {
            throw new SQLException("Wasted quantity must be greater than zero.");
        }

        String selectSql = "SELECT quantity, status FROM food_items WHERE food_id = ? FOR UPDATE";
        String insertSql = "INSERT INTO waste_records (food_id, quantity_wasted, reason) VALUES (?, ?, ?)";
        String updateSql = "UPDATE food_items SET quantity = ?, status = ? WHERE food_id = ?";

        try (Connection conn = AppletDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                double currentQty = 0;
                String currentStatus = "";

                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, foodId);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Food item #" + foodId + " not found.");
                        }
                        currentQty = rs.getDouble("quantity");
                        currentStatus = rs.getString("status");
                    }
                }

                if (quantityWasted > currentQty && currentQty > 0) {
                    throw new SQLException(
                        "Cannot waste " + quantityWasted + ". Only " + currentQty + " recorded in inventory."
                    );
                }

                // Insert into waste_records
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, foodId);
                    insertStmt.setDouble(2, quantityWasted);
                    insertStmt.setString(3, (reason != null && !reason.trim().isEmpty()) ? reason.trim() : "Expired/Spoiled");
                    insertStmt.executeUpdate();
                }

                // Update food_items
                double remaining = Math.max(0.0, currentQty - quantityWasted);
                String newStatus;
                if (remaining <= 0.001) {
                    if (reason != null && reason.toLowerCase().contains("expir")) {
                        newStatus = "EXPIRED";
                    } else {
                        newStatus = "WASTED";
                    }
                } else {
                    newStatus = currentStatus;
                }

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, remaining);
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
