package mvvm.example.shell.main.sidebar;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mvvm.example.core.view.controls.Labels;

public class SidebarView extends VBox {

    public SidebarView(SidebarViewModel viewModel) {
        setPadding(new Insets(8));
        setSpacing(4);
        setPrefWidth(180);
        setStyle("-fx-border-color: -fx-box-border; -fx-border-width: 0 1 0 0;");

        var pendingBadge = Labels.badge(viewModel.pendingOrderCountProperty());

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        var ordersContent = new HBox(new Label("Orders"), spacer, pendingBadge);
        ordersContent.setAlignment(Pos.CENTER_LEFT);

        var ordersBtn = new Button();
        ordersBtn.setGraphic(ordersContent);
        ordersBtn.setMaxWidth(Double.MAX_VALUE);
        ordersBtn.setAlignment(Pos.CENTER_LEFT);

        var customersBtn = new Button("Customers");
        customersBtn.setMaxWidth(Double.MAX_VALUE);
        customersBtn.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(ordersBtn, customersBtn);

        ordersBtn.setOnAction(e -> viewModel.openOrdersWorkspace());
        customersBtn.setOnAction(e -> viewModel.openCustomersWorkspace());
    }
}
