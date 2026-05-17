package mvvm.example.customers.editor;

public record EditCustomerRequest(String customerId, Runnable onSaved) {
}
