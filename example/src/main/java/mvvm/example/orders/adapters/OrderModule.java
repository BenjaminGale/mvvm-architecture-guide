package mvvm.example.orders.adapters;

import mvvm.example.core.view.ViewRouter;
import mvvm.example.orders.context.OrderContext;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;
import mvvm.example.orders.editor.CopyOrderUseCase;
import mvvm.example.orders.editor.DeleteOrderUseCase;
import mvvm.example.orders.editor.OrderEditorUseCases;
import mvvm.example.orders.editor.OrderEditorViewModel;
import mvvm.example.orders.editor.SaveOrderUseCase;
import mvvm.example.orders.editor.edititem.EditItemSession;
import mvvm.example.orders.editor.edititem.EditItemViewModel;
import mvvm.example.orders.explorer.OrdersViewModel;

public class OrderModule {

    private final OrderService orderService;
    private final OrderContext orderContext;
    private final ViewRouter viewRouter;

    public OrderModule(OrderService orderService, OrderContext orderContext, ViewRouter viewRouter) {
        this.orderService = orderService;
        this.orderContext = orderContext;
        this.viewRouter = viewRouter;
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
