package mvvm.example.customers.editor;

import mvvm.example.customers.domain.Customer;

public interface CustomerEditorService {
    Customer load(String id);
    void save(Customer customer);
}
