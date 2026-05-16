package mvvm.example.shell.main.sidebar;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import mvvm.example.core.view.controls.Buttons;

public class SidebarView extends BorderPane {

    private final VBox navigationHost = new VBox();

    public SidebarView(SidebarViewModel viewModel) {
        setRight(new Separator(Orientation.VERTICAL));
        setCenter(navigationHost);
        setPrefWidth(180);

        navigationHost.setPadding(new Insets(8));
        navigationHost.setSpacing(4);

        setContent(viewModel.navigationItems());

        viewModel
            .navigationItems()
            .addListener((ListChangeListener<SidebarItemViewModel>) _ ->
                setContent(viewModel.navigationItems())
            );
    }

    private void setContent(ObservableList<SidebarItemViewModel> items) {
        navigationHost
            .getChildren()
            .setAll(
                items.stream()
                    .map(this::navigationButton)
                    .toList()
            );
    }

    private Button navigationButton(SidebarItemViewModel viewModel) {
        var button = new Button();

        button.textProperty().bind(viewModel.titleProperty());
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);

        Buttons.bind(button, viewModel.openWorkspaceAction());

        return button;
    }
}
