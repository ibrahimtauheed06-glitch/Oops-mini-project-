import java.io.Serializable;

/**
 * FoodItemModel encapsulates a food donation/item in the Food Waste Management System.
 */
public class FoodItemModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int foodId;
    private String foodName;
    private String category;
    private double quantity;
    private String unit;
    private String preparationDate;
    private String expiryDate;
    private String status;
    private int providerId;
    private String providerName;
    private String createdAt;

    public FoodItemModel() {
    }

    public FoodItemModel(int foodId, String foodName, String category, double quantity, String unit,
                         String preparationDate, String expiryDate, String status, int providerId) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.preparationDate = preparationDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.providerId = providerId;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPreparationDate() {
        return preparationDate;
    }

    public void setPreparationDate(String preparationDate) {
        this.preparationDate = preparationDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format(
            "Food[#%d] %s (%.1f %s) - %s [Exp: %s]",
            foodId, foodName, quantity, (unit != null ? unit : ""), status, (expiryDate != null ? expiryDate : "N/A")
        );
    }
}
