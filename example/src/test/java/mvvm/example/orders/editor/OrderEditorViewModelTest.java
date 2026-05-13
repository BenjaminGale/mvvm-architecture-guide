package mvvm.example.orders.editor;

import mvvm.example.orders.StubOrderRepository;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderEditorViewModel")
class OrderEditorViewModelTest {

    private static final LocalDate A_DATE = LocalDate.of(2025, 1, 15);
    private static final LineItem A_LINE_ITEM = new LineItem("Widget", 1, BigDecimal.TEN);

    private static Order validOrderWithLineItems() {
        return new Order("id-1", "Acme Ltd", A_DATE, "REF-001", List.of(A_LINE_ITEM));
    }

    private static Order orderWithBlankCustomerName() {
        return new Order("id-1", "", A_DATE, "REF-001", List.of(A_LINE_ITEM));
    }

    private static Order orderWithNoLineItems() {
        return new Order("id-1", "Acme Ltd", A_DATE, "REF-001", List.of());
    }

    private static OrderService serviceWith(Order... orders) {
        return new OrderService(new StubOrderRepository(orders));
    }

    private static OrderEditorRequests noOpRequests() {
        return new OrderEditorRequests(() -> {}, order -> {}, () -> {});
    }

    private static OrderEditorViewModel vmFor(Order order) {
        return new OrderEditorViewModel(order, serviceWith(order), noOpRequests(), session -> {});
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("canSave is false when the customer name is blank")
        void canSaveIsFalseWhenCustomerNameBlank() {
            var vm = vmFor(orderWithBlankCustomerName());

            assertFalse(vm.save.canExecute());
        }

        @Test
        @DisplayName("canSave is false when there are no line items")
        void canSaveIsFalseWhenNoLineItems() {
            var vm = vmFor(orderWithNoLineItems());

            assertFalse(vm.save.canExecute());
        }

        @Test
        @DisplayName("canSave is true when the header is valid and line items are present")
        void canSaveIsTrueWhenValid() {
            var vm = vmFor(validOrderWithLineItems());

            assertTrue(vm.save.canExecute());
        }
    }

    @Nested
    @DisplayName("when fields are edited")
    class WhenFieldsAreEdited {

        @Test
        @DisplayName("canSave becomes true when a blank customer name is populated")
        void canSaveBecomesTrue() {
            var vm = vmFor(orderWithBlankCustomerName());

            vm.getHeader().customerNameProperty().set("Acme Ltd");

            assertTrue(vm.save.canExecute());
        }

        @Test
        @DisplayName("canSave becomes false when the last line item is removed")
        void canSaveBecomesFalseWhenLastItemRemoved() {
            var vm = vmFor(validOrderWithLineItems());
            vm.getLineItems().selectRow(vm.getLineItems().getRows().getFirst());

            vm.getLineItems().removeSelected();

            assertFalse(vm.save.canExecute());
        }
    }

    @Nested
    @DisplayName("when the order is saved")
    class WhenSaved {

        @Test
        @DisplayName("the order is persisted via the service")
        void orderIsPersisted() {
            var repo = new StubOrderRepository();
            var vm = new OrderEditorViewModel(
                validOrderWithLineItems(),
                new OrderService(repo),
                noOpRequests(),
                session -> {}
            );

            vm.save.executeAsync(Runnable::run).join();

            assertFalse(repo.findAll().isEmpty());
        }
    }

    @Nested
    @DisplayName("when the order is deleted")
    class WhenDeleted {

        @Test
        @DisplayName("the order is removed via the service")
        void orderIsRemoved() {
            var order = validOrderWithLineItems();
            var repo = new StubOrderRepository(order);
            var vm = new OrderEditorViewModel(order, new OrderService(repo), noOpRequests(), session -> {});

            vm.delete.execute();

            assertTrue(repo.findAll().isEmpty());
        }
    }

    @Nested
    @DisplayName("when the order is copied")
    class WhenCopied {

        @Test
        @DisplayName("a copy of the order is passed to the onCopied callback")
        void copyIsPassedToCallback() {
            var order = validOrderWithLineItems();
            var copied = new AtomicReference<Order>();
            var repo = new StubOrderRepository(order);
            var requests = new OrderEditorRequests(() -> {}, copied::set, () -> {});
            var vm = new OrderEditorViewModel(order, new OrderService(repo), requests, session -> {});

            vm.copy.execute();

            assertNotNull(copied.get());
            assertNotEquals(order.id(), copied.get().id());
        }
    }

    @Nested
    @DisplayName("when the updated order is built")
    class WhenUpdatedOrderIsBuilt {

        @Test
        @DisplayName("the order reflects the current header and line item values")
        void orderReflectsCurrentValues() {
            var vm = vmFor(validOrderWithLineItems());
            vm.getHeader().customerNameProperty().set("New Customer");

            var updated = vm.buildUpdatedOrder();

            assertEquals("New Customer", updated.customerName());
            assertEquals(1, updated.lineItems().size());
        }
    }
}
