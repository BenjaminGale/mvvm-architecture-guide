package mvvm.example.shell.sidebar;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SidebarView extends VBox {

    public SidebarView(SidebarViewModel viewModel) {
        setPadding(new Insets(8));
        setSpacing(4);
        setPrefWidth(180);
        setStyle("-fx-border-color: -fx-box-border; -fx-border-width: 0 1 0 0;");

        var pendingBadge = new Label();
        pendingBadge.textProperty().bind(
            Bindings.format("(%s)", viewModel.pendingOrderCountProperty().asString())
        );

        pendingBadge.visibleProperty().bind(
            Bindings.greaterThan(viewModel.pendingOrderCountProperty(), 0)
        );

        var ordersBtn = new Button("Orders");
        ordersBtn.setMaxWidth(Double.MAX_VALUE);
        ordersBtn.setAlignment(Pos.CENTER_LEFT);
        ordersBtn.setGraphic(pendingBadge);
        ordersBtn.setContentDisplay(ContentDisplay.RIGHT);

        var customersBtn = new Button("Customers");
        customersBtn.setMaxWidth(Double.MAX_VALUE);
        customersBtn.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(ordersBtn, customersBtn);

        ordersBtn.setOnAction(e -> viewModel.navigateToOrders());
        customersBtn.setOnAction(e -> viewModel.navigateToCustomers());
    }
}
