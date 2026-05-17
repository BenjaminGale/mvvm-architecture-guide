package mvvm.example.customers.explorer;

import mvvm.example.customers.requests.EditCustomerRequest;

public interface CustomerExplorerHost {
    void editCustomer(EditCustomerRequest request);
}
