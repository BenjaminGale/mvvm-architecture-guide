package mvvm.example.shell;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import mvvm.example.core.view.ViewRouter;

public class MainView extends StackPane {

    private final StackPane workspace = new StackPane();

    public MainView(MainViewModel viewModel, ViewRouter viewRouter) {
        var layout = new BorderPane();
        layout.setLeft(new SidebarView(viewModel.getSidebar()));
        layout.setCenter(workspace);

        var dialogManager = new DialogManagerView(viewRouter);

        getChildren().addAll(layout, dialogManager);

        // Workspace listener registrations added as screens are built
    }
}
