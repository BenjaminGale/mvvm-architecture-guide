package mvvm.example.shell.main;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import mvvm.example.core.viewmodel.AppHost;
import mvvm.example.customers.detail.CustomerDetailViewModel;
import mvvm.example.customers.explorer.CustomersExplorerViewModel;
import mvvm.example.orders.editor.OrderEditorViewModel;
import mvvm.example.orders.explorer.OrdersExplorerViewModel;
import mvvm.example.settings.SettingsViewModel;
import mvvm.example.shell.sidebar.SidebarViewModel;

public class MainViewModel {

    private final SidebarViewModel sidebar;
    private final ObjectProperty<Object> activeViewModel = new SimpleObjectProperty<>();

    public MainViewModel(SidebarViewModel sidebar, AppHost host) {
        this.sidebar = sidebar;

        host.receive(OrdersExplorerViewModel.class, this::show);
        host.receive(OrderEditorViewModel.class, this::show);
        host.receive(CustomersExplorerViewModel.class, this::show);
        host.receive(CustomerDetailViewModel.class, this::show);
        host.receive(SettingsViewModel.class, this::show);
    }

    public SidebarViewModel getSidebar() {
        return sidebar;
    }

    private void show(Object viewModel) {
        activeViewModel.set(viewModel);
    }

    public ReadOnlyObjectProperty<Object> activeViewModelProperty() {
        return activeViewModel;
    }
}
