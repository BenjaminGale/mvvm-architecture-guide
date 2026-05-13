package mvvm.example.orders.adapters;

import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.viewmodel.ViewModelRouter;
import mvvm.example.orders.context.OrderContext;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;
import mvvm.example.orders.editor.OrderEditorHost;
import mvvm.example.orders.editor.OrderEditorView;
import mvvm.example.orders.editor.OrderEditorViewModel;
import mvvm.example.orders.editor.edititem.EditItemSession;
import mvvm.example.orders.editor.edititem.EditItemView;
import mvvm.example.orders.editor.edititem.EditItemViewModel;
import mvvm.example.orders.explorer.OrdersExplorerView;
import mvvm.example.orders.explorer.OrdersExplorerViewModel;

public class OrderModule {

    private final OrderService orderService;
    private final OrderContext orderContext;
    private final ViewModelRouter viewModelRouter;

    public OrderModule(ViewLocator viewLocator, ViewModelRouter viewModelRouter) {
        this.orderService = new OrderService(new InMemoryOrderRepository());
        this.orderContext  = new OrderContext();
        this.viewModelRouter = viewModelRouter;

        viewLocator.register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
        viewLocator.register(OrderEditorViewModel.class, OrderEditorView::new);
        viewLocator.register(EditItemViewModel.class, EditItemView::new);
    }

    public OrderContext orderContext() {
        return orderContext;
    }

    public void routeToOrders() {
        viewModelRouter.dispatch(orders());
    }

    public OrdersExplorerViewModel orders() {
        return new OrdersExplorerViewModel(
            orderService,
            orderContext,
            order -> viewModelRouter.dispatch(orderEditor(order))
        );
    }

    private OrderEditorViewModel orderEditor(Order order) {
        var host = new OrderEditorHost() {
            @Override public void returnToList()                  { viewModelRouter.dispatch(orders()); }
            @Override public void openOrder(Order copied)         { viewModelRouter.dispatch(orderEditor(copied)); }
            @Override public void showItemEditor(EditItemSession s){ viewModelRouter.dispatch(editItem(s)); }
        };

        return new OrderEditorViewModel(order, orderService, host);
    }

    private EditItemViewModel editItem(EditItemSession session) {
        return new EditItemViewModel(session);
    }
}
