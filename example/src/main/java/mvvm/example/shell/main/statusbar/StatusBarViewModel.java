package mvvm.example.shell.main.statusbar;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class StatusBarViewModel {

    private ObservableList<StatusItemViewModel> statusItems = FXCollections.observableArrayList();

    public StatusBarViewModel(ObservableList<StatusItemViewModel> statusItems) {
        this.statusItems = statusItems;
    }

    public ObservableList<StatusItemViewModel> statusItems() {
        return statusItems;
    }
}
