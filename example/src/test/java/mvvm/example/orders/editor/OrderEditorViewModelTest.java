package mvvm.example.orders.editor;

import mvvm.example.orders.MockOrders;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.editor.header.CustomerSelectorRequest;
import mvvm.example.orders.editor.lineitems.LineItemEditorRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Orders.OrderEditorViewModel")
class OrderEditorViewModelTest {

    private static OrderEditorService serviceFor(Order order) {
        var service = mock(OrderEditorService.class);
        var customer = order.customerId() != null ? MockOrders.ACME_CUSTOMER : null;
        when(service.fetch(any())).thenReturn(new OrderEditorData(order, customer, Map.of()));
        return service;
    }

    private static OrderEditorViewModel vmFor(Order order) {
        return new OrderEditorViewModel(
            OrderEditorRequest.of(order.id()),
            serviceFor(order),
            mock(OrderEditorHost.class),
            req -> {},
            req -> {}
        );
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("canSave is false when no customer is selected")
        void canSaveIsFalseWhenNoCustomer() {
            var vm = vmFor(MockOrders.orderWithNoCustomer());

            assertFalse(vm.saveAction.canExecute());
        }

        @Test
        @DisplayName("canSave is false when there are no line items")
        void canSaveIsFalseWhenNoLineItems() {
            var vm = vmFor(MockOrders.orderWithNoLineItems());

            assertFalse(vm.saveAction.canExecute());
        }

        @Test
        @DisplayName("canSave is true when the header is valid and line items are present")
        void canSaveIsTrueWhenValid() {
            var vm = vmFor(MockOrders.validOrderWithLineItems());

            assertTrue(vm.saveAction.canExecute());
        }
    }

    @Nested
    @DisplayName("when fields are edited")
    class WhenFieldsAreEdited {

        @Test
        @DisplayName("canSave becomes true when a customer is selected")
        void canSaveBecomesTrue() {
            var vm = vmFor(MockOrders.orderWithNoCustomer());

            vm.header().selectedCustomerProperty().set(MockOrders.ACME_CUSTOMER);

            assertTrue(vm.saveAction.canExecute());
        }

        @Test
        @DisplayName("canSave becomes false when the last line item is removed")
        void canSaveBecomesFalseWhenLastItemRemoved() {
            var vm = vmFor(MockOrders.validOrderWithLineItems());

            vm.lineItems().getFirst().deleteAction.execute();

            assertFalse(vm.saveAction.canExecute());
        }
    }

    @Nested
    @DisplayName("when the order is saved")
    class WhenSaved {

        @Test
        @DisplayName("the order is persisted via the service")
        void orderIsPersisted() {
            var order = MockOrders.validOrderWithLineItems();
            var service = serviceFor(order);
            var vm = new OrderEditorViewModel(
                OrderEditorRequest.of(order.id()),
                service,
                mock(OrderEditorHost.class),
                req -> {},
                req -> {}
            );

            vm.saveAction.executeAsync(Runnable::run).join();

            verify(service).save(eq(order.id()), eq(MockOrders.ACME_CUSTOMER_ID), any(), any(), any());
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
            var vm = new OrderEditorViewModel(
                OrderEditorRequest.of(order.id()),
                service,
                mock(OrderEditorHost.class),
                req -> {},
                req -> {}
            );

            vm.deleteOrderAction.execute();

            verify(service).delete(order.id());
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
            when(service.copy(order.id())).thenReturn("copied-" + order.id());
            var host = mock(OrderEditorHost.class);
            var vm = new OrderEditorViewModel(
                OrderEditorRequest.of(order.id()),
                service,
                host,
                req -> {},
                req -> {}
            );

            vm.copyAction.execute();

            verify(service).copy(order.id());
            verify(host).openOrder(OrderEditorRequest.of("copied-" + order.id()));
        }
    }

    @Nested
    @DisplayName("when created for a new order")
    class WhenCreatedForNewOrder {

        @Test
        @DisplayName("canSave is false when no fields have been filled")
        void canSaveIsFalseInitially() {
            var service = mock(OrderEditorService.class);
            when(service.fetch(any())).thenReturn(new OrderEditorData(Order.empty(), null, Map.of()));
            var vm = new OrderEditorViewModel(
                OrderEditorRequest.forNewOrder(),
                service,
                mock(OrderEditorHost.class),
                req -> {},
                req -> {}
            );

            assertFalse(vm.saveAction.canExecute());
        }
    }

    @Nested
    @DisplayName("when a line item is added")
    class WhenLineItemAdded {

        @Test
        @DisplayName("addLineItemAction invokes the edit host")
        void addLineItemActionInvokesEditHost() {
            var order = MockOrders.orderWithNoLineItems();
            Consumer<LineItemEditorRequest> editHost = mock();
            var vm = new OrderEditorViewModel(
                OrderEditorRequest.of(order.id()),
                serviceFor(order),
                mock(OrderEditorHost.class),
                req -> {},
                editHost
            );

            vm.addLineItemAction.execute();

            verify(editHost).accept(any());
        }

        @Test
        @DisplayName("a confirmed item appears in the line items list")
        void confirmedItemAppearsInList() {
            var order = MockOrders.orderWithNoLineItems();
            var newItem = new LineItem("prod-1", "Widget", 1, BigDecimal.TEN);
            var vm = new OrderEditorViewModel(
                OrderEditorRequest.of(order.id()),
                serviceFor(order),
                mock(OrderEditorHost.class),
                req -> {},
                req -> req.confirmChanges(newItem)
            );

            vm.addLineItemAction.execute();

            assertEquals(1, vm.lineItems().size());
            assertEquals("prod-1", vm.lineItems().getFirst().productId());
        }
    }

    @Nested
    @DisplayName("when the customer selector is triggered")
    class WhenCustomerSelectorTriggered {

        @Test
        @DisplayName("the select customer callback is invoked")
        void selectCustomerCallbackInvoked() {
            var order = MockOrders.validOrderWithLineItems();
            Consumer<CustomerSelectorRequest> selectCustomerHost = mock();
            var vm = new OrderEditorViewModel(
                OrderEditorRequest.of(order.id()),
                serviceFor(order),
                mock(OrderEditorHost.class),
                selectCustomerHost,
                req -> {}
            );

            vm.header().selectCustomerAction.execute();

            verify(selectCustomerHost).accept(any());
        }
    }
}
