package mvvm.example.shell.tabs;

import javafx.beans.property.ReadOnlyStringProperty;
import mvvm.example.core.viewmodel.Action;

import java.util.function.BooleanSupplier;

public class TabViewModel {

    public static TabViewModel unclosable(ReadOnlyStringProperty title, TabContentViewModel content) {
        return new TabViewModel(title, content, Action.disabled());
    }

    public static TabViewModel closable(ReadOnlyStringProperty title, TabContentViewModel content) {
        return closable(title, content, () -> true);
    }

    public static TabViewModel closable(ReadOnlyStringProperty title, TabContentViewModel content, BooleanSupplier canClose) {
        return new TabViewModel(title, content, canClose);
    }

    private final ReadOnlyStringProperty title;
    private final TabContentViewModel content;
    private Action.Listener onClose = () -> {};
    private final Action closeAction;

    private TabViewModel(ReadOnlyStringProperty title, TabContentViewModel content, Action closeAction) {
        this.title = title;
        this.content = content;
        this.closeAction = closeAction;
    }

    private TabViewModel(ReadOnlyStringProperty title, TabContentViewModel content, BooleanSupplier canClose) {
        this.title = title;
        this.content = content;
        this.closeAction = new Action(() -> {
            if (canClose.getAsBoolean()) onClose.actionExecuted();
        });
    }

    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    public TabContentViewModel content() {
        return content;
    }

    public Action closeAction() {
        return closeAction;
    }

    public void onClose(Action.Listener listener) {
        this.onClose = listener;
    }
}
