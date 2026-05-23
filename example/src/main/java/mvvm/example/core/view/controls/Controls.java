package mvvm.example.core.view.controls;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;

public class Controls {

    public static void onAttached(Node node, Runnable action) {
        node.sceneProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Scene> obs, Scene old, Scene scene) {
                if (scene != null) {
                    action.run();
                    node.sceneProperty().removeListener(this);
                }
            }
        });
    }

    public static void focusOnShow(Node node) {
        onAttached(node, () -> Platform.runLater(node::requestFocus));
    }

    private Controls() {}
}
