package mvvm.example.core.config;

import javafx.stage.Stage;
import mvvm.example.core.view.DialogManager;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.ViewServices;
import mvvm.example.core.config.adapters.InMemoryCustomerRepository;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.core.config.adapters.InMemoryOrderRepository;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.core.config.adapters.InMemoryProductRepository;
import mvvm.example.stock.domain.ProductRepository;
import mvvm.example.core.config.adapters.InMemoryStockRepository;
import mvvm.example.stock.domain.StockRepository;
import mvvm.example.shell.ShellContext;

public class AppModule {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    public AppModule() {
        this.customerRepository = new InMemoryCustomerRepository();
        this.orderRepository = new InMemoryOrderRepository();
        this.productRepository = new InMemoryProductRepository();
        this.stockRepository = new InMemoryStockRepository();
    }

    public ShellModule createShellModule(Stage stage) {
        return new ShellModule(
            customerRepository,
            orderRepository,
            productRepository,
            stockRepository,
            new ViewServices(
                new ViewLocator<>(),
                new DialogManager(
                    stage,
                    new ViewLocator<>())),
            new ShellContext());
    }
}
