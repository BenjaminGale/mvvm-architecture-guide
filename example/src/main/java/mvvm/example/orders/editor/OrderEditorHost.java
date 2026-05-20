package mvvm.example.orders.editor;

import mvvm.example.orders.editor.lineitems.EditItemRequest;
import mvvm.example.orders.editor.header.SelectCustomerRequest;

public interface OrderEditorHost {
    void returnToList();
    void openOrder(EditOrderRequest request);
    void showItemEditor(EditItemRequest request);
    void showCustomerSelector(SelectCustomerRequest request);
}
