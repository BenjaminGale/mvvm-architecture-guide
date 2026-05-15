package mvvm.example;

import javafx.scene.layout.Region;
import mvvm.example.core.view.DialogManager;
import mvvm.example.core.view.ViewLocator;

public record AppContext(
    ViewLocator<Region> viewLocator,
    DialogManager dialogManager) {
}
