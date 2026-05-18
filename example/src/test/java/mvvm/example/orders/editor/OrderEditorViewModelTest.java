package mvvm.example.orders.editor;

import mvvm.example.orders.MockOrders;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.requests.EditOrderRequest;
import mvvm.example.orders.requests.SelectCustomerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Orders.OrderEditorViewModel")
class OrderEditorViewModelTest {

    private static OrderEditorService serviceFor(Order order) {
        var service = mock(OrderEditorService.class);
        when(service.fetchOrder(order.id())).thenReturn(order);
        if (order.customerId() != null) {
            when(service.findCustomer(order.customerId())).thenReturn(Optional.of(MockOrders.ACME_CUSTOMER));
        }
        return service;
    }

    private static OrderEditorViewModel vmFor(Order order) {
        return new OrderEditorViewModel(new EditOrderRequest(order.id()), serviceFor(order), mock(OrderEditorHost.class));
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
            vm.getLineItems().selectRow(vm.getLineItems().getRows().getFirst());

            vm.getLineItems().removeSelected();

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
            var vm = new OrderEditorViewModel(new EditOrderRequest(order.id()), service, mock(OrderEditorHost.class));

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
            var vm = new OrderEditorViewModel(new EditOrderRequest(order.id()), service, mock(OrderEditorHost.class));

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
            var vm = new OrderEditorViewModel(new EditOrderRequest(order.id()), service, host);

            vm.copy.execute();

            verify(service).copyOrder(order.id());
            verify(host).openOrder(new EditOrderRequest("copied-" + order.id()));
        }
    }

    @Nested
    @DisplayName("when created")
    class WhenCreatedWithCustomer {

        @Test
        @DisplayName("the customer is fetched from the service")
        void customerFetchedFromService() {
            var order = MockOrders.validOrderWithLineItems();
            var service = serviceFor(order);

            new OrderEditorViewModel(new EditOrderRequest(order.id()), service, mock(OrderEditorHost.class));

            verify(service).findCustomer(MockOrders.ACME_CUSTOMER_ID);
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
            var vm = new OrderEditorViewModel(new EditOrderRequest(order.id()), serviceFor(order), host);

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
            assertEquals("Acme Ltd", updated.customerName());
            assertEquals(1, updated.lineItems().size());
        }
    }
}
