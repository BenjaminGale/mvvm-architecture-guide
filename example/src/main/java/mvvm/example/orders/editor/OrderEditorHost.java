package mvvm.example.orders.editor;

import mvvm.example.orders.editor.edititem.EditItemRequest;

public interface OrderEditorHost {
    void returnToList();
    void openOrder(EditOrderRequest request);
    void showItemEditor(EditItemRequest request);
}
