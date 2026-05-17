package mvvm.example.orders.editor;

import mvvm.example.orders.requests.EditItemRequest;
import mvvm.example.orders.requests.EditOrderRequest;

public interface OrderEditorHost {
    void returnToList();
    void openOrder(EditOrderRequest request);
    void showItemEditor(EditItemRequest request);
}
