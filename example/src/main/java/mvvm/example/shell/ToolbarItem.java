package mvvm.example.shell;

import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.AsyncAction;

public sealed interface ToolbarItem {

    record Sync(String label, Action action) implements ToolbarItem {}

    record Async(String label, AsyncAction action) implements ToolbarItem {}
}
