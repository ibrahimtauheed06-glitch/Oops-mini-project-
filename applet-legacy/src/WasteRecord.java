import java.io.Serializable;

/**
 * WasteRecord encapsulates a logged waste occurrence (e.g. food expired, spoiled, or damaged).
 */
public class WasteRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int wasteId;
    private int foodId;
    private String foodName;
    private double quantityWasted;
    private String unit;
    private String reason;
    private String wasteDate;

    public WasteRecord() {
    }

    public WasteRecord(int wasteId, int foodId, String foodName, double quantityWasted,
                       String unit, String reason, String wasteDate) {
        this.wasteId = wasteId;
        this.foodId = foodId;
        this.foodName = foodName;
        this.quantityWasted = quantityWasted;
        this.unit = unit;
        this.reason = reason;
        this.wasteDate = wasteDate;
    }

    public int getWasteId() {
        return wasteId;
    }

    public void setWasteId(int wasteId) {
        this.wasteId = wasteId;
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

    public double getQuantityWasted() {
        return quantityWasted;
    }

    public void setQuantityWasted(double quantityWasted) {
        this.quantityWasted = quantityWasted;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getWasteDate() {
        return wasteDate;
    }

    public void setWasteDate(String wasteDate) {
        this.wasteDate = wasteDate;
    }

    @Override
    public String toString() {
        return String.format(
            "Waste #%d: %.1f %s of '%s' wasted (Reason: %s) on %s",
            wasteId, quantityWasted, (unit != null ? unit : ""),
            (foodName != null ? foodName : "Food #" + foodId),
            (reason != null ? reason : "Unspecified"), wasteDate
        );
    }
}
