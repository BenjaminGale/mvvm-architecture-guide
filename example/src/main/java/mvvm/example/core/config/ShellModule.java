package mvvm.example.core.config;

import javafx.scene.Parent;
import mvvm.example.core.view.ViewServices;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.orders.domain.commands.CopyOrderCommand;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.stock.domain.ProductRepository;
import mvvm.example.shell.ShellView;
import mvvm.example.shell.ShellViewModel;
import mvvm.example.shell.workspace.WorkspaceViewModel;
import mvvm.example.shell.statusbar.StatusItemView;
import mvvm.example.shell.statusbar.StatusItemViewModel;

import java.util.List;

public class ShellModule {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ViewServices view;

    public ShellModule(CustomerRepository customerRepository, OrderRepository orderRepository, ProductRepository productRepository, ViewServices view) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.view = view;

        view.viewLocator().register(ShellViewModel.class, vm -> new ShellView(vm, view.viewLocator()));
        view.viewLocator().register(StatusItemViewModel.class, StatusItemView::new);
    }

    public OrdersModule createOrdersModule() {
        return new OrdersModule(
            orderRepository,
            customerRepository,
            productRepository,
            view,
            new CopyOrderCommand(orderRepository));
    }

    public CustomersModule createCustomersModule() {
        return new CustomersModule(customerRepository, view);
    }

    public StockModule createStockModule() {
        return new StockModule(productRepository, view);
    }

    public Parent mainView(WorkspaceViewModel... workspaces) {
        return view.viewLocator().locate(new ShellViewModel(List.of(workspaces)));
    }
}
