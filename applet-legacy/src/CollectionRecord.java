import java.io.Serializable;

/**
 * CollectionRecord represents a food collection / distribution event by an NGO, charity, or volunteer.
 */
public class CollectionRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int collectionId;
    private int foodId;
    private String foodName;
    private String collectorName;
    private double quantityCollected;
    private String unit;
    private String collectionDate;

    public CollectionRecord() {
    }

    public CollectionRecord(int collectionId, int foodId, String foodName, String collectorName,
                            double quantityCollected, String unit, String collectionDate) {
        this.collectionId = collectionId;
        this.foodId = foodId;
        this.foodName = foodName;
        this.collectorName = collectorName;
        this.quantityCollected = quantityCollected;
        this.unit = unit;
        this.collectionDate = collectionDate;
    }

    public int getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(int collectionId) {
        this.collectionId = collectionId;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getCollectorName() {
        return collectorName;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
    }

    public double getQuantityCollected() {
        return quantityCollected;
    }

    public void setQuantityCollected(double quantityCollected) {
        this.quantityCollected = quantityCollected;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(String collectionDate) {
        this.collectionDate = collectionDate;
    }

    @Override
    public String toString() {
        return String.format(
            "Collection #%d: %.1f %s of '%s' collected by %s on %s",
            collectionId, quantityCollected, (unit != null ? unit : ""),
            (foodName != null ? foodName : "Food #" + foodId),
            collectorName, collectionDate
        );
    }
}
