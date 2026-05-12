package mvvm.example.orders.adapters;

import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.ViewRouter;
import mvvm.example.orders.context.OrderContext;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;
import mvvm.example.orders.editor.usecases.CopyOrderUseCase;
import mvvm.example.orders.editor.usecases.DeleteOrderUseCase;
import mvvm.example.orders.editor.usecases.OrderEditorUseCases;
import mvvm.example.orders.editor.OrderEditorView;
import mvvm.example.orders.editor.OrderEditorViewModel;
import mvvm.example.orders.editor.usecases.SaveOrderUseCase;
import mvvm.example.orders.editor.edititem.EditItemSession;
import mvvm.example.orders.editor.edititem.EditItemView;
import mvvm.example.orders.editor.edititem.EditItemViewModel;
import mvvm.example.orders.explorer.OrdersExplorerView;
import mvvm.example.orders.explorer.OrdersViewModel;

public class OrderModule {

    private final OrderService orderService;
    private final OrderContext orderContext;
    private final ViewRouter viewRouter;

    public OrderModule(ViewLocator viewLocator, ViewRouter viewRouter) {
        this.orderService = new OrderService(new InMemoryOrderRepository());
        this.orderContext  = new OrderContext();
        this.viewRouter    = viewRouter;

        viewLocator.register(OrdersViewModel.class,      OrdersExplorerView::new);
        viewLocator.register(OrderEditorViewModel.class, OrderEditorView::new);
        viewLocator.register(EditItemViewModel.class,    EditItemView::new);
    }

    public OrderContext orderContext() {
        return orderContext;
    }

    public void routeToOrders() {
        viewRouter.route(orders());
    }

    public OrdersViewModel orders() {
        return new OrdersViewModel(
            orderService::fetchAll,
            orderContext,
            order -> viewRouter.route(orderEditor(order))
        );
    }

    private OrderEditorViewModel orderEditor(Order order) {
        var useCases = new OrderEditorUseCases(
            new SaveOrderUseCase(
                orderService,
                () -> viewRouter.route(orders())
            ),
            new CopyOrderUseCase(
                orderService,
                copy -> viewRouter.route(orderEditor(copy))
            ),
            new DeleteOrderUseCase(
                orderService,
                () -> viewRouter.route(orders())
            )
        );

        return new OrderEditorViewModel(
            order,
            useCases,
            session -> viewRouter.route(editItem(session))
        );
    }

    private EditItemViewModel editItem(EditItemSession session) {
        return new EditItemViewModel(session);
    }
}
