package mvvm.example;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mvvm.example.core.config.AppModule;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var mainView = bootstrap(stage);

        stage.setTitle("Order Management");
        stage.setScene(new Scene(mainView, 1024, 768));
        stage.show();
    }

    private Parent bootstrap(Stage stage) {
        var app = new AppModule();
        var shell = app.createShellModule(stage);

        var orders = shell.createOrdersModule();
        var customers = shell.createCustomersModule();
        var stock = shell.createStockModule();

        return shell.mainView(
            orders.sidebarItem(),
            customers.sidebarItem(),
            stock.sidebarItem()
        );
    }
}
