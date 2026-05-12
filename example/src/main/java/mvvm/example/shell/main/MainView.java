package mvvm.example.shell.main;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import mvvm.example.core.view.ViewRouter;
import mvvm.example.customers.detail.CustomerDetailView;
import mvvm.example.customers.explorer.CustomersExplorerView;
import mvvm.example.orders.editor.OrderEditorView;
import mvvm.example.orders.explorer.OrdersExplorerView;
import mvvm.example.settings.SettingsView;
import mvvm.example.shell.sidebar.SidebarView;

public class MainView extends StackPane {

    private final StackPane workspace = new StackPane();

    public MainView(MainViewModel viewModel, ViewRouter viewRouter) {
        var layout = new BorderPane();
        layout.setLeft(new SidebarView(viewModel.getSidebar()));
        layout.setCenter(workspace);

        getChildren().add(layout);

        viewRouter.addListener(OrdersExplorerView.class, view -> workspace.getChildren().setAll(view));
        viewRouter.addListener(OrderEditorView.class,       view -> workspace.getChildren().setAll(view));
        viewRouter.addListener(CustomersExplorerView.class, view -> workspace.getChildren().setAll(view));
        viewRouter.addListener(CustomerDetailView.class,    view -> workspace.getChildren().setAll(view));
        viewRouter.addListener(SettingsView.class,          view -> workspace.getChildren().setAll(view));
    }
}
