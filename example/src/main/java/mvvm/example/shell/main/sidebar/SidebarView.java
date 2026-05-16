package mvvm.example.shell.main.sidebar;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import mvvm.example.core.view.controls.Buttons;
import mvvm.example.core.view.controls.Labels;
import mvvm.example.core.view.controls.Spacer;

public class SidebarView extends BorderPane {

    private final VBox navigationHost = new VBox();

    public SidebarView(SidebarViewModel viewModel) {
        var separator = new Region();
        separator.setStyle("-fx-background-color: -fx-box-border;");
        separator.setPrefWidth(1);
        separator.setMinWidth(1);
        separator.setMaxWidth(1);

        setRight(separator);
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

        var titleLabel = new Label();
        titleLabel.textProperty().bind(viewModel.titleProperty());

        button.setGraphic(new HBox(titleLabel, Spacer.create(), Labels.badge(viewModel.countProperty())));
        button.setContentDisplay(ContentDisplay.RIGHT);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);

        Buttons.bind(button, viewModel.openWorkspaceAction());

        return button;
    }
}
