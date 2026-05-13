package mvvm.example.shell.main;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.shell.sidebar.SidebarView;

public class MainView extends StackPane {

    private final StackPane workspace = new StackPane();

    public MainView(MainViewModel viewModel, ViewLocator viewLocator) {
        var layout = new BorderPane();
        layout.setLeft(new SidebarView(viewModel.getSidebar()));
        layout.setCenter(workspace);

        getChildren().add(layout);

        viewModel.activeViewModelProperty().addListener((obs, old, vm) -> {
            if (vm != null) workspace.getChildren().setAll(viewLocator.resolve(vm));
        });
    }
}
