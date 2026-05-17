package mvvm.example.core.config;

import mvvm.example.core.view.ViewServices;
import mvvm.example.orders.domain.CopyOrderCommand;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.orders.editor.*;
import mvvm.example.orders.requests.EditItemRequest;
import mvvm.example.orders.editor.lineitems.editor.EditItemView;
import mvvm.example.orders.editor.lineitems.editor.EditItemViewModel;
import mvvm.example.orders.editor.header.OrderHeaderView;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsView;
import mvvm.example.orders.editor.lineitems.LineItemsViewModel;
import mvvm.example.orders.explorer.OrdersExplorerView;
import mvvm.example.orders.explorer.OrdersExplorerViewModel;
import mvvm.example.orders.requests.EditOrderRequest;
import mvvm.example.shell.ShellContext;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;

public class OrdersModule {

    private final OrderRepository orderRepository;
    private final ViewServices view;
    private final ShellContext shell;
    private final CopyOrderCommand copyOrderCommand;

    public OrdersModule(OrderRepository orderRepository, ViewServices view, ShellContext shell, CopyOrderCommand copyOrderCommand) {
        this.orderRepository = orderRepository;
        this.view = view;
        this.shell = shell;
        this.copyOrderCommand = copyOrderCommand;

        view.viewLocator().register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
        view.viewLocator().register(OrderHeaderViewModel.class, OrderHeaderView::new);
        view.viewLocator().register(LineItemsViewModel.class, LineItemsView::new);
        view.viewLocator().register(OrderEditorViewModel.class, vm -> new OrderEditorView(vm, view.viewLocator()));
        view.dialogManager().register(EditItemViewModel.class, EditItemView::dialog);
    }

    public SidebarItemViewModel sidebarItem() {
        return new SidebarItemViewModel("Orders", this::showExplorer);
    }

    public void showExplorer() {
        shell.show(this::ordersExplorerViewModel);
    }

    public OrdersExplorerViewModel ordersExplorerViewModel() {
        return new OrdersExplorerViewModel(
            orderRepository::findAll,
            request -> shell.show(() -> orderEditorViewModel(request)),
            shell.statusItems()
        );
    }

    private OrderEditorViewModel orderEditorViewModel(EditOrderRequest request) {
        return new OrderEditorViewModel(
            request,
            new OrderEditorService() {
                @Override public Order fetchOrder(String orderId) { return orderRepository.findById(orderId).orElseThrow(); }
                @Override public void saveOrder(Order order) { orderRepository.save(order); }
                @Override public String copyOrder(String orderId) { return copyOrderCommand.copy(orderId); }
                @Override public void deleteOrder(String orderId) { orderRepository.delete(orderId); }
            },
            new OrderEditorHost() {
                @Override public void returnToList() { shell.show(OrdersModule.this::ordersExplorerViewModel); }
                @Override public void openOrder(EditOrderRequest copyRequest) { shell.show(() -> orderEditorViewModel(copyRequest)); }
                @Override public void showItemEditor(EditItemRequest request) { view.dialogManager().show(editItemViewModel(request)); }
            });
    }

    private EditItemViewModel editItemViewModel(EditItemRequest request) {
        return new EditItemViewModel(request);
    }
}
