package mvvm.example.core.config;

import mvvm.example.core.view.ViewServices;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.orders.domain.CopyOrderCommand;
import mvvm.example.orders.domain.GetOrderSummariesQuery;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.stock.domain.ProductRepository;
import mvvm.example.stock.domain.StockRepository;
import mvvm.example.orders.editor.*;
import mvvm.example.orders.editor.header.CustomerSelectorView;
import mvvm.example.orders.editor.header.CustomerSelectorViewModel;
import mvvm.example.orders.requests.EditItemRequest;
import mvvm.example.orders.editor.lineitems.editor.EditItemView;
import mvvm.example.orders.editor.lineitems.editor.EditItemViewModel;
import mvvm.example.orders.editor.lineitems.selector.ProductSelectorView;
import mvvm.example.orders.editor.lineitems.selector.ProductSelectorViewModel;
import mvvm.example.orders.editor.header.OrderHeaderView;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsView;
import mvvm.example.orders.editor.lineitems.LineItemsViewModel;
import mvvm.example.orders.explorer.OrdersExplorerView;
import mvvm.example.orders.explorer.OrdersExplorerViewModel;
import mvvm.example.orders.requests.EditOrderRequest;
import mvvm.example.orders.requests.SelectCustomerRequest;
import mvvm.example.shell.ShellContext;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;

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

    public OrdersModule(OrderRepository orderRepository, CustomerRepository customerRepository, ProductRepository productRepository, StockRepository stockRepository, ViewServices view, ShellContext shell, CopyOrderCommand copyOrderCommand) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.view = view;
        this.shell = shell;
        this.copyOrderCommand = copyOrderCommand;

        view.viewLocator().register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
        view.viewLocator().register(OrderHeaderViewModel.class, OrderHeaderView::new);
        view.viewLocator().register(LineItemsViewModel.class, LineItemsView::new);
        view.viewLocator().register(OrderEditorViewModel.class, vm -> new OrderEditorView(vm, view.viewLocator()));
        view.dialogManager().register(EditItemViewModel.class, EditItemView::dialog);
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
        return new OrdersExplorerViewModel(
            new GetOrderSummariesQuery(orderRepository, customerRepository)::execute,
            request -> shell.show(() -> orderEditorViewModel(request)),
            shell.statusItems()
        );
    }

    private OrderEditorViewModel orderEditorViewModel(EditOrderRequest request) {
        return new OrderEditorViewModel(
            request,
            new OrderEditorService() {
                @Override public Order fetchOrder(String orderId) { return orderRepository.findById(orderId).orElseThrow(); }
                @Override public Optional<Customer> findCustomer(String customerId) { return customerRepository.findById(customerId); }
                @Override public void saveOrder(Order order) { orderRepository.save(order); }
                @Override public String copyOrder(String orderId) { return copyOrderCommand.copy(orderId); }
                @Override public void deleteOrder(String orderId) { orderRepository.delete(orderId); }
            },
            new OrderEditorHost() {
                @Override public void returnToList() { shell.show(OrdersModule.this::ordersExplorerViewModel); }
                @Override public void openOrder(EditOrderRequest copyRequest) { shell.show(() -> orderEditorViewModel(copyRequest)); }
                @Override public void showItemEditor(EditItemRequest request) { view.dialogManager().show(editItemViewModel(request)); }
                @Override public void showCustomerSelector(SelectCustomerRequest request) { view.dialogManager().show(new CustomerSelectorViewModel(request, customerRepository.findAll())); }
            });
    }

    private EditItemViewModel editItemViewModel(EditItemRequest request) {
        return new EditItemViewModel(request, r -> view.dialogManager().show(new ProductSelectorViewModel(r, productRepository.findAll())));
    }

}
