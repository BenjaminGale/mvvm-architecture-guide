package mvvm.example.shell.main.statusbar;

import javafx.collections.ObservableList;

public class StatusBarViewModel {

    private final ObservableList<StatusItemViewModel> statusItems;

    public StatusBarViewModel(ObservableList<StatusItemViewModel> statusItems) {
        this.statusItems = statusItems;
    }

    public ObservableList<StatusItemViewModel> statusItems() {
        return statusItems;
    }
}
