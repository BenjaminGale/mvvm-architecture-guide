package mvvm.example.orders.editor.header;

import mvvm.example.orders.domain.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderHeaderViewModel")
class OrderHeaderViewModelTest {

    private static final LocalDate A_DATE = LocalDate.of(2025, 1, 15);

    private static Order validOrder() {
        return new Order("id-1", "Acme Ltd", A_DATE, "REF-001", List.of());
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("the customer name property is populated from the order")
        void customerNamePopulated() {
            var vm = new OrderHeaderViewModel(validOrder());

            assertEquals("Acme Ltd", vm.customerNameProperty().get());
        }

        @Test
        @DisplayName("the order date property is populated from the order")
        void orderDatePopulated() {
            var vm = new OrderHeaderViewModel(validOrder());

            assertEquals(A_DATE, vm.orderDateProperty().get());
        }

        @Test
        @DisplayName("the reference property is populated from the order")
        void referencePopulated() {
            var vm = new OrderHeaderViewModel(validOrder());

            assertEquals("REF-001", vm.referenceProperty().get());
        }
    }

    @Nested
    @DisplayName("when all required fields are populated")
    class WhenAllFieldsPopulated {

        @Test
        @DisplayName("the header is valid")
        void headerIsValid() {
            var vm = new OrderHeaderViewModel(validOrder());

            assertTrue(vm.validProperty().get());
        }
    }

    @Nested
    @DisplayName("when a required field is missing")
    class WhenAFieldIsMissing {

        @Test
        @DisplayName("the header is invalid when the customer name is blank")
        void invalidWhenCustomerNameBlank() {
            var vm = new OrderHeaderViewModel(new Order("id-1", "", A_DATE, "REF-001", List.of()));

            assertFalse(vm.validProperty().get());
        }

        @Test
        @DisplayName("the header is invalid when the reference is blank")
        void invalidWhenReferenceBlank() {
            var vm = new OrderHeaderViewModel(new Order("id-1", "Acme Ltd", A_DATE, "", List.of()));

            assertFalse(vm.validProperty().get());
        }

        @Test
        @DisplayName("the header is invalid when the order date is null")
        void invalidWhenOrderDateNull() {
            var vm = new OrderHeaderViewModel(new Order("id-1", "Acme Ltd", null, "REF-001", List.of()));

            assertFalse(vm.validProperty().get());
        }
    }

    @Nested
    @DisplayName("when fields are edited")
    class WhenFieldsAreEdited {

        @Test
        @DisplayName("the header becomes invalid when the customer name is cleared")
        void becomesInvalidWhenCustomerNameCleared() {
            var vm = new OrderHeaderViewModel(validOrder());

            vm.customerNameProperty().set("");

            assertFalse(vm.validProperty().get());
        }

        @Test
        @DisplayName("the header becomes valid when a blank customer name is populated")
        void becomesValidWhenCustomerNamePopulated() {
            var vm = new OrderHeaderViewModel(new Order("id-1", "", A_DATE, "REF-001", List.of()));

            vm.customerNameProperty().set("Acme Ltd");

            assertTrue(vm.validProperty().get());
        }

        @Test
        @DisplayName("the header becomes invalid when the reference is cleared")
        void becomesInvalidWhenReferenceCleared() {
            var vm = new OrderHeaderViewModel(validOrder());

            vm.referenceProperty().set("");

            assertFalse(vm.validProperty().get());
        }

        @Test
        @DisplayName("the header becomes invalid when the order date is cleared")
        void becomesInvalidWhenOrderDateCleared() {
            var vm = new OrderHeaderViewModel(validOrder());

            vm.orderDateProperty().set(null);

            assertFalse(vm.validProperty().get());
        }
    }

    @Nested
    @DisplayName("when the header record is built")
    class WhenHeaderRecordIsBuilt {

        @Test
        @DisplayName("the header record reflects the current property values")
        void reflectsCurrentPropertyValues() {
            var vm = new OrderHeaderViewModel(validOrder());
            vm.customerNameProperty().set("New Customer");
            vm.referenceProperty().set("REF-999");
            var newDate = LocalDate.of(2025, 6, 1);
            vm.orderDateProperty().set(newDate);

            var header = vm.buildHeader();

            assertEquals("New Customer", header.customerName());
            assertEquals("REF-999", header.reference());
            assertEquals(newDate, header.date());
        }
    }
}
