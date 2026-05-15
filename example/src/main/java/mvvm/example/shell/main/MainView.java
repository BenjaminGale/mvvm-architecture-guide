package mvvm.example.shell.main;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.shell.sidebar.SidebarView;

public class MainView extends StackPane {

    private final ViewLocator<Region> viewLocator;
    private final StackPane workspace = new StackPane();

    public MainView(MainViewModel viewModel, ViewLocator<Region> viewLocator) {
        this.viewLocator = viewLocator;

        var layout = new BorderPane();
        layout.setLeft(new SidebarView(viewModel.getSidebar()));
        layout.setCenter(workspace);
        layout.setBottom(new StatusBarView(viewModel.statusMessages()));

        getChildren().add(layout);

        if (viewModel.currentWorkspaceProperty().get() != null) {
            show(viewModel.currentWorkspaceProperty().get());
        }

        viewModel.currentWorkspaceProperty().addListener((obs, old, vm) -> {
            if (vm != null) show(vm);
        });
    }

    private void show(Object viewModel) {
        workspace.getChildren().setAll(viewLocator.locate(viewModel));
    }
}
