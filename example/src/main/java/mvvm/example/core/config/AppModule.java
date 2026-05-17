package mvvm.example.core.config;

import javafx.stage.Stage;
import mvvm.example.core.view.DialogManager;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.ViewServices;
import mvvm.example.core.config.adapters.InMemoryCustomerRepository;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.core.config.adapters.InMemoryOrderRepository;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.shell.ShellContext;

public class AppModule {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public AppModule() {
        this.customerRepository = new InMemoryCustomerRepository();
        this.orderRepository = new InMemoryOrderRepository();
    }

    public ShellModule createShellModule(Stage stage) {
        return new ShellModule(
            customerRepository,
            orderRepository,
            new ViewServices(
                new ViewLocator<>(),
                new DialogManager(
                    stage,
                    new ViewLocator<>())),
            new ShellContext());
    }
}
