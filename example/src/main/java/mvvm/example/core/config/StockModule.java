package mvvm.example.core.config;

import javafx.beans.property.ReadOnlyStringWrapper;
import mvvm.example.core.view.ViewServices;
import mvvm.example.shell.tabs.TabContentViewModel;
import mvvm.example.shell.tabs.TabViewModel;
import mvvm.example.shell.workspace.WorkspaceViewModel;
import mvvm.example.stock.domain.ProductRepository;
import mvvm.example.stock.explorer.StockExplorerView;
import mvvm.example.stock.explorer.StockExplorerViewModel;

public class StockModule {

    private static final Object EXPLORER_KEY = new Object();

    private final ProductRepository productRepository;
    private final ViewServices view;
    private final WorkspaceViewModel workspace;

    public StockModule(ProductRepository productRepository, ViewServices view) {
        this.productRepository = productRepository;
        this.view = view;

        view.viewLocator().register(StockExplorerViewModel.class, StockExplorerView::new);

        this.workspace = new WorkspaceViewModel("Stock");
        workspace.openTab(EXPLORER_KEY, this::stockExplorerTab);
    }

    public WorkspaceViewModel workspace() {
        return workspace;
    }

    private TabViewModel stockExplorerTab() {
        var vm = new StockExplorerViewModel(productRepository::findAll);

        return TabViewModel.unclosable(
            new ReadOnlyStringWrapper("Stock").getReadOnlyProperty(),
            new TabContentViewModel(vm)
        );
    }
}
