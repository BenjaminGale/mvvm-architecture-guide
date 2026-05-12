package mvvm.example.orders;

import mvvm.example.core.view.ViewRouter;
import mvvm.example.orders.editor.CopyOrderUseCase;
import mvvm.example.orders.editor.DeleteOrderUseCase;
import mvvm.example.orders.editor.OrderEditorUseCases;
import mvvm.example.orders.editor.OrderEditorViewModel;
import mvvm.example.orders.editor.SaveOrderUseCase;
import mvvm.example.orders.editor.edititem.EditItemSession;
import mvvm.example.orders.editor.edititem.EditItemViewModel;

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
            order -> viewRouter.navigateTo(orderEditor(order))
        );
    }

    private OrderEditorViewModel orderEditor(Order order) {
        var vmHolder = new OrderEditorViewModel[1];

        var useCases = new OrderEditorUseCases(
            new SaveOrderUseCase(
                orderService,
                () -> vmHolder[0].buildUpdatedOrder(),
                () -> viewRouter.navigateTo(orders())
            ),
            new CopyOrderUseCase(
                orderService,
                copy -> viewRouter.navigateTo(orderEditor(copy))
            ),
            new DeleteOrderUseCase(
                orderService,
                () -> viewRouter.navigateTo(orders())
            )
        );

        vmHolder[0] = new OrderEditorViewModel(
            order,
            useCases,
            session -> viewRouter.navigateTo(editItem(session))
        );

        return vmHolder[0];
    }

    private EditItemViewModel editItem(EditItemSession session) {
        return new EditItemViewModel(session);
    }
}
