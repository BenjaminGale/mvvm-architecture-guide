package mvvm.example.orders.editor;

import mvvm.example.orders.editor.header.SelectCustomerRequest;

public interface OrderEditorHost {
    void returnToList();
    void openOrder(EditOrderRequest request);
    void showCustomerSelector(SelectCustomerRequest request);
}
