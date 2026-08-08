package mvvm.example.shell.workspace;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mvvm.example.shell.ShellViewModel;

public class WorkspaceSidebarView extends BorderPane {

    private final VBox navigationHost = navigationHost();
    private final ToggleGroup toggleGroup = new ToggleGroup();

    public WorkspaceSidebarView(ShellViewModel viewModel) {
        setRight(separator());
        setCenter(navigationHost);
        setPrefWidth(180);

        toggleGroup.selectedToggleProperty().addListener((_, oldToggle, newToggle) -> {
            if (newToggle == null) {
                toggleGroup.selectToggle(oldToggle);
            }
        });

        setContent(viewModel);
        viewModel.workspaces().addListener((InvalidationListener) _ -> setContent(viewModel));

        viewModel.currentWorkspaceProperty().addListener((_, _, workspace) -> selectToggleFor(workspace));
    }

    private void setContent(ShellViewModel viewModel) {
        navigationHost.getChildren().setAll(
            viewModel.workspaces().stream().map(this::navigationButton).toList()
        );
        selectToggleFor(viewModel.currentWorkspaceProperty().get());
    }

    private void selectToggleFor(WorkspaceViewModel workspace) {
        navigationHost.getChildren().stream()
            .filter(node -> node.getUserData() == workspace)
            .findFirst()
            .ifPresent(node -> toggleGroup.selectToggle((Toggle) node));
    }

    private ToggleButton navigationButton(WorkspaceViewModel workspace) {
        var button = new ToggleButton();
        button.textProperty().bind(workspace.titleProperty());
        button.setUserData(workspace);
        button.setToggleGroup(toggleGroup);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(e -> workspace.openAction().execute());

        return button;
    }

    private static VBox navigationHost() {
        var host = new VBox();
        host.setPadding(new Insets(8));
        host.setSpacing(4);
        return host;
    }

    private static Region separator() {
        var separator = new Region();
        separator.setStyle("-fx-background-color: -fx-box-border;");
        separator.setPrefWidth(1);
        separator.setMinWidth(1);
        separator.setMaxWidth(1);
        return separator;
    }
}
