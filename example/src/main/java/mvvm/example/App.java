package mvvm.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mvvm.example.core.view.DialogManager;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.ViewRouter;
import mvvm.example.customers.adapters.CustomerModule;
import mvvm.example.orders.adapters.OrderModule;
import mvvm.example.orders.editor.edititem.EditItemView;
import mvvm.example.settings.SettingsModule;
import mvvm.example.shell.main.MainView;
import mvvm.example.shell.main.MainViewModel;
import mvvm.example.shell.sidebar.SidebarViewModel;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Navigation infrastructure
        var viewLocator = new ViewLocator();
        var viewRouter  = new ViewRouter(viewLocator);
        viewRouter.addListener(EditItemView.class, v -> new DialogManager(stage).openAsDialog(v));

        // Modules
        var orderModule    = new OrderModule(viewLocator, viewRouter);
        var customerModule = new CustomerModule(viewLocator, viewRouter);
        var settingsModule = new SettingsModule(viewLocator, viewRouter, orderModule::routeToOrders);

        // Shell
        var rootView = new MainView(
            new MainViewModel(
                new SidebarViewModel(
                    orderModule.orderContext(),
                    orderModule::routeToOrders,
                    customerModule::routeToCustomers,
                    settingsModule::routeToOrders
                )), viewRouter);

        stage.setTitle("Order Management");
        stage.setScene(new Scene(rootView, 1024, 768));
        stage.show();

        // Show initial screen
        viewRouter.route(orderModule.orders());
    }
}
