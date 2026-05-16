package mvvm.example.shell.adapters;

import javafx.scene.Parent;
import javafx.stage.Window;
import mvvm.example.AppContext;
import mvvm.example.core.view.DialogManager;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.orders.context.OrderContext;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;
import mvvm.example.shell.WorkspaceContext;
import mvvm.example.shell.main.MainView;
import mvvm.example.shell.main.statusbar.StatusItemView;
import mvvm.example.shell.main.MainViewModel;
import mvvm.example.shell.sidebar.SidebarHost;
import mvvm.example.shell.sidebar.SidebarViewModel;

public class ShellModule {

    private final AppContext appContext;
    private final WorkspaceContext workspaceContext;

    public ShellModule(Window window) {
        this.appContext = new AppContext(
            new ViewLocator<>(),
            new DialogManager(
                window,
                new ViewLocator<>()
            )
        );

        this.workspaceContext = new WorkspaceContext();

        this.appContext.viewLocator().register(MainViewModel.class, vm -> new MainView(vm, this.appContext.viewLocator()));
        this.appContext.viewLocator().register(StatusItemViewModel.class, StatusItemView::new);
    }

    public AppContext appContext() {
        return appContext;
    }

    public WorkspaceContext workspaceContext() {
        return workspaceContext;
    }

    public Parent mainView(OrderContext orderContext, SidebarHost sidebarHost) {
        return appContext.viewLocator().locate(mainViewModel(orderContext, sidebarHost));
    }

    private MainViewModel mainViewModel(OrderContext orderContext, SidebarHost sidebarHost) {
        return new MainViewModel(
            new SidebarViewModel(orderContext, sidebarHost),
            workspaceContext
        );
    }
}
