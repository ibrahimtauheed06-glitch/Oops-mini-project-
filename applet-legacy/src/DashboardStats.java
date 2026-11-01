import java.io.Serializable;

/**
 * DashboardStats holds aggregated metrics for the executive overview dashboard.
 */
public class DashboardStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private int totalFoodItems;
    private int availableFoodItems;
    private double totalAvailableQuantity;
    private int expiredFoodItems;
    private double totalExpiredQuantity;
    private int totalCollections;
    private double totalQuantityCollected;
    private int totalWasteRecords;
    private double totalQuantityWasted;
    private int totalProviders;

    public DashboardStats() {
    }

    public int getTotalFoodItems() {
        return totalFoodItems;
    }

    public void setTotalFoodItems(int totalFoodItems) {
        this.totalFoodItems = totalFoodItems;
    }

    public int getAvailableFoodItems() {
        return availableFoodItems;
    }

    public void setAvailableFoodItems(int availableFoodItems) {
        this.availableFoodItems = availableFoodItems;
    }

    public double getTotalAvailableQuantity() {
        return totalAvailableQuantity;
    }

    public void setTotalAvailableQuantity(double totalAvailableQuantity) {
        this.totalAvailableQuantity = totalAvailableQuantity;
    }

    public int getExpiredFoodItems() {
        return expiredFoodItems;
    }

    public void setExpiredFoodItems(int expiredFoodItems) {
        this.expiredFoodItems = expiredFoodItems;
    }

    public double getTotalExpiredQuantity() {
        return totalExpiredQuantity;
    }

    public void setTotalExpiredQuantity(double totalExpiredQuantity) {
        this.totalExpiredQuantity = totalExpiredQuantity;
    }

    public int getTotalCollections() {
        return totalCollections;
    }

    public void setTotalCollections(int totalCollections) {
        this.totalCollections = totalCollections;
    }

    public double getTotalQuantityCollected() {
        return totalQuantityCollected;
    }

    public void setTotalQuantityCollected(double totalQuantityCollected) {
        this.totalQuantityCollected = totalQuantityCollected;
    }

    public int getTotalWasteRecords() {
        return totalWasteRecords;
    }

    public void setTotalWasteRecords(int totalWasteRecords) {
        this.totalWasteRecords = totalWasteRecords;
    }

    public double getTotalQuantityWasted() {
        return totalQuantityWasted;
    }

    public void setTotalQuantityWasted(double totalQuantityWasted) {
        this.totalQuantityWasted = totalQuantityWasted;
    }

    public int getTotalProviders() {
        return totalProviders;
    }

    public void setTotalProviders(int totalProviders) {
        this.totalProviders = totalProviders;
    }
}
