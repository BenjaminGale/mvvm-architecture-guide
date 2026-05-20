package mvvm.example.core.config;

import javafx.scene.Parent;
import mvvm.example.core.view.ViewServices;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.orders.domain.commands.CopyOrderCommand;
import mvvm.example.stock.domain.commands.DeleteStockAllocationsCommand;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.stock.domain.ProductRepository;
import mvvm.example.stock.domain.StockRepository;
import mvvm.example.shell.ShellContext;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;
import mvvm.example.shell.main.sidebar.SidebarView;
import mvvm.example.shell.main.statusbar.StatusBarView;
import mvvm.example.shell.main.statusbar.StatusBarViewModel;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;
import mvvm.example.shell.main.MainView;
import mvvm.example.shell.main.statusbar.StatusItemView;
import mvvm.example.shell.main.MainViewModel;
import mvvm.example.shell.main.sidebar.SidebarViewModel;

import java.util.Arrays;

public class ShellModule {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final ViewServices view;
    private final ShellContext shell;

    public ShellModule(CustomerRepository customerRepository, OrderRepository orderRepository, ProductRepository productRepository, StockRepository stockRepository, ViewServices view, ShellContext shell) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.view = view;
        this.shell = shell;

        view.viewLocator().register(MainViewModel.class, vm -> new MainView(vm, view.viewLocator()));
        view.viewLocator().register(StatusBarViewModel.class, vm -> new StatusBarView(vm, view.viewLocator()));
        view.viewLocator().register(StatusItemViewModel.class, StatusItemView::new);
        view.viewLocator().register(SidebarViewModel.class, SidebarView::new);
    }

    public OrdersModule createOrdersModule() {
        return new OrdersModule(
            orderRepository,
            customerRepository,
            productRepository,
            stockRepository,
            view,
            shell,
            new CopyOrderCommand(orderRepository),
            new DeleteStockAllocationsCommand(stockRepository));
    }

    public CustomersModule createCustomersModule() {
        return new CustomersModule(customerRepository, view, shell);
    }

    public StockModule createStockModule() {
        return new StockModule(productRepository, view, shell);
    }

    public Parent mainView(SidebarItemViewModel... items) {
        shell.navigationItems().addAll(items);

        // TODO: Move to MainViewModel
        Arrays.stream(items)
            .findFirst()
            .ifPresent(item -> item.openWorkspaceAction().execute());

        return view.viewLocator().locate(mainViewModel());
    }

    private MainViewModel mainViewModel() {
        return new MainViewModel(
            new SidebarViewModel(shell.navigationItems()),
            new StatusBarViewModel(shell.statusItems()),
            shell.currentWorkspaceProperty()
        );
    }
}
