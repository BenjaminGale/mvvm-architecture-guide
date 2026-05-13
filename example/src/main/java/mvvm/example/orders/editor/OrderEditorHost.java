package mvvm.example.orders.editor;

import mvvm.example.orders.domain.Order;
import mvvm.example.orders.editor.edititem.EditItemSession;

public interface OrderEditorHost {
    void returnToList();
    void openOrder(Order order);
    void showItemEditor(EditItemSession session);
}
