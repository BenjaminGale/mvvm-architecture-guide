package mvvm.example.core.config;

import mvvm.example.core.view.ViewServices;
import mvvm.example.shell.ShellContext;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;
import mvvm.example.stock.domain.ProductRepository;
import mvvm.example.stock.explorer.StockExplorerView;
import mvvm.example.stock.explorer.StockExplorerViewModel;

public class StockModule {

    private final ProductRepository productRepository;
    private final ViewServices view;
    private final ShellContext shell;

    public StockModule(ProductRepository productRepository, ViewServices view, ShellContext shell) {
        this.productRepository = productRepository;
        this.view = view;
        this.shell = shell;

        view.viewLocator().register(StockExplorerViewModel.class, StockExplorerView::new);
    }

    public SidebarItemViewModel sidebarItem() {
        return new SidebarItemViewModel("Stock", this::showExplorer);
    }

    public void showExplorer() {
        shell.show(this::stockExplorerViewModel);
    }

    private StockExplorerViewModel stockExplorerViewModel() {
        return new StockExplorerViewModel(productRepository::findAll);
    }
}
