package mvvm.example.customers.explorer;

import mvvm.example.customers.editor.CustomerEditorRequest;

public interface CustomerExplorerHost {
    void editCustomer(CustomerEditorRequest request);
}
