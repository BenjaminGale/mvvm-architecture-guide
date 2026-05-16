package mvvm.example.core.view;

import javafx.scene.layout.Region;

public record ViewServices(
    ViewLocator<Region> viewLocator,
    DialogManager dialogManager) {
}
