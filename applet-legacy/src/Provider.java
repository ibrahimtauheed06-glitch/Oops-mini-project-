import java.io.Serializable;

/**
 * Provider represents a food donor / business entity (e.g., Canteen, Bakery, Restaurant, Supermarket).
 */
public class Provider implements Serializable {
    private static final long serialVersionUID = 1L;

    private int providerId;
    private String providerName;
    private String phone;
    private String address;
    private String createdAt;
    private int itemsCount;

    public Provider() {
    }

    public Provider(int providerId, String providerName, String phone, String address, String createdAt) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s)", providerId, providerName, (phone != null ? phone : "No phone"));
    }
}
