package mvvm.example.shell.main.statusbar;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;

public class StatusItemView extends Label {

    public StatusItemView(StatusItemViewModel viewModel) {
        textProperty().bind(
            Bindings.format(
                createFormat(viewModel),
                viewModel.countProperty()
            )
        );
    }

    private static String createFormat(StatusItemViewModel vm) {
        return switch (vm.label()) {
            case All_ORDERS -> "%d orders";
            case OVERDUE_ORDERS -> "%d overdue";
        };
    }
}
