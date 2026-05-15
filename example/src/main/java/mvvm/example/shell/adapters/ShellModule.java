package mvvm.example.shell.adapters;

import javafx.scene.Parent;
import javafx.stage.Window;
import mvvm.example.AppContext;
import mvvm.example.core.view.DialogManager;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.customers.adapters.CustomersModule;
import mvvm.example.orders.adapters.OrdersModule;
import mvvm.example.orders.context.OrderContext;
import mvvm.example.shell.WorkspaceContext;
import mvvm.example.shell.main.MainView;
import mvvm.example.shell.main.MainViewModel;
import mvvm.example.shell.sidebar.SidebarViewModel;

public class ShellModule {

    private final AppContext appContext;
    private final WorkspaceContext workspaceContext;

    public ShellModule(Window window) {
        this.appContext = new AppContext(
            new ViewLocator<>(),
            new DialogManager(
                window,
                new ViewLocator<>()
            )
        );

        this.workspaceContext = new WorkspaceContext();

        this.appContext.viewLocator().register(MainViewModel.class, vm -> new MainView(vm, this.appContext.viewLocator()));
    }

    public AppContext appContext() {
        return appContext;
    }

    public WorkspaceContext workspaceContext() {
        return workspaceContext;
    }

    public Parent mainView(OrderContext orderContext, Navigation navigation) {
        return appContext.viewLocator().locate(mainViewModel(orderContext, navigation));
    }

    private MainViewModel mainViewModel(OrderContext orderContext, Navigation navigation) {
        return new MainViewModel(
            new SidebarViewModel(
                orderContext,
                navigation.navigateToOrders,
                navigation.navigateToCustomers
            ),
            workspaceContext
        );
    }

    public Navigation navigation(OrdersModule orders, CustomersModule customers) {
        return new Navigation(
            () -> workspaceContext.show(orders.ordersExplorerViewModel()),
            () -> workspaceContext.show(customers.customersExplorerViewModel())
        );
    }

    public record Navigation(
        Runnable navigateToOrders,
        Runnable navigateToCustomers
    ) {
    }
}
