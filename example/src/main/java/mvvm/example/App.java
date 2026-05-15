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
        var shellModule = new ShellModule(stage);
        var ordersModule = new OrdersModule(shellModule.appContext(), shellModule.workspaceContext());
        var customersModule = new CustomersModule(shellModule.appContext(), shellModule.workspaceContext());
        var settingsModule = new SettingsModule(shellModule.appContext());

        // TODO: Need a better way of doing this...
        var navigationContext = new ShellModule.NavigationContext(
            () -> shellModule.workspaceContext().show(ordersModule.orders()),
            () -> shellModule.workspaceContext().show(customersModule.customers()),
            () -> shellModule.workspaceContext().show(
                settingsModule.settings(
                    () -> shellModule.workspaceContext().show(ordersModule.orders())
                )
            )
        );

        shellModule.workspaceContext().show(ordersModule.orders());
        var mainView = shellModule.mainView(ordersModule.orderContext(), navigationContext);

        stage.setTitle("Order Management");
        stage.setScene(new Scene(mainView, 1024, 768));
        stage.show();
    }
}
