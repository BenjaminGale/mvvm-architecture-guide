package mvvm.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.ViewRouter;
import mvvm.example.customers.adapters.CustomerModule;
import mvvm.example.customers.adapters.InMemoryCustomerRepository;
import mvvm.example.customers.domain.CustomerService;
import mvvm.example.customers.detail.CustomerDetailView;
import mvvm.example.customers.detail.CustomerDetailViewModel;
import mvvm.example.customers.explorer.CustomersExplorerView;
import mvvm.example.customers.explorer.CustomersViewModel;
import mvvm.example.orders.adapters.InMemoryOrderRepository;
import mvvm.example.orders.adapters.OrderModule;
import mvvm.example.orders.context.OrderContext;
import mvvm.example.orders.domain.OrderService;
import mvvm.example.orders.editor.OrderEditorView;
import mvvm.example.orders.editor.OrderEditorViewModel;
import mvvm.example.orders.editor.edititem.EditItemView;
import mvvm.example.orders.editor.edititem.EditItemViewModel;
import mvvm.example.orders.explorer.OrdersExplorerView;
import mvvm.example.orders.explorer.OrdersViewModel;
import mvvm.example.settings.SettingsView;
import mvvm.example.settings.SettingsViewModel;
import mvvm.example.shell.MainView;
import mvvm.example.shell.MainViewModel;
import mvvm.example.shell.SidebarViewModel;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Services
        var orderService    = new OrderService(new InMemoryOrderRepository());
        var customerService = new CustomerService(new InMemoryCustomerRepository());

        // Shared observable state
        var orderContext = new OrderContext();

        // Navigation infrastructure
        var viewLocator = new ViewLocator();
        var viewRouter  = new ViewRouter(viewLocator);

        // Modules
        var orderModule    = new OrderModule(orderService, orderContext, viewRouter);
        var customerModule = new CustomerModule(customerService, viewRouter);

        // View registrations
        viewLocator.register(OrdersViewModel.class,        OrdersExplorerView::new);
        viewLocator.register(OrderEditorViewModel.class,   OrderEditorView::new);
        viewLocator.register(EditItemViewModel.class,      EditItemView::new);
        viewLocator.register(CustomersViewModel.class,     CustomersExplorerView::new);
        viewLocator.register(CustomerDetailViewModel.class, CustomerDetailView::new);
        viewLocator.register(SettingsViewModel.class,      SettingsView::new);

        // Shell
        var sidebarVm = new SidebarViewModel(
            orderContext,
            () -> viewRouter.navigateTo(orderModule.orders()),
            () -> viewRouter.navigateTo(customerModule.customers()),
            () -> viewRouter.navigateTo(new SettingsViewModel(() -> viewRouter.navigateTo(orderModule.orders())))
        );

        var rootView = new MainView(new MainViewModel(sidebarVm), viewRouter);

        stage.setTitle("Order Management");
        stage.setScene(new Scene(rootView, 1024, 768));
        stage.show();

        // Show initial screen
        viewRouter.navigateTo(orderModule.orders());
    }
}
