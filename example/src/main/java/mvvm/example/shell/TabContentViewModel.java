package mvvm.example.shell;

import mvvm.example.shell.main.statusbar.StatusItemViewModel;

import java.util.List;

public class TabContentViewModel {

    private final Object viewModel;
    private List<ToolbarItem> toolbarActions = List.of();
    private List<StatusItemViewModel> statusItems = List.of();

    public TabContentViewModel(Object viewModel) {
        this.viewModel = viewModel;
    }

    public TabContentViewModel withToolbarActions(List<ToolbarItem> toolbarActions) {
        this.toolbarActions = toolbarActions;
        return this;
    }

    public TabContentViewModel withStatusItems(List<StatusItemViewModel> statusItems) {
        this.statusItems = statusItems;
        return this;
    }

    public Object viewModel() {
        return viewModel;
    }

    public List<ToolbarItem> toolbarActions() {
        return toolbarActions;
    }

    public List<StatusItemViewModel> statusItems() {
        return statusItems;
    }
}
