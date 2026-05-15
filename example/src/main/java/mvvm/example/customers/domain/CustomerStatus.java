package mvvm.example.customers.domain;

public enum CustomerStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String displayName;

    CustomerStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
