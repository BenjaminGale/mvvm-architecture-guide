package mvvm.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mvvm.example.core.view.DialogManager;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.viewmodel.ViewModelRouter;
import mvvm.example.customers.adapters.CustomerModule;
import mvvm.example.orders.adapters.OrdersModule;
import mvvm.example.orders.editor.edititem.EditItemViewModel;
import mvvm.example.settings.SettingsModule;
import mvvm.example.shell.main.MainView;
import mvvm.example.shell.main.MainViewModel;
import mvvm.example.shell.sidebar.SidebarViewModel;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var viewLocator = new ViewLocator();
        var viewModelRouter = new ViewModelRouter();

        var dialogManager = new DialogManager(stage, viewLocator, viewModelRouter);
        dialogManager.register(EditItemViewModel.class);

        var orderModule = new OrdersModule(viewLocator, viewModelRouter);
        var customerModule = new CustomerModule(viewLocator, viewModelRouter);
        var settingsModule = new SettingsModule(viewLocator, viewModelRouter, orderModule::routeToOrders);

        var mainViewModel = new MainViewModel(
            new SidebarViewModel(
                orderModule.orderContext(),
                orderModule::routeToOrders,
                customerModule::routeToCustomers,
                settingsModule::routeToOrders
            ),
            viewModelRouter
        );

        var rootView = new MainView(mainViewModel, viewLocator);

        stage.setTitle("Order Management");
        stage.setScene(new Scene(rootView, 1024, 768));
        stage.show();

        // Show initial screen
        viewModelRouter.dispatch(orderModule.orders());
    }
}
