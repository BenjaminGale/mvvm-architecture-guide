package mvvm.example.orders.explorer;

import mvvm.example.orders.editor.OrderEditorRequest;

public interface OrdersExplorerHost {
    void showOrderDetails(OrderEditorRequest request);
}
