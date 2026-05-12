package mvvm.example.customers.detail;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

public class CustomerDetailView extends BorderPane {

    public CustomerDetailView(CustomerDetailViewModel viewModel) {
        var customer = viewModel.getCustomer();

        var form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(16));

        form.add(new Label("Name"),  0, 0);
        form.add(new Label(customer.name()),  1, 0);
        form.add(new Label("Email"), 0, 1);
        form.add(new Label(customer.email()), 1, 1);

        var backBtn = new Button("Back");

        setCenter(form);
        setBottom(backBtn);
        BorderPane.setMargin(backBtn, new Insets(8));

        backBtn.setOnAction(e -> viewModel.back());
    }
}
