package mvvm.example;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mvvm.example.customers.adapters.CustomersModule;
import mvvm.example.orders.adapters.OrdersModule;
import mvvm.example.shell.adapters.ShellModule;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var mainView = bootstrap(stage);

        stage.setTitle("Order Management");
        stage.setScene(new Scene(mainView, 1024, 768));
        stage.show();
    }

    private Parent bootstrap(Stage stage) {
        var shell = new ShellModule(stage);
        var orders = new OrdersModule(shell.appContext(), shell.context());
        var customers = new CustomersModule(shell.appContext(), shell.context());

        orders.showExplorer();

        return shell.mainView(orders, customers);
    }
}
