package mvvm.example.core.config;

import mvvm.example.core.view.ViewServices;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.orders.domain.commands.CopyOrderCommand;
import mvvm.example.orders.domain.commands.UpsertOrderCommand;
import mvvm.example.orders.domain.queries.GetOrderSummariesQuery;
import mvvm.example.orders.editor.OrderEditorHost;
import mvvm.example.orders.editor.OrderEditorRequest;
import mvvm.example.orders.editor.header.CustomerSelectorRequest;
import mvvm.example.orders.editor.header.CustomerSelectorDialog;
import mvvm.example.orders.editor.header.CustomerSelectorViewModel;
import mvvm.example.orders.editor.GetOrderEditorDataQuery;
import mvvm.example.orders.editor.OrderEditorData;
import mvvm.example.orders.editor.OrderEditorService;
import mvvm.example.orders.editor.OrderEditorView;
import mvvm.example.orders.editor.OrderEditorViewModel;
import mvvm.example.orders.editor.header.OrderHeaderView;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemEditorRequest;
import mvvm.example.orders.editor.lineitems.LineItemEditorDialog;
import mvvm.example.orders.editor.lineitems.LineItemEditorViewModel;
import mvvm.example.orders.editor.lineitems.ProductSelectorDialog;
import mvvm.example.orders.editor.lineitems.ProductSelectorRequest;
import mvvm.example.orders.editor.lineitems.ProductSelectorViewModel;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.stock.domain.ProductRepository;
import mvvm.example.orders.explorer.OrdersExplorerView;
import mvvm.example.orders.explorer.OrdersExplorerViewModel;
import mvvm.example.shell.ShellContext;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;
import mvvm.example.shell.main.statusbar.LabelType;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class OrdersModule {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ViewServices view;
    private final ShellContext shell;
    private final CopyOrderCommand copyOrderCommand;

    public OrdersModule(OrderRepository orderRepository, CustomerRepository customerRepository, ProductRepository productRepository, ViewServices view, ShellContext shell, CopyOrderCommand copyOrderCommand) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.view = view;
        this.shell = shell;
        this.copyOrderCommand = copyOrderCommand;

        view.viewLocator().register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
        view.viewLocator().register(OrderHeaderViewModel.class, OrderHeaderView::new);
        view.viewLocator().register(OrderEditorViewModel.class, vm -> new OrderEditorView(vm, view.viewLocator()));
        view.dialogManager().register(LineItemEditorViewModel.class, LineItemEditorDialog::dialog);
        view.dialogManager().register(CustomerSelectorViewModel.class, CustomerSelectorDialog::dialog);
        view.dialogManager().register(ProductSelectorViewModel.class, ProductSelectorDialog::dialog);
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
        var query = new GetOrderEditorDataQuery(orderRepository, customerRepository);
        return new OrderEditorViewModel(
            request,
            new OrderEditorService() {
                @Override public OrderEditorData fetch(OrderEditorRequest req) { return query.execute(req); }
                @Override public UUID save(UUID orderId, UUID customerId, String reference, LocalDate plannedShipDate, List<LineItem> lineItems) { return new UpsertOrderCommand(orderRepository).execute(orderId, customerId, reference, plannedShipDate, lineItems); }
                @Override public UUID copy(UUID orderId) { return copyOrderCommand.execute(orderId); }
                @Override public void delete(UUID orderId) { orderRepository.delete(orderId); }
            },
            new OrderEditorHost() {
                @Override public void returnToList() { shell.show(OrdersModule.this::ordersExplorerViewModel); }
                @Override public void openOrder(OrderEditorRequest req) { shell.show(() -> orderEditorViewModel(req)); }
            },
            req -> view.dialogManager().show(customerSelectorViewModel(req)),
            req -> view.dialogManager().show(editLineItemViewModel(req))
        );
    }

    private LineItemEditorViewModel editLineItemViewModel(LineItemEditorRequest request) {
        return new LineItemEditorViewModel(request, req -> view.dialogManager().show(productSelectorViewModel(req)));
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
