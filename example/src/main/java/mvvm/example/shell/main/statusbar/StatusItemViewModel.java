package mvvm.example.shell.main.statusbar;

import javafx.beans.property.ReadOnlyIntegerProperty;

public class StatusItemViewModel {

    private final ReadOnlyIntegerProperty count;
    private final LabelType label;

    public StatusItemViewModel(ReadOnlyIntegerProperty count, LabelType label) {
        this.count = count;
        this.label = label;
    }

    public ReadOnlyIntegerProperty countProperty() {
        return count;
    }

    public LabelType label() {
        return label;
    }
}
