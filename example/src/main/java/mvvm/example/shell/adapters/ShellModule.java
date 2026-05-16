package mvvm.example.shell.adapters;

import javafx.scene.Parent;
import javafx.stage.Window;
import mvvm.example.AppContext;
import mvvm.example.core.view.DialogManager;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;
import mvvm.example.shell.main.sidebar.SidebarView;
import mvvm.example.shell.main.statusbar.StatusBarView;
import mvvm.example.shell.main.statusbar.StatusBarViewModel;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;
import mvvm.example.shell.ShellContext;
import mvvm.example.shell.main.MainView;
import mvvm.example.shell.main.statusbar.StatusItemView;
import mvvm.example.shell.main.MainViewModel;
import mvvm.example.shell.main.sidebar.SidebarViewModel;

import java.util.Arrays;

public class ShellModule {

    private final AppContext appContext;
    private final ShellContext shell;

    public ShellModule(Window window) {
        this.appContext = new AppContext(
            new ViewLocator<>(),
            new DialogManager(
                window,
                new ViewLocator<>()
            )
        );

        this.shell = new ShellContext();

        var viewLocator = this.appContext.viewLocator();
        viewLocator.register(MainViewModel.class, vm -> new MainView(vm, viewLocator));
        viewLocator.register(StatusBarViewModel.class, vm -> new StatusBarView(vm, viewLocator));
        viewLocator.register(StatusItemViewModel.class, StatusItemView::new);
        viewLocator.register(SidebarViewModel.class, SidebarView::new);
    }

    public AppContext appContext() {
        return appContext;
    }

    public ShellContext context() {
        return shell;
    }

    public Parent mainView(SidebarItemViewModel... items) {
        shell.navigationItems().addAll(items);

        Arrays.stream(items)
            .findFirst()
            .ifPresent(item -> item.openWorkspaceAction().execute());

        return appContext.viewLocator().locate(mainViewModel());
    }

    private MainViewModel mainViewModel() {
        return new MainViewModel(
            new SidebarViewModel(shell.navigationItems()),
            new StatusBarViewModel(shell.statusItems()),
            shell.currentWorkspaceProperty()
        );
    }
}
