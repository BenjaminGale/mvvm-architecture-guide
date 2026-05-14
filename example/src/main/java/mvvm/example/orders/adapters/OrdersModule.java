package mvvm.example.orders.adapters;

import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.viewmodel.ViewModelRouter;
import mvvm.example.orders.context.OrderContext;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.CopyOrderService;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.orders.editor.OrderEditorHost;
import mvvm.example.orders.editor.OrderEditorService;
import mvvm.example.orders.editor.OrderEditorView;
import mvvm.example.orders.editor.OrderEditorViewModel;
import mvvm.example.orders.editor.edititem.EditItemRequest;
import mvvm.example.orders.editor.edititem.EditItemView;
import mvvm.example.orders.editor.edititem.EditItemViewModel;
import mvvm.example.orders.explorer.OrdersExplorerHost;
import mvvm.example.orders.explorer.OrdersExplorerView;
import mvvm.example.orders.explorer.OrdersExplorerViewModel;

public class OrdersModule {

    private final OrderRepository orderRepository;
    private final CopyOrderService orderService;
    private final OrderContext orderContext;
    private final ViewModelRouter viewModelRouter;

    public OrdersModule(ViewLocator viewLocator, ViewModelRouter viewModelRouter) {
        this.orderRepository = new InMemoryOrderRepository();
        this.orderService = new CopyOrderService(this.orderRepository);
        this.orderContext = new OrderContext();
        this.viewModelRouter = viewModelRouter;

        viewLocator.register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
        viewLocator.register(OrderEditorViewModel.class, OrderEditorView::new);
        viewLocator.registerDialog(EditItemViewModel.class, EditItemView::dialog);
    }

    public OrderContext orderContext() {
        return orderContext;
    }

    public void routeToOrders() {
        viewModelRouter.dispatch(orders());
    }

    public OrdersExplorerViewModel orders() {
        return new OrdersExplorerViewModel(
            orderRepository::findAll,
            new OrdersExplorerHost() {
                @Override public void showOrderDetails(Order order) { viewModelRouter.dispatch(orderEditor(order)); }
                @Override public void setPendingOrderCount(int count) { orderContext.setCount(count); }
            }
        );
    }

    private OrderEditorViewModel orderEditor(Order order) {
        return new OrderEditorViewModel(
            order,
            new OrderEditorService() {
                @Override public void saveOrder(Order order) { orderRepository.save(order); }
                @Override public Order copyOrder(String orderId) { return orderService.copy(orderId); }
                @Override public void deleteOrder(String orderId) { orderRepository.delete(orderId); }
            },
            new OrderEditorHost() {
                @Override public void returnToList() { viewModelRouter.dispatch(orders()); }
                @Override public void openOrder(Order copied) { viewModelRouter.dispatch(orderEditor(copied)); }
                @Override public void showItemEditor(EditItemRequest request) { viewModelRouter.dispatch(editItem(request)); }
            });
    }

    private EditItemViewModel editItem(EditItemRequest request) {
        return new EditItemViewModel(request);
    }
}
