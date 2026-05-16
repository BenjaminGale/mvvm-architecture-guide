package mvvm.example.shell.main.sidebar;

import javafx.collections.ObservableList;

public class SidebarViewModel {

    private final ObservableList<SidebarItemViewModel> navigationItems;

    public SidebarViewModel(ObservableList<SidebarItemViewModel> navigationItems) {
        this.navigationItems = navigationItems;
    }

    public ObservableList<SidebarItemViewModel> navigationItems() {
        return navigationItems;
    }
}
