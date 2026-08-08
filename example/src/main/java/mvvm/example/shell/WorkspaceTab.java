package mvvm.example.shell;

import javafx.beans.property.ReadOnlyStringProperty;
import mvvm.example.core.viewmodel.Action;

public interface WorkspaceTab {

    ReadOnlyStringProperty titleProperty();

    Action closeAction();

    void onClose(Action.Listener listener);
}
