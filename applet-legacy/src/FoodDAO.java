import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Food Item entities.
 */
public class FoodDAO {

    public List<FoodItemModel> getAllFoodItems() throws SQLException {
        List<FoodItemModel> list = new ArrayList<FoodItemModel>();
        String sql = "SELECT f.food_id, f.food_name, f.category, f.quantity, f.unit, "
                   + "       f.preparation_date, f.expiry_date, f.status, f.provider_id, "
                   + "       p.provider_name, f.created_at "
                   + "FROM food_items f "
                   + "JOIN providers p ON f.provider_id = p.provider_id "
                   + "ORDER BY f.food_id DESC";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<FoodItemModel> getAvailableFoodItems() throws SQLException {
        List<FoodItemModel> list = new ArrayList<FoodItemModel>();
        String sql = "SELECT f.food_id, f.food_name, f.category, f.quantity, f.unit, "
                   + "       f.preparation_date, f.expiry_date, f.status, f.provider_id, "
                   + "       p.provider_name, f.created_at "
                   + "FROM food_items f "
                   + "JOIN providers p ON f.provider_id = p.provider_id "
                   + "WHERE f.status IN ('AVAILABLE', 'PARTIALLY_COLLECTED') "
                   + "  AND f.quantity > 0 "
                   + "  AND f.expiry_date >= CURRENT_DATE() "
                   + "ORDER BY f.expiry_date ASC, f.food_id ASC";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<FoodItemModel> getExpiredFoodItems() throws SQLException {
        List<FoodItemModel> list = new ArrayList<FoodItemModel>();
        String sql = "SELECT f.food_id, f.food_name, f.category, f.quantity, f.unit, "
                   + "       f.preparation_date, f.expiry_date, f.status, f.provider_id, "
                   + "       p.provider_name, f.created_at "
                   + "FROM food_items f "
                   + "JOIN providers p ON f.provider_id = p.provider_id "
                   + "WHERE (f.status = 'EXPIRED' OR (f.status IN ('AVAILABLE', 'PARTIALLY_COLLECTED') AND f.expiry_date < CURRENT_DATE())) "
                   + "ORDER BY f.expiry_date DESC, f.food_id ASC";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public FoodItemModel getFoodItemById(int foodId) throws SQLException {
        String sql = "SELECT f.food_id, f.food_name, f.category, f.quantity, f.unit, "
                   + "       f.preparation_date, f.expiry_date, f.status, f.provider_id, "
                   + "       p.provider_name, f.created_at "
                   + "FROM food_items f "
                   + "JOIN providers p ON f.provider_id = p.provider_id "
                   + "WHERE f.food_id = ?";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, foodId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean addFoodItem(FoodItemModel item) throws SQLException {
        String sql = "INSERT INTO food_items (food_name, category, quantity, unit, "
                   + "                      preparation_date, expiry_date, status, provider_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getFoodName().trim());
            stmt.setString(2, (item.getCategory() != null) ? item.getCategory().trim() : "Other");
            stmt.setDouble(3, item.getQuantity());
            stmt.setString(4, (item.getUnit() != null) ? item.getUnit().trim() : "servings");

            if (item.getPreparationDate() != null && !item.getPreparationDate().trim().isEmpty()) {
                stmt.setDate(5, java.sql.Date.valueOf(item.getPreparationDate().trim()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.setDate(6, java.sql.Date.valueOf(item.getExpiryDate().trim()));
            stmt.setString(7, (item.getStatus() != null) ? item.getStatus().trim() : "AVAILABLE");
            stmt.setInt(8, item.getProviderId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateFoodItem(FoodItemModel item) throws SQLException {
        String sql = "UPDATE food_items SET food_name = ?, category = ?, quantity = ?, unit = ?, "
                   + "                     preparation_date = ?, expiry_date = ?, status = ?, provider_id = ? "
                   + "WHERE food_id = ?";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getFoodName().trim());
            stmt.setString(2, item.getCategory().trim());
            stmt.setDouble(3, item.getQuantity());
            stmt.setString(4, item.getUnit().trim());

            if (item.getPreparationDate() != null && !item.getPreparationDate().trim().isEmpty()) {
                stmt.setDate(5, java.sql.Date.valueOf(item.getPreparationDate().trim()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.setDate(6, java.sql.Date.valueOf(item.getExpiryDate().trim()));
            stmt.setString(7, item.getStatus().trim());
            stmt.setInt(8, item.getProviderId());
            stmt.setInt(9, item.getFoodId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteFoodItem(int foodId) throws SQLException {
        if (hasChildRecords(foodId)) {
            throw new SQLException(
                "Cannot delete food item #" + foodId + ": It has associated collection or waste tracking records.\n"
                + "Audit history must be maintained for accountability."
            );
        }

        String sql = "DELETE FROM food_items WHERE food_id = ?";
        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, foodId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean hasChildRecords(int foodId) throws SQLException {
        String sqlColl = "SELECT COUNT(*) FROM collections WHERE food_id = ?";
        String sqlWaste = "SELECT COUNT(*) FROM waste_records WHERE food_id = ?";

        try (Connection conn = AppletDB.getConnection()) {
            try (PreparedStatement stmt1 = conn.prepareStatement(sqlColl)) {
                stmt1.setInt(1, foodId);
                try (ResultSet rs1 = stmt1.executeQuery()) {
                    if (rs1.next() && rs1.getInt(1) > 0) return true;
                }
            }
            try (PreparedStatement stmt2 = conn.prepareStatement(sqlWaste)) {
                stmt2.setInt(1, foodId);
                try (ResultSet rs2 = stmt2.executeQuery()) {
                    if (rs2.next() && rs2.getInt(1) > 0) return true;
                }
            }
        }
        return false;
    }

    public List<FoodItemModel> searchAndFilter(String keyword, String category, String status, int providerId) throws SQLException {
        List<FoodItemModel> list = new ArrayList<FoodItemModel>();
        StringBuilder sql = new StringBuilder(
            "SELECT f.food_id, f.food_name, f.category, f.quantity, f.unit, "
            + "     f.preparation_date, f.expiry_date, f.status, f.provider_id, "
            + "     p.provider_name, f.created_at "
            + "FROM food_items f "
            + "JOIN providers p ON f.provider_id = p.provider_id WHERE 1=1 "
        );

        List<Object> params = new ArrayList<Object>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(f.food_name) LIKE ? OR LOWER(p.provider_name) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if (category != null && !category.equalsIgnoreCase("ALL") && !category.trim().isEmpty()) {
            sql.append(" AND f.category = ? ");
            params.add(category.trim());
        }

        if (status != null && !status.equalsIgnoreCase("ALL") && !status.trim().isEmpty()) {
            if ("AVAILABLE".equalsIgnoreCase(status.trim())) {
                sql.append(" AND f.status IN ('AVAILABLE', 'PARTIALLY_COLLECTED') AND f.expiry_date >= CURRENT_DATE() ");
            } else if ("EXPIRED".equalsIgnoreCase(status.trim())) {
                sql.append(" AND (f.status = 'EXPIRED' OR (f.status IN ('AVAILABLE', 'PARTIALLY_COLLECTED') AND f.expiry_date < CURRENT_DATE())) ");
            } else {
                sql.append(" AND f.status = ? ");
                params.add(status.trim());
            }
        }

        if (providerId > 0) {
            sql.append(" AND f.provider_id = ? ");
            params.add(providerId);
        }

        sql.append(" ORDER BY f.food_id DESC");

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) p);
                } else {
                    stmt.setString(i + 1, (String) p);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private FoodItemModel mapRow(ResultSet rs) throws SQLException {
        FoodItemModel m = new FoodItemModel();
        m.setFoodId(rs.getInt("food_id"));
        m.setFoodName(rs.getString("food_name"));
        m.setCategory(rs.getString("category"));
        m.setQuantity(rs.getDouble("quantity"));
        m.setUnit(rs.getString("unit"));
        m.setPreparationDate(rs.getString("preparation_date"));
        m.setExpiryDate(rs.getString("expiry_date"));
        m.setStatus(rs.getString("status"));
        m.setProviderId(rs.getInt("provider_id"));
        m.setProviderName(rs.getString("provider_name"));
        m.setCreatedAt(rs.getString("created_at"));
        return m;
    }
}
