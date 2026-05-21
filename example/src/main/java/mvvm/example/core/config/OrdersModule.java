package mvvm.example.core.config;

import mvvm.example.core.view.ViewServices;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.orders.domain.commands.CopyOrderCommand;
import mvvm.example.stock.domain.commands.DeleteStockAllocationsCommand;
import mvvm.example.orders.domain.queries.GetLineItemSummariesQuery;
import mvvm.example.orders.domain.queries.GetOrderHeaderSummaryQuery;
import mvvm.example.orders.domain.queries.GetOrderSummariesQuery;
import mvvm.example.orders.domain.queries.OrderLineItemsService;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.stock.domain.ProductRepository;
import mvvm.example.stock.domain.StockRepository;
import mvvm.example.orders.domain.commands.UpsertOrderCommand;
import mvvm.example.orders.editor.*;
import mvvm.example.orders.editor.header.CustomerSelectorView;
import mvvm.example.orders.editor.header.CustomerSelectorViewModel;
import mvvm.example.orders.editor.header.CustomerSelectorRequest;
import mvvm.example.orders.editor.OrderEditorRequest;
import mvvm.example.orders.editor.lineitems.LineItemEditorRequest;
import mvvm.example.orders.editor.lineitems.LineItemEditorView;
import mvvm.example.orders.editor.lineitems.LineItemEditorViewModel;
import mvvm.example.orders.editor.lineitems.ProductSelectorView;
import mvvm.example.orders.editor.lineitems.ProductSelectorViewModel;
import mvvm.example.orders.editor.lineitems.ProductSelectorRequest;
import mvvm.example.orders.editor.header.OrderHeaderView;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsExplorerView;
import mvvm.example.orders.editor.lineitems.LineItemsExplorerViewModel;
import mvvm.example.orders.explorer.OrdersExplorerView;
import mvvm.example.orders.explorer.OrdersExplorerViewModel;
import mvvm.example.shell.ShellContext;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;
import mvvm.example.shell.main.statusbar.LabelType;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;

import java.time.LocalDate;
import java.util.List;

public class OrdersModule {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final ViewServices view;
    private final ShellContext shell;
    private final CopyOrderCommand copyOrderCommand;
    private final DeleteStockAllocationsCommand deleteStockAllocationsCommand;

    public OrdersModule(OrderRepository orderRepository, CustomerRepository customerRepository, ProductRepository productRepository, StockRepository stockRepository, ViewServices view, ShellContext shell, CopyOrderCommand copyOrderCommand, DeleteStockAllocationsCommand deleteStockAllocationsCommand) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.view = view;
        this.shell = shell;
        this.copyOrderCommand = copyOrderCommand;
        this.deleteStockAllocationsCommand = deleteStockAllocationsCommand;

        view.viewLocator().register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
        view.viewLocator().register(OrderHeaderViewModel.class, OrderHeaderView::new);
        view.viewLocator().register(LineItemsExplorerViewModel.class, LineItemsExplorerView::new);
        view.viewLocator().register(OrderEditorViewModel.class, vm -> new OrderEditorView(vm, view.viewLocator()));
        view.dialogManager().register(LineItemEditorViewModel.class, LineItemEditorView::dialog);
        view.dialogManager().register(CustomerSelectorViewModel.class, CustomerSelectorView::dialog);
        view.dialogManager().register(ProductSelectorViewModel.class, ProductSelectorView::dialog);
    }

    public SidebarItemViewModel sidebarItem() {
        return new SidebarItemViewModel("Orders", this::showExplorer);
    }

    public void showExplorer() {
        shell.show(this::ordersExplorerViewModel);
    }

    public OrdersExplorerViewModel ordersExplorerViewModel() {
        var vm = new OrdersExplorerViewModel(
            new GetOrderSummariesQuery(orderRepository, customerRepository)::execute,
            request -> shell.show(() -> orderEditorViewModel(request))
        );

        shell.statusItems().addAll(
            ordersCountStatusItem(vm),
            overdueOrdersStatusItem(vm)
        );

        return vm;
    }

    private OrderEditorViewModel orderEditorViewModel(OrderEditorRequest request) {
        return new OrderEditorViewModel(
            request,
            orderHeaderViewModel(request),
            lineItemsExplorerViewModel(request),
            new OrderEditorService() {
                @Override public void upsert(String orderId, String customerId, String reference, LocalDate plannedShipDate, List<LineItem> items) { new UpsertOrderCommand(orderRepository).execute(orderId, customerId, reference, plannedShipDate, items); }
                @Override public String copyOrder(String orderId) { return copyOrderCommand.copy(orderId); }
                @Override public void deleteOrder(String orderId) { orderRepository.delete(orderId); }
            },
            new OrderEditorHost() {
                @Override public void returnToList() { shell.show(OrdersModule.this::ordersExplorerViewModel); }
                @Override public void openOrder(OrderEditorRequest req) { shell.show(() -> orderEditorViewModel(req)); }
            }
        );
    }

    private OrderHeaderViewModel orderHeaderViewModel(OrderEditorRequest request) {
        return new OrderHeaderViewModel(
            request,
            new GetOrderHeaderSummaryQuery(orderRepository, customerRepository),
            req -> view.dialogManager().show(customerSelectorViewModel(req))
        );
    }

    private LineItemsExplorerViewModel lineItemsExplorerViewModel(OrderEditorRequest request) {
        return new LineItemsExplorerViewModel(
            request,
            new OrderLineItemsService(orderRepository, new GetLineItemSummariesQuery(productRepository, stockRepository), deleteStockAllocationsCommand),
            req -> view.dialogManager().show(editItemViewModel(req))
        );
    }

    private LineItemEditorViewModel editItemViewModel(LineItemEditorRequest request) {
        return new LineItemEditorViewModel(request, r -> view.dialogManager().show(productSelectorViewModel(r)));
    }

    private CustomerSelectorViewModel customerSelectorViewModel(CustomerSelectorRequest request) {
        return new CustomerSelectorViewModel(request, customerRepository.findAll());
    }

    private ProductSelectorViewModel productSelectorViewModel(ProductSelectorRequest request) {
        return new ProductSelectorViewModel(request, productRepository.findAll());
    }

    private StatusItemViewModel ordersCountStatusItem(OrdersExplorerViewModel vm) {
        return new StatusItemViewModel(vm.ordersCountProperty(), LabelType.All_ORDERS);
    }

    private StatusItemViewModel overdueOrdersStatusItem(OrdersExplorerViewModel vm) {
        return new StatusItemViewModel(vm.overdueOrdersCountProperty(), LabelType.OVERDUE_ORDERS);
    }
}
