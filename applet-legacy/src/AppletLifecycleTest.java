import java.lang.reflect.Field;

/**
 * Validates FoodWasteApplet under standard Java Applet lifecycle (init() -> start()).
 * Ensures all dashboard statistics, labels, and text areas reflect actual MySQL data.
 */
public class AppletLifecycleTest {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("  VALIDATING FOOD WASTE APPLET LIFECYCLE & LIVE DATA (JDK 8)");
        System.out.println("===============================================================");

        try {
            FoodWasteApplet applet = new FoodWasteApplet();
            System.out.println("[Step 1] Instantiated FoodWasteApplet.");

            System.out.println("[Step 2] Invoking applet.init()...");
            applet.init();
            System.out.println("         -> init() completed successfully with no exceptions.");

            System.out.println("[Step 3] Invoking applet.start()...");
            applet.start();
            System.out.println("         -> start() completed successfully.");

            System.out.println("[Step 4] Inspecting UI state & database records via reflection:");
            String totalDonations = getFieldValue(applet, "lblDashTotalDonations");
            String activeAvailable = getFieldValue(applet, "lblDashAvailableCount");
            String availableQty   = getFieldValue(applet, "lblDashAvailableQty");
            String providersCount = getFieldValue(applet, "lblDashProvidersCount");
            String statusText     = getFieldValue(applet, "lblStatus");
            String recentText     = getTextAreaValue(applet, "txtDashRecent");

            System.out.println("         - Total Donations Card   : " + totalDonations.trim());
            System.out.println("         - Active Available Card  : " + activeAvailable.trim());
            System.out.println("         - Available Quantity     : " + availableQty.trim());
            System.out.println("         - Providers Registered   : " + providersCount.trim());
            System.out.println("         - Status Bar Message     : " + statusText.trim());

            // ---------------------------------------------------------------
            // FOOD-SAFETY-AWARE ASSERTIONS (as of 2026-10-31):
            //   - Total donations     = 3 (all records)
            //   - Available items     = 1 (only Bread Loaves; Veg Rice & Dal expired)
            //   - Available qty       = 15.0 (Bread Loaves only)
            //   - Providers           = 2
            //   - Recent table shows  all 3 records (for audit / dashboard overview)
            // ---------------------------------------------------------------
            if (!totalDonations.contains("3")) {
                throw new AssertionError("Expected 3 total donations, got: " + totalDonations);
            }
            // After food-safety fix: only non-expired items count as available
            if (!activeAvailable.contains("1")) {
                throw new AssertionError("Expected 1 available item (Bread Loaves only - others expired), got: " + activeAvailable);
            }
            if (!availableQty.contains("15.0")) {
                throw new AssertionError("Expected available qty = 15.0 (Bread Loaves), got: " + availableQty);
            }
            if (!recentText.contains("Vegetable Rice") || !recentText.contains("Bread Loaves") || !recentText.contains("Dal and Roti")) {
                throw new AssertionError("Dashboard recent table missing expected food items! Content:\n" + recentText);
            }

            // Fetch the expired KPI label
            String expiredCount = getFieldValue(applet, "lblDashExpiredCount");
            System.out.println("         - Past Expiry Count     : " + expiredCount.trim());
            System.out.println("\n[Dashboard Recent Inventory Table Content]:\n" + recentText);
            if (!expiredCount.contains("2")) {
                throw new AssertionError("Expected 2 expired items (Veg Rice + Dal & Roti), got: " + expiredCount);
            }

            System.out.println("===============================================================");
            System.out.println("  FOOD-SAFETY VERIFICATION:");
            System.out.println("  - Total Donations      : 3 records in DB (unchanged)");
            System.out.println("  - Available (unexpired) : 1 item  -> Bread Loaves (expiry 2026-10-31)");
            System.out.println("  - Past Expiry           : 2 items -> Veg Rice + Dal and Roti");
            System.out.println("  All 3 records visible in dashboard Recent Activity log.");
            System.out.println("===============================================================");
            System.out.println("  APPLET INITIALIZATION & VERIFICATION 100% SUCCESSFUL!");
            System.out.println("===============================================================");

        } catch (Throwable t) {
            System.err.println("\n[FAILED] Applet lifecycle verification failed:");
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static String getFieldValue(FoodWasteApplet applet, String fieldName) throws Exception {
        Field f = FoodWasteApplet.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        java.awt.Label lbl = (java.awt.Label) f.get(applet);
        return lbl != null ? lbl.getText() : "null";
    }

    private static String getTextAreaValue(FoodWasteApplet applet, String fieldName) throws Exception {
        Field f = FoodWasteApplet.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        java.awt.TextArea txt = (java.awt.TextArea) f.get(applet);
        return txt != null ? txt.getText() : "null";
    }
}
