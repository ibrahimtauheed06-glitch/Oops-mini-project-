import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Provider entities.
 */
public class ProviderDAO {

    public List<Provider> getAllProviders() throws SQLException {
        List<Provider> list = new ArrayList<Provider>();
        String sql = "SELECT p.provider_id, p.provider_name, p.phone, p.address, p.created_at, "
                   + "       COUNT(f.food_id) AS items_count "
                   + "FROM providers p "
                   + "LEFT JOIN food_items f ON p.provider_id = f.provider_id "
                   + "GROUP BY p.provider_id, p.provider_name, p.phone, p.address, p.created_at "
                   + "ORDER BY p.provider_id";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Provider p = new Provider();
                p.setProviderId(rs.getInt("provider_id"));
                p.setProviderName(rs.getString("provider_name"));
                p.setPhone(rs.getString("phone"));
                p.setAddress(rs.getString("address"));
                p.setCreatedAt(rs.getString("created_at"));
                p.setItemsCount(rs.getInt("items_count"));
                list.add(p);
            }
        }
        return list;
    }

    public Provider getProviderById(int providerId) throws SQLException {
        String sql = "SELECT provider_id, provider_name, phone, address, created_at "
                   + "FROM providers WHERE provider_id = ?";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, providerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Provider p = new Provider();
                    p.setProviderId(rs.getInt("provider_id"));
                    p.setProviderName(rs.getString("provider_name"));
                    p.setPhone(rs.getString("phone"));
                    p.setAddress(rs.getString("address"));
                    p.setCreatedAt(rs.getString("created_at"));
                    return p;
                }
            }
        }
        return null;
    }

    public boolean addProvider(String name, String phone, String address) throws SQLException {
        String sql = "INSERT INTO providers (provider_name, phone, address) VALUES (?, ?, ?)";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            stmt.setString(2, (phone != null && !phone.trim().isEmpty()) ? phone.trim() : null);
            stmt.setString(3, (address != null && !address.trim().isEmpty()) ? address.trim() : null);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateProvider(int id, String name, String phone, String address) throws SQLException {
        String sql = "UPDATE providers SET provider_name = ?, phone = ?, address = ? WHERE provider_id = ?";

        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            stmt.setString(2, (phone != null && !phone.trim().isEmpty()) ? phone.trim() : null);
            stmt.setString(3, (address != null && !address.trim().isEmpty()) ? address.trim() : null);
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteProvider(int id) throws SQLException {
        if (hasAssociatedFood(id)) {
            throw new SQLException("Cannot delete provider: Provider ID #" + id + " has associated food donation records.");
        }

        String sql = "DELETE FROM providers WHERE provider_id = ?";
        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean hasAssociatedFood(int providerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM food_items WHERE provider_id = ?";
        try (Connection conn = AppletDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, providerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
