package mvvm.example.orders.editor;

import mvvm.example.orders.MockOrders;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.editor.edititem.EditItemRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.OrderEditorViewModel")
class OrderEditorViewModelTest {

    static class MockService implements OrderEditorService {

        private final List<String> copiedOrderIds = new ArrayList<>();
        private final Map<String, Order> ordersMap = new HashMap<>();

        public MockService(Order... orders) {
            Arrays
                .stream(orders)
                .forEach(order -> ordersMap.put(order.id(), order));
        }

        @Override public Order fetchOrder(String orderId) { return ordersMap.get(orderId); }

        @Override public void saveOrder(Order order) {
            ordersMap.put(order.id(), order);
        }

        @Override public String copyOrder(String orderId) {
            var copiedId = "copied-" + orderId;
            copiedOrderIds.add(orderId);
            return copiedId;
        }

        @Override public void deleteOrder(String orderId) {
            ordersMap.remove(orderId);
        }

        // TODO: Need to check more than the id here...
        public void assertOrderWasAdded(Order order) { assertEquals(order.id(), ordersMap.get(order.id()).id()); }
        public void assertOrderWasCopied(Order order) { assertTrue(copiedOrderIds.contains(order.id())); }
        public void assertOrderWasDeleted(Order order) { assertFalse(ordersMap.containsKey(order.id())); }
    }

    static class MockHost implements OrderEditorHost {

        private EditOrderRequest request;

        @Override public void returnToList() {}
        @Override public void openOrder(EditOrderRequest request) { this.request = request; }
        @Override public void showItemEditor(EditItemRequest request) { }

        void assertOrderWasShown(String orderId) {
            assertEquals(orderId, request.orderId());
        }
    }

    private static OrderEditorHost noOpHost() {
        return new OrderEditorHost() {
            @Override public void returnToList() {}
            @Override public void openOrder(EditOrderRequest request) {}
            @Override public void showItemEditor(EditItemRequest request) {}
        };
    }

    private static OrderEditorViewModel vmFor(Order order) {
        return new OrderEditorViewModel(new EditOrderRequest(order.id()), new MockService(order), noOpHost());
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("canSave is false when the customer name is blank")
        void canSaveIsFalseWhenCustomerNameBlank() {
            var vm = vmFor(MockOrders.orderWithBlankCustomerName());

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
        @DisplayName("canSave becomes true when a blank customer name is populated")
        void canSaveBecomesTrue() {
            var vm = vmFor(MockOrders.orderWithBlankCustomerName());

            vm.getHeader().customerNameProperty().set("Acme Ltd");

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
            var service = new MockService(order);
            var vm = new OrderEditorViewModel(new EditOrderRequest(order.id()), service, noOpHost());

            vm.save.executeAsync(Runnable::run).join();

            service.assertOrderWasAdded(order);
        }
    }

    @Nested
    @DisplayName("when the order is deleted")
    class WhenDeleted {

        @Test
        @DisplayName("the order is removed from storage")
        void orderIsRemoved() {
            var order = MockOrders.validOrderWithLineItems();
            var service = new MockService(order);
            var vm = new OrderEditorViewModel(new EditOrderRequest(order.id()), service, noOpHost());

            vm.delete.execute();

            service.assertOrderWasDeleted(order);
        }
    }

    @Nested
    @DisplayName("when the order is copied")
    class WhenCopied {

        @Test
        @DisplayName("the copied order is shown")
        void copiedOrderIsShown() {
            var order = MockOrders.validOrderWithLineItems();
            var service = new MockService(order);
            var host = new MockHost();
            var vm = new OrderEditorViewModel(new EditOrderRequest(order.id()), service, host);

            vm.copy.execute();

            service.assertOrderWasCopied(order);
            host.assertOrderWasShown("copied-" + order.id());
        }
    }

    @Nested
    @DisplayName("when the updated order is built")
    class WhenUpdatedOrderIsBuilt {

        @Test
        @DisplayName("the order reflects the current header and line item values")
        void orderReflectsCurrentValues() {
            var vm = vmFor(MockOrders.validOrderWithLineItems());
            vm.getHeader().customerNameProperty().set("New Customer");

            var updated = vm.buildUpdatedOrder();

            assertEquals("New Customer", updated.customerName());
            assertEquals(1, updated.lineItems().size());
        }
    }
}
