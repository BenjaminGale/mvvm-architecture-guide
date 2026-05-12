package mvvm.example.shell.main;

import mvvm.example.shell.sidebar.SidebarViewModel;

public class MainViewModel {

    private final SidebarViewModel sidebar;

    public MainViewModel(SidebarViewModel sidebar) {
        this.sidebar = sidebar;
    }

    public SidebarViewModel getSidebar() {
        return sidebar;
    }
}
