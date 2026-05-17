package mvvm.example.orders.editor;

import mvvm.example.orders.editor.lineitems.editor.EditItemRequest;

public interface OrderEditorHost {
    void returnToList();
    void openOrder(EditOrderRequest request);
    void showItemEditor(EditItemRequest request);
}
