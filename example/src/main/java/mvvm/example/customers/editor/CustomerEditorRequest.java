package mvvm.example.customers.editor;

import java.util.UUID;

public record CustomerEditorRequest(UUID customerId, boolean isNew, Runnable onSaved) {

    public static CustomerEditorRequest forCustomer(UUID customerId, Runnable onSaved) {
        return new CustomerEditorRequest(customerId, false, onSaved);
    }

    public static CustomerEditorRequest newCustomer(Runnable onSaved) {
        return new CustomerEditorRequest(null, true, onSaved);
    }
}
