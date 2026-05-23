package mvvm.example.customers.editor;

import mvvm.example.customers.domain.Customer;

import java.util.UUID;

public interface CustomerEditorService {
    Customer load(UUID id);
    void save(Customer customer);
}
