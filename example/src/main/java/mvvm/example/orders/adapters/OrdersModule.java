package mvvm.example.orders.adapters;

import mvvm.example.core.view.ViewServices;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;
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
import mvvm.example.shell.ShellContext;

public class OrdersModule {

    private final ViewServices view;
    private final ShellContext shell;

    private final OrderRepository orderRepository;
    private final CopyOrderService orderService;
    private final OrderContext orderContext;

    public OrdersModule(ViewServices view, ShellContext shell) {
        this.view = view;
        this.shell = shell;

        this.orderRepository = new InMemoryOrderRepository();
        this.orderService = new CopyOrderService(this.orderRepository);
        this.orderContext = new OrderContext();

        view.viewLocator().register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
        view.viewLocator().register(OrderHeaderViewModel.class, OrderHeaderView::new);
        view.viewLocator().register(LineItemsViewModel.class, LineItemsView::new);
        view.viewLocator().register(OrderEditorViewModel.class, vm -> new OrderEditorView(vm, view.viewLocator()));
        view.dialogManager().register(EditItemViewModel.class, EditItemView::dialog);
    }

    public OrderContext orderContext() {
        return orderContext;
    }

    public SidebarItemViewModel sidebarItem() {
        return new SidebarItemViewModel("Orders", this::showExplorer, orderContext.pendingCountProperty());
    }

    public void showExplorer() {
        shell.show(this::ordersExplorerViewModel);
    }

    public OrdersExplorerViewModel ordersExplorerViewModel() {
        return new OrdersExplorerViewModel(
            orderRepository::findAll,
            new OrdersExplorerHost() {
                @Override public void showOrderDetails(Order order) { shell.show(() -> orderEditorViewModel(order)); }
                @Override public void setPendingOrderCount(int count) { orderContext.setCount(count); }
            },
            shell.statusItems()
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
                @Override public void returnToList() { shell.show(OrdersModule.this::ordersExplorerViewModel); }
                @Override public void openOrder(Order copied) { shell.show(() -> orderEditorViewModel(copied)); }
                @Override public void showItemEditor(EditItemRequest request) { view.dialogManager().show(editItemViewModel(request)); }
            });
    }

    private EditItemViewModel editItemViewModel(EditItemRequest request) {
        return new EditItemViewModel(request);
    }
}
