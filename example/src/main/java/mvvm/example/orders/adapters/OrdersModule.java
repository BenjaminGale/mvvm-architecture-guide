package mvvm.example.orders.adapters;

import mvvm.example.AppContext;
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
import mvvm.example.orders.editor.header.OrderHeaderView;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsView;
import mvvm.example.orders.editor.lineitems.LineItemsViewModel;
import mvvm.example.orders.explorer.OrdersExplorerHost;
import mvvm.example.orders.explorer.OrdersExplorerView;
import mvvm.example.orders.explorer.OrdersExplorerViewModel;
import mvvm.example.shell.WorkspaceContext;

public class OrdersModule {

    private final AppContext appContext;
    private final WorkspaceContext workspaces;

    private final OrderRepository orderRepository;
    private final CopyOrderService orderService;
    private final OrderContext orderContext;

    public OrdersModule(AppContext appContext, WorkspaceContext workspaces) {
        this.appContext = appContext;
        this.workspaces = workspaces;

        this.orderRepository = new InMemoryOrderRepository();
        this.orderService = new CopyOrderService(this.orderRepository);
        this.orderContext = new OrderContext();

        appContext.viewLocator().register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
        appContext.viewLocator().register(OrderHeaderViewModel.class, OrderHeaderView::new);
        appContext.viewLocator().register(LineItemsViewModel.class, LineItemsView::new);
        appContext.viewLocator().register(OrderEditorViewModel.class, vm -> new OrderEditorView(vm, appContext.viewLocator()));
        appContext.dialogManager().register(EditItemViewModel.class, EditItemView::dialog);
    }

    public OrderContext orderContext() {
        return orderContext;
    }

    public void showExplorer() {
        workspaces.show(this::ordersExplorerViewModel);
    }

    public OrdersExplorerViewModel ordersExplorerViewModel() {
        return new OrdersExplorerViewModel(
            orderRepository::findAll,
            new OrdersExplorerHost() {
                @Override public void showOrderDetails(Order order) { workspaces.show(() -> orderEditorViewModel(order)); }
                @Override public void setPendingOrderCount(int count) { orderContext.setCount(count); }
            },
            workspaces.statusMessages()
        );
    }

    private OrderEditorViewModel orderEditorViewModel(Order order) {
        return new OrderEditorViewModel(
            order,
            new OrderEditorService() {
                @Override public void saveOrder(Order order) { orderRepository.save(order); }
                @Override public Order copyOrder(String orderId) { return orderService.copy(orderId); }
                @Override public void deleteOrder(String orderId) { orderRepository.delete(orderId); }
            },
            new OrderEditorHost() {
                @Override public void returnToList() { workspaces.show(OrdersModule.this::ordersExplorerViewModel); }
                @Override public void openOrder(Order copied) { workspaces.show(() -> orderEditorViewModel(copied)); }
                @Override public void showItemEditor(EditItemRequest request) { appContext.dialogManager().show(editItemViewModel(request)); }
            });
    }

    private EditItemViewModel editItemViewModel(EditItemRequest request) {
        return new EditItemViewModel(request);
    }
}
