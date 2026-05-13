package mvvm.example.orders.adapters;

import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.ViewRouter;
import mvvm.example.orders.context.OrderContext;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;
import mvvm.example.orders.editor.OrderEditorRequests;
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
    private final ViewRouter viewRouter;

    public OrderModule(ViewLocator viewLocator, ViewRouter viewRouter) {
        this.orderService = new OrderService(new InMemoryOrderRepository());
        this.orderContext  = new OrderContext();
        this.viewRouter    = viewRouter;

        viewLocator.register(OrdersExplorerViewModel.class,      OrdersExplorerView::new);
        viewLocator.register(OrderEditorViewModel.class, OrderEditorView::new);
        viewLocator.register(EditItemViewModel.class,    EditItemView::new);
    }

    public OrderContext orderContext() {
        return orderContext;
    }

    public void routeToOrders() {
        viewRouter.route(orders());
    }

    public OrdersExplorerViewModel orders() {
        return new OrdersExplorerViewModel(
            orderService,
            orderContext,
            order -> viewRouter.route(orderEditor(order))
        );
    }

    private OrderEditorViewModel orderEditor(Order order) {
        var requests = new OrderEditorRequests(
            () -> viewRouter.route(orders()),
            copy -> viewRouter.route(orderEditor(copy)),
            () -> viewRouter.route(orders())
        );

        return new OrderEditorViewModel(
            order,
            orderService,
            requests,
            session -> viewRouter.route(editItem(session))
        );
    }

    private EditItemViewModel editItem(EditItemSession session) {
        return new EditItemViewModel(session);
    }
}
