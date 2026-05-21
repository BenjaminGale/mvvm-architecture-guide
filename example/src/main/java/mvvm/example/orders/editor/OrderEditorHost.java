package mvvm.example.orders.editor;

public interface OrderEditorHost {
    void returnToList();
    void openOrder(OrderEditorRequest request);
}
