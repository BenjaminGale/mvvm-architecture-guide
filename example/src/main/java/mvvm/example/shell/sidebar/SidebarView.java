package mvvm.example.shell.sidebar;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SidebarView extends VBox {

    public SidebarView(SidebarViewModel viewModel) {
        setPadding(new Insets(8));
        setSpacing(4);
        setPrefWidth(180);

        var title = new Label("Order Manager");
        VBox.setMargin(title, new Insets(0, 0, 8, 0));

        var pendingBadge = new Label();
        pendingBadge.textProperty().bind(viewModel.pendingOrderCountProperty().asString());
        pendingBadge.visibleProperty().bind(
            Bindings.greaterThan(viewModel.pendingOrderCountProperty(), 0)
        );

        var ordersBtn = new Button("Orders");
        ordersBtn.setMaxWidth(Double.MAX_VALUE);
        ordersBtn.setAlignment(Pos.CENTER_LEFT);

        var ordersRow = new HBox(8, ordersBtn, pendingBadge);
        ordersRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(ordersBtn, Priority.ALWAYS);

        var customersBtn = new Button("Customers");
        customersBtn.setMaxWidth(Double.MAX_VALUE);
        customersBtn.setAlignment(Pos.CENTER_LEFT);

        var settingsBtn = new Button("Settings");
        settingsBtn.setMaxWidth(Double.MAX_VALUE);
        settingsBtn.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(title, ordersRow, customersBtn, settingsBtn);

        ordersBtn.setOnAction(e -> viewModel.navigateToOrders());
        customersBtn.setOnAction(e -> viewModel.navigateToCustomers());
        settingsBtn.setOnAction(e -> viewModel.navigateToSettings());
    }
}
