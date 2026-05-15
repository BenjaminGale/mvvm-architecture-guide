package mvvm.example.core.view;

import javafx.application.Platform;
import javafx.scene.control.Button;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.AsyncAction;

public class Buttons {

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
