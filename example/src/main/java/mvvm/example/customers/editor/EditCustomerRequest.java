package mvvm.example.customers.editor;

public record EditCustomerRequest(String customerId, boolean isNew, Runnable onSaved) {

    public static EditCustomerRequest forCustomer(String customerId, Runnable onSaved) {
        return new EditCustomerRequest(customerId, false, onSaved);
    }

    public static EditCustomerRequest newCustomer(Runnable onSaved) {
        return new EditCustomerRequest(null, true, onSaved);
    }
}
