package mvvm.example.shell;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import mvvm.example.core.view.ViewRouter;
import mvvm.example.orders.editor.OrderEditorView;
import mvvm.example.orders.OrdersExplorerView;

public class MainView extends StackPane {

    private final StackPane workspace = new StackPane();

    public MainView(MainViewModel viewModel, ViewRouter viewRouter) {
        var layout = new BorderPane();
        layout.setLeft(new SidebarView(viewModel.getSidebar()));
        layout.setCenter(workspace);

        var dialogManager = new DialogManagerView(viewRouter);

        getChildren().addAll(layout, dialogManager);

        viewRouter.addListener(OrdersExplorerView.class, view -> workspace.getChildren().setAll(view));
        viewRouter.addListener(OrderEditorView.class,    view -> workspace.getChildren().setAll(view));
        // Further workspace listener registrations added as screens are built
    }
}
