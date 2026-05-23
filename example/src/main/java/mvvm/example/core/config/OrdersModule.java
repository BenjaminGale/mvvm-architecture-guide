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

        view.viewLocator().register(mvvm.example.orders.editor2.header.OrderHeaderViewModel.class, mvvm.example.orders.editor2.header.OrderHeaderView::new);
        view.viewLocator().register(mvvm.example.orders.editor2.OrderEditorViewModel.class, vm -> new mvvm.example.orders.editor2.OrderEditorView(vm, view.viewLocator()));
        view.dialogManager().register(mvvm.example.orders.editor2.lineitems.LineItemEditorViewModel.class, mvvm.example.orders.editor2.lineitems.LineItemEditorView::dialog);
        view.dialogManager().register(mvvm.example.orders.editor2.lineitems.ProductSelectorViewModel.class, mvvm.example.orders.editor2.lineitems.ProductSelectorView::dialog);
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
            request -> shell.show(() -> orderEditorViewModelV2(request))
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

    private mvvm.example.orders.editor2.OrderEditorViewModel orderEditorViewModelV2(OrderEditorRequest request) {
        var query = new mvvm.example.orders.editor2.GetOrderEditorDataQuery(orderRepository, customerRepository, stockRepository);
        return new mvvm.example.orders.editor2.OrderEditorViewModel(
            request,
            new mvvm.example.orders.editor2.OrderEditorService() {
                @Override public mvvm.example.orders.editor2.OrderEditorData fetch(OrderEditorRequest req) { return query.execute(req); }
                @Override public String save(String orderId, String customerId, String reference, java.time.LocalDate plannedShipDate, java.util.List<mvvm.example.orders.domain.LineItem> lineItems) { return new UpsertOrderCommand(orderRepository).execute(orderId, customerId, reference, plannedShipDate, lineItems); }
                @Override public String copy(String orderId) { return copyOrderCommand.copy(orderId); }
                @Override public void delete(String orderId) { orderRepository.delete(orderId); }
            },
            new OrderEditorHost() {
                @Override public void returnToList() { shell.show(OrdersModule.this::ordersExplorerViewModel); }
                @Override public void openOrder(OrderEditorRequest req) { shell.show(() -> orderEditorViewModelV2(req)); }
            },
            req -> view.dialogManager().show(customerSelectorViewModel(req)),
            req -> view.dialogManager().show(editLineItemViewModelV2(req))
        );
    }

    private mvvm.example.orders.editor2.lineitems.LineItemEditorViewModel editLineItemViewModelV2(
        mvvm.example.orders.editor2.lineitems.LineItemEditorRequest request
    ) {
        return new mvvm.example.orders.editor2.lineitems.LineItemEditorViewModel(
            request,
            req -> view.dialogManager().show(productSelectorViewModelV2(req))
        );
    }

    private mvvm.example.orders.editor2.lineitems.ProductSelectorViewModel productSelectorViewModelV2(
        mvvm.example.orders.editor2.lineitems.ProductSelectorRequest request
    ) {
        return new mvvm.example.orders.editor2.lineitems.ProductSelectorViewModel(request, productRepository.findAll());
    }

    private StatusItemViewModel ordersCountStatusItem(OrdersExplorerViewModel vm) {
        return new StatusItemViewModel(vm.ordersCountProperty(), LabelType.All_ORDERS);
    }

    private StatusItemViewModel overdueOrdersStatusItem(OrdersExplorerViewModel vm) {
        return new StatusItemViewModel(vm.overdueOrdersCountProperty(), LabelType.OVERDUE_ORDERS);
    }
}
