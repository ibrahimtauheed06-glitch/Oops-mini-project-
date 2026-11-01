import java.util.List;

/**
 * Headless integration test to verify all Applet DAOs and components on JDK 8.
 */
public class AppletIntegrationTest {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("  RUNNING JDK 8 APPLET MODULE JDBC INTEGRATION TEST");
        System.out.println("===============================================================");

        try {
            // 1. Test AppletDB Connection
            System.out.print("[1/6] Testing AppletDB connection... ");
            try (java.sql.Connection conn = AppletDB.getConnection()) {
                if (conn != null && !conn.isClosed()) {
                    System.out.println("SUCCESS (" + conn.getMetaData().getDatabaseProductName() + " " + conn.getMetaData().getDatabaseProductVersion() + ")");
                } else {
                    throw new RuntimeException("Connection is null or closed.");
                }
            }

            // 2. Test ProviderDAO
            System.out.print("[2/6] Testing ProviderDAO.getAllProviders()... ");
            ProviderDAO providerDAO = new ProviderDAO();
            List<Provider> providers = providerDAO.getAllProviders();
            System.out.println("SUCCESS (" + providers.size() + " providers found)");
            for (Provider p : providers) {
                System.out.println("      * ID #" + p.getProviderId() + ": " + p.getProviderName() + " (Phone: " + p.getPhone() + ", Items: " + p.getItemsCount() + ")");
            }

            // 3. Test FoodDAO
            System.out.print("[3/6] Testing FoodDAO.getAllFoodItems() and getAvailableFoodItems()... ");
            FoodDAO foodDAO = new FoodDAO();
            List<FoodItemModel> allFood = foodDAO.getAllFoodItems();
            List<FoodItemModel> availFood = foodDAO.getAvailableFoodItems();
            System.out.println("SUCCESS (Total: " + allFood.size() + ", Available: " + availFood.size() + ")");
            for (FoodItemModel f : allFood) {
                System.out.println("      * ID #" + f.getFoodId() + ": " + f.getFoodName() + " (" + f.getQuantity() + " " + f.getUnit() + ") [" + f.getStatus() + "] Donor: " + f.getProviderName());
            }

            // 4. Test CollectionDAO
            System.out.print("[4/6] Testing CollectionDAO.getAllCollections()... ");
            CollectionDAO collectionDAO = new CollectionDAO();
            List<CollectionRecord> collections = collectionDAO.getAllCollections();
            System.out.println("SUCCESS (" + collections.size() + " collections found)");

            // 5. Test WasteDAO
            System.out.print("[5/6] Testing WasteDAO.getAllWasteRecords()... ");
            WasteDAO wasteDAO = new WasteDAO();
            List<WasteRecord> wasteRecords = wasteDAO.getAllWasteRecords();
            System.out.println("SUCCESS (" + wasteRecords.size() + " waste records found)");

            // 6. Test DashboardDAO
            System.out.print("[6/6] Testing DashboardDAO.getStats()... ");
            DashboardDAO dashboardDAO = new DashboardDAO();
            DashboardStats stats = dashboardDAO.getStats();
            System.out.println("SUCCESS");
            System.out.println("      * Total Donations Registered : " + stats.getTotalFoodItems());
            System.out.println("      * Available Food Items       : " + stats.getAvailableFoodItems());
            System.out.println("      * Total Available Quantity   : " + stats.getTotalAvailableQuantity());
            System.out.println("      * Total Collections Logged   : " + stats.getTotalCollections());
            System.out.println("      * Total Waste Incidents      : " + stats.getTotalWasteRecords());
            System.out.println("      * Registered Providers       : " + stats.getTotalProviders());

            System.out.println("===============================================================");
            System.out.println("  ALL JDK 8 APPLET DATABASE OPERATIONS VERIFIED SUCCESSFULLY!");
            System.out.println("===============================================================");

        } catch (Exception ex) {
            System.err.println("\n[FAILED] Error during verification:");
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
