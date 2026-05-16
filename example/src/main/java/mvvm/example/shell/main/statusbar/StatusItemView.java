package mvvm.example.shell.main.statusbar;

import javafx.scene.control.Label;

public class StatusItemView extends Label {

    public StatusItemView(StatusItemViewModel vm) {
        var label = switch (vm.label()) {
            case All_ORDERS -> "orders";
            case OVERDUE_ORDERS -> "overdue";
        };
        textProperty().bind(vm.countProperty().asString().concat(" " + label));
    }
}
