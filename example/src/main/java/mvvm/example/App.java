package mvvm.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mvvm.example.customers.adapters.CustomersModule;
import mvvm.example.orders.adapters.OrdersModule;
import mvvm.example.settings.SettingsModule;
import mvvm.example.shell.adapters.ShellModule;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var shell = new ShellModule(stage);
        var orders = new OrdersModule(shell.appContext(), shell.workspaceContext());
        var customers = new CustomersModule(shell.appContext(), shell.workspaceContext());
        var settings = new SettingsModule(shell.appContext());

        // TODO: Need a better way of doing this...
        var navigationContext = new ShellModule.NavigationContext(
            () -> shell.workspaceContext().show(orders.ordersExplorerViewModel()),
            () -> shell.workspaceContext().show(customers.customersExplorerViewModel()),
            () -> shell.workspaceContext().show(
                settings.settingsViewModel(
                    () -> shell.workspaceContext().show(orders.ordersExplorerViewModel())
                )
            )
        );

        shell.workspaceContext().show(orders.ordersExplorerViewModel());
        var mainView = shell.mainView(orders.orderContext(), navigationContext);

        stage.setTitle("Order Management");
        stage.setScene(new Scene(mainView, 1024, 768));
        stage.show();
    }
}
