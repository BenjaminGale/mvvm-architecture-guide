package mvvm.example.core.config;

import mvvm.example.core.view.ViewServices;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.orders.domain.commands.CopyOrderCommand;
import mvvm.example.orders.domain.commands.DeleteLineItemCommand;
import mvvm.example.orders.domain.queries.GetLineItemSummariesQuery;
import mvvm.example.orders.domain.queries.GetOrderSummariesQuery;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.queries.LineItemSummary;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.stock.domain.ProductRepository;
import mvvm.example.stock.domain.StockRepository;
import mvvm.example.orders.editor.*;
import mvvm.example.orders.editor.header.CustomerSelectorView;
import mvvm.example.orders.editor.header.CustomerSelectorViewModel;
import mvvm.example.orders.editor.EditOrderRequest;
import mvvm.example.orders.editor.header.SelectCustomerRequest;
import mvvm.example.orders.editor.lineitems.EditItemRequest;
import mvvm.example.orders.editor.lineitems.LineItemEditorView;
import mvvm.example.orders.editor.lineitems.LineItemEditorViewModel;
import mvvm.example.orders.editor.lineitems.ProductSelectorView;
import mvvm.example.orders.editor.lineitems.ProductSelectorViewModel;
import mvvm.example.orders.editor.header.OrderHeaderView;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsExplorerView;
import mvvm.example.orders.editor.lineitems.LineItemsExplorerViewModel;
import mvvm.example.orders.explorer.OrdersExplorerView;
import mvvm.example.orders.explorer.OrdersExplorerViewModel;
import mvvm.example.shell.ShellContext;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;
import mvvm.example.shell.main.statusbar.LabelType;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;

import java.util.List;
import java.util.Optional;

public class OrdersModule {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final ViewServices view;
    private final ShellContext shell;
    private final CopyOrderCommand copyOrderCommand;
    private final DeleteLineItemCommand deleteLineItemCommand;

    public OrdersModule(OrderRepository orderRepository, CustomerRepository customerRepository, ProductRepository productRepository, StockRepository stockRepository, ViewServices view, ShellContext shell, CopyOrderCommand copyOrderCommand, DeleteLineItemCommand deleteLineItemCommand) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.view = view;
        this.shell = shell;
        this.copyOrderCommand = copyOrderCommand;
        this.deleteLineItemCommand = deleteLineItemCommand;

        view.viewLocator().register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
        view.viewLocator().register(OrderHeaderViewModel.class, OrderHeaderView::new);
        view.viewLocator().register(LineItemsExplorerViewModel.class, LineItemsExplorerView::new);
        view.viewLocator().register(OrderEditorViewModel.class, vm -> new OrderEditorView(vm, view.viewLocator()));
        view.dialogManager().register(LineItemEditorViewModel.class, LineItemEditorView::dialog);
        view.dialogManager().register(CustomerSelectorViewModel.class, CustomerSelectorView::dialog);
        view.dialogManager().register(ProductSelectorViewModel.class, ProductSelectorView::dialog);
    }

    public SidebarItemViewModel sidebarItem() {
        return new SidebarItemViewModel("Orders", this::showExplorer);
    }

    public void showExplorer() {
        shell.show(this::ordersExplorerViewModel);
    }

    public OrdersExplorerViewModel ordersExplorerViewModel() {
        var vm = new OrdersExplorerViewModel(
            new GetOrderSummariesQuery(orderRepository, customerRepository)::execute,
            request -> shell.show(() -> orderEditorViewModel(request))
        );

        shell.statusItems().addAll(
            new StatusItemViewModel(vm.ordersCountProperty(), LabelType.All_ORDERS),
            new StatusItemViewModel(vm.overdueOrdersCountProperty(), LabelType.OVERDUE_ORDERS)
        );

        return vm;
    }

    private OrderEditorViewModel orderEditorViewModel(EditOrderRequest request) {
        var lineItemsQuery = new GetLineItemSummariesQuery(productRepository, stockRepository);
        return new OrderEditorViewModel(
            request,
            new OrderEditorService() {
                @Override public Order fetchOrder(String orderId) { return orderRepository.findById(orderId).orElseThrow(); }
                @Override public Optional<Customer> findCustomer(String customerId) { return customerRepository.findById(customerId); }
                @Override public void saveOrder(Order order) { orderRepository.save(order); }
                @Override public String copyOrder(String orderId) { return copyOrderCommand.copy(orderId); }
                @Override public void deleteOrder(String orderId) { orderRepository.delete(orderId); }
                @Override public java.util.concurrent.CompletableFuture<List<LineItemSummary>> fetchLineItemSummaries(List<LineItem> items, String orderId) { return lineItemsQuery.execute(items, orderId); }
                @Override public void deleteLineItem(String productId, String orderId) { deleteLineItemCommand.execute(productId, orderId); }
            },
            new OrderEditorHost() {
                @Override public void returnToList() { shell.show(OrdersModule.this::ordersExplorerViewModel); }
                @Override public void openOrder(EditOrderRequest copyRequest) { shell.show(() -> orderEditorViewModel(copyRequest)); }
                @Override public void showItemEditor(EditItemRequest request) { view.dialogManager().show(editItemViewModel(request)); }
                @Override public void showCustomerSelector(SelectCustomerRequest request) { view.dialogManager().show(new CustomerSelectorViewModel(request, customerRepository.findAll())); }
            });
    }

    private LineItemEditorViewModel editItemViewModel(EditItemRequest request) {
        return new LineItemEditorViewModel(request, r -> view.dialogManager().show(new ProductSelectorViewModel(r, productRepository.findAll())));
    }

}
