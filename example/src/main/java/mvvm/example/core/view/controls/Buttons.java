package mvvm.example.core.view.controls;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.AsyncAction;

public class Buttons {

    public static Button button(ObservableValue<String> label, Action action) {
        var button = new Button();
        button.textProperty().bind(label);
        bind(button, action);
        return button;
    }

    public static Button button(String label, Action action) {
        var button = new Button(label);
        bind(button, action);
        return button;
    }

    public static Button button(String label, AsyncAction action) {
        var button = new Button(label);
        bind(button, action);
        return button;
    }

    public static void bind(Button button, Action action) {
        button.disableProperty().bind(action.canExecuteProperty().not());
        button.setOnAction(e -> action.execute());
    }

    public static void bind(Button button, AsyncAction action) {
        button.disableProperty().bind(action.canExecuteProperty().not());
        button.setOnAction(e -> action.executeAsync(Platform::runLater));
    }

    private Buttons() {}
}
