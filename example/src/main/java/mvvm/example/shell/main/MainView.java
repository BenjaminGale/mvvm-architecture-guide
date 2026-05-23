package mvvm.example.shell.main;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import mvvm.example.core.view.ViewLocator;

public class MainView extends BorderPane {

    private final ViewLocator<Region> viewLocator;

    public MainView(MainViewModel viewModel, ViewLocator<Region> viewLocator) {
        this.viewLocator = viewLocator;

        setLeft(viewLocator.locate(viewModel.getSidebar()));
        setBottom(viewLocator.locate(viewModel.getStatusBar()));

        show(viewModel.currentWorkspaceProperty().get());

        viewModel
            .currentWorkspaceProperty()
            .addListener(_ -> show(viewModel.currentWorkspaceProperty().get()));
    }

    private void show(Object viewModel) {
        setCenter(viewModel != null ? viewLocator.locate(viewModel) : null);
    }
}
