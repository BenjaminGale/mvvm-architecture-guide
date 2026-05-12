package mvvm.example.settings;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SettingsView extends VBox {

    public SettingsView(SettingsViewModel viewModel) {
        setSpacing(8);
        setPadding(new Insets(16));

        var backBtn = new Button("Back to Orders");

        getChildren().addAll(new Label("Settings"), backBtn);

        backBtn.setOnAction(e -> viewModel.back());
    }
}
