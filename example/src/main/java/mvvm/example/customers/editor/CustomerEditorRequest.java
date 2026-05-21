package mvvm.example.customers.editor;

public record CustomerEditorRequest(String customerId, boolean isNew, Runnable onSaved) {

    public static CustomerEditorRequest forCustomer(String customerId, Runnable onSaved) {
        return new CustomerEditorRequest(customerId, false, onSaved);
    }

    public static CustomerEditorRequest newCustomer(Runnable onSaved) {
        return new CustomerEditorRequest(null, true, onSaved);
    }
}
