package mvvm.example.orders.editor;

import mvvm.example.orders.MockOrders;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderStatus;
import mvvm.example.orders.domain.queries.LineItemSummary;
import mvvm.example.orders.editor.header.OrderHeaderService;
import mvvm.example.orders.editor.header.OrderHeaderSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Orders.OrderEditorViewModel")
class OrderEditorViewModelTest {

    private static OrderHeaderService headerServiceFor(Order order) {
        var customer = order.customerId() != null ? MockOrders.ACME_CUSTOMER : null;
        var summary = new OrderHeaderSummary(order.createdDate(), order.status(), customer, order.plannedShipDate(), order.reference());
        return req -> summary;
    }

    private static OrderHeaderService emptyHeaderService() {
        var summary = new OrderHeaderSummary(LocalDate.now(), OrderStatus.PENDING, null, LocalDate.now(), "");
        return req -> summary;
    }

    private static OrderEditorService serviceFor(Order order) {
        var service = mock(OrderEditorService.class);
        when(service.fetchOrder(order.id())).thenReturn(order);
        when(service.fetchLineItemSummaries(any(), any())).thenAnswer(inv -> {
            List<mvvm.example.orders.domain.LineItem> items = inv.getArgument(0);
            var summaries = items.stream()
                .map(i -> new LineItemSummary(i.productId(), i.description(), i.quantity(), i.unitPrice(), 0))
                .toList();
            return CompletableFuture.completedFuture(summaries);
        });
        return service;
    }

    private static OrderEditorViewModel vmFor(Order order) {
        return new OrderEditorViewModel(EditOrderRequest.of(order.id()), headerServiceFor(order), serviceFor(order), mock(OrderEditorHost.class));
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
        @DisplayName("the order is added to storage")
        void orderIsPersisted() {
            var order = MockOrders.validOrderWithLineItems();
            var service = serviceFor(order);
            var vm = new OrderEditorViewModel(EditOrderRequest.of(order.id()), headerServiceFor(order), service, mock(OrderEditorHost.class));

            vm.save.executeAsync(Runnable::run).join();

            verify(service).saveOrder(any(Order.class));
        }
    }

    @Nested
    @DisplayName("when the order is deleted")
    class WhenDeleted {

        @Test
        @DisplayName("the order is removed from storage")
        void orderIsRemoved() {
            var order = MockOrders.validOrderWithLineItems();
            var service = serviceFor(order);
            var vm = new OrderEditorViewModel(EditOrderRequest.of(order.id()), headerServiceFor(order), service, mock(OrderEditorHost.class));

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
            var service = serviceFor(order);
            when(service.copyOrder(order.id())).thenReturn("copied-" + order.id());
            var host = mock(OrderEditorHost.class);
            var vm = new OrderEditorViewModel(EditOrderRequest.of(order.id()), headerServiceFor(order), service, host);

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
            var service = mock(OrderEditorService.class);
            when(service.fetchLineItemSummaries(any(), any())).thenReturn(CompletableFuture.completedFuture(List.of()));
            var vm = new OrderEditorViewModel(EditOrderRequest.forNewOrder(), emptyHeaderService(), service, mock(OrderEditorHost.class));

            assertFalse(vm.save.canExecute());
        }

        @Test
        @DisplayName("does not fetch an order from the service")
        void doesNotFetchOrder() {
            var service = mock(OrderEditorService.class);
            when(service.fetchLineItemSummaries(any(), any())).thenReturn(CompletableFuture.completedFuture(List.of()));
            new OrderEditorViewModel(EditOrderRequest.forNewOrder(), emptyHeaderService(), service, mock(OrderEditorHost.class));

            verify(service, never()).fetchOrder(any());
        }
    }

    @Nested
    @DisplayName("when the customer selector is triggered")
    class WhenCustomerSelectorTriggered {

        @Test
        @DisplayName("the host is asked to show the customer selector")
        void hostShowsCustomerSelector() {
            var order = MockOrders.validOrderWithLineItems();
            var host = mock(OrderEditorHost.class);
            var vm = new OrderEditorViewModel(EditOrderRequest.of(order.id()), headerServiceFor(order), serviceFor(order), host);

            vm.getHeader().selectCustomer.execute();

            verify(host).showCustomerSelector(any());
        }
    }

    @Nested
    @DisplayName("when the updated order is built")
    class WhenUpdatedOrderIsBuilt {

        @Test
        @DisplayName("the order reflects the current header and line item values")
        void orderReflectsCurrentValues() {
            var vm = vmFor(MockOrders.validOrderWithLineItems());

            var updated = vm.buildUpdatedOrder();

            assertEquals(MockOrders.ACME_CUSTOMER_ID, updated.customerId());
            assertEquals(1, updated.lineItems().size());
        }
    }
}
