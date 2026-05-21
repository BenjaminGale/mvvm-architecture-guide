package mvvm.example.orders.editor;

import mvvm.example.orders.MockOrders;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderStatus;
import mvvm.example.orders.domain.queries.LineItemSummary;
import mvvm.example.orders.editor.header.OrderHeaderHost;
import mvvm.example.orders.domain.queries.OrderHeaderSummary;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsExplorerViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsHost;
import mvvm.example.orders.editor.lineitems.LineItemsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Orders.OrderEditorViewModel")
class OrderEditorViewModelTest {

    private static OrderHeaderViewModel headerVmFor(Order order) {
        var customer = order.customerId() != null ? MockOrders.ACME_CUSTOMER : null;
        var summary = new OrderHeaderSummary(order.createdDate(), order.status(), customer, order.plannedShipDate(), order.reference());
        return new OrderHeaderViewModel(EditOrderRequest.of(order.id()), req -> summary, mock(OrderHeaderHost.class));
    }

    private static LineItemsExplorerViewModel lineItemsVmFor(Order order) {
        var service = mock(LineItemsService.class);
        when(service.fetchLineItems(any())).thenReturn(order.lineItems());
        when(service.fetchSummaries(any(), any())).thenAnswer(inv -> {
            List<LineItem> items = inv.getArgument(0);
            var summaries = items.stream()
                .map(i -> new LineItemSummary(i.productId(), i.description(), i.quantity(), i.unitPrice(), 0))
                .toList();
            return CompletableFuture.completedFuture(summaries);
        });
        return new LineItemsExplorerViewModel(EditOrderRequest.of(order.id()), service, mock(LineItemsHost.class));
    }

    private static OrderEditorViewModel vmFor(Order order) {
        return new OrderEditorViewModel(
            EditOrderRequest.of(order.id()),
            headerVmFor(order),
            lineItemsVmFor(order),
            mock(OrderEditorService.class),
            mock(OrderEditorHost.class)
        );
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("canSave is false when no customer is selected")
        void canSaveIsFalseWhenNoCustomer() {
            var vm = vmFor(MockOrders.orderWithNoCustomer());

            assertFalse(vm.save.canExecute());
        }

        @Test
        @DisplayName("canSave is false when there are no line items")
        void canSaveIsFalseWhenNoLineItems() {
            var vm = vmFor(MockOrders.orderWithNoLineItems());

            assertFalse(vm.save.canExecute());
        }

        @Test
        @DisplayName("canSave is true when the header is valid and line items are present")
        void canSaveIsTrueWhenValid() {
            var vm = vmFor(MockOrders.validOrderWithLineItems());

            assertTrue(vm.save.canExecute());
        }
    }

    @Nested
    @DisplayName("when fields are edited")
    class WhenFieldsAreEdited {

        @Test
        @DisplayName("canSave becomes true when a customer is selected")
        void canSaveBecomesTrue() {
            var vm = vmFor(MockOrders.orderWithNoCustomer());

            vm.getHeader().selectedCustomerProperty().set(MockOrders.ACME_CUSTOMER);

            assertTrue(vm.save.canExecute());
        }

        @Test
        @DisplayName("canSave becomes false when the last line item is removed")
        void canSaveBecomesFalseWhenLastItemRemoved() {
            var vm = vmFor(MockOrders.validOrderWithLineItems());
            vm.getLineItems().selectedItemProperty().set(vm.getLineItems().items().getFirst());

            vm.getLineItems().deleteItemAction().execute();

            assertFalse(vm.save.canExecute());
        }
    }

    @Nested
    @DisplayName("when the order is saved")
    class WhenSaved {

        @Test
        @DisplayName("the order is persisted via the service")
        void orderIsPersisted() {
            var order = MockOrders.validOrderWithLineItems();
            var service = mock(OrderEditorService.class);
            var vm = new OrderEditorViewModel(
                EditOrderRequest.of(order.id()),
                headerVmFor(order),
                lineItemsVmFor(order),
                service,
                mock(OrderEditorHost.class)
            );

            vm.save.executeAsync(Runnable::run).join();

            verify(service).upsert(eq(order.id()), eq(MockOrders.ACME_CUSTOMER_ID), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when the order is deleted")
    class WhenDeleted {

        @Test
        @DisplayName("the order is removed from storage")
        void orderIsRemoved() {
            var order = MockOrders.validOrderWithLineItems();
            var service = mock(OrderEditorService.class);
            var vm = new OrderEditorViewModel(
                EditOrderRequest.of(order.id()),
                headerVmFor(order),
                lineItemsVmFor(order),
                service,
                mock(OrderEditorHost.class)
            );

            vm.delete.execute();

            verify(service).deleteOrder(order.id());
        }
    }

    @Nested
    @DisplayName("when the order is copied")
    class WhenCopied {

        @Test
        @DisplayName("the copied order is shown")
        void copiedOrderIsShown() {
            var order = MockOrders.validOrderWithLineItems();
            var service = mock(OrderEditorService.class);
            when(service.copyOrder(order.id())).thenReturn("copied-" + order.id());
            var host = mock(OrderEditorHost.class);
            var vm = new OrderEditorViewModel(
                EditOrderRequest.of(order.id()),
                headerVmFor(order),
                lineItemsVmFor(order),
                service,
                host
            );

            vm.copy.execute();

            verify(service).copyOrder(order.id());
            verify(host).openOrder(EditOrderRequest.of("copied-" + order.id()));
        }
    }

    @Nested
    @DisplayName("when created for a new order")
    class WhenCreatedForNewOrder {

        @Test
        @DisplayName("canSave is false when no fields have been filled")
        void canSaveIsFalseInitially() {
            var emptySummary = new OrderHeaderSummary(LocalDate.now(), OrderStatus.PENDING, null, LocalDate.now(), "");
            var lineItemsService = mock(LineItemsService.class);
            when(lineItemsService.fetchLineItems(any())).thenReturn(List.of());
            when(lineItemsService.fetchSummaries(any(), any())).thenReturn(CompletableFuture.completedFuture(List.of()));

            var vm = new OrderEditorViewModel(
                EditOrderRequest.forNewOrder(),
                new OrderHeaderViewModel(EditOrderRequest.forNewOrder(), req -> emptySummary, mock(OrderHeaderHost.class)),
                new LineItemsExplorerViewModel(EditOrderRequest.forNewOrder(), lineItemsService, mock(LineItemsHost.class)),
                mock(OrderEditorService.class),
                mock(OrderEditorHost.class)
            );

            assertFalse(vm.save.canExecute());
        }
    }

    @Nested
    @DisplayName("when the customer selector is triggered")
    class WhenCustomerSelectorTriggered {

        @Test
        @DisplayName("the header host is asked to show the customer selector")
        void headerHostShowsCustomerSelector() {
            var order = MockOrders.validOrderWithLineItems();
            var headerHost = mock(OrderHeaderHost.class);
            var summary = new OrderHeaderSummary(order.createdDate(), order.status(), MockOrders.ACME_CUSTOMER, order.plannedShipDate(), order.reference());
            var header = new OrderHeaderViewModel(EditOrderRequest.of(order.id()), req -> summary, headerHost);
            var vm = new OrderEditorViewModel(
                EditOrderRequest.of(order.id()),
                header,
                lineItemsVmFor(order),
                mock(OrderEditorService.class),
                mock(OrderEditorHost.class)
            );

            vm.getHeader().selectCustomer.execute();

            verify(headerHost).showCustomerSelector(any());
        }
    }
}
