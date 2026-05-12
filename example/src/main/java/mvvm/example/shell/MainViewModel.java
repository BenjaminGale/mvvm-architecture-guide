package mvvm.example.shell;

public class MainViewModel {

    private final SidebarViewModel sidebar;

    public MainViewModel(SidebarViewModel sidebar) {
        this.sidebar = sidebar;
    }

    public SidebarViewModel getSidebar() {
        return sidebar;
    }
}
