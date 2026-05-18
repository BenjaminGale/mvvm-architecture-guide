package mvvm.example.orders.editor.header;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;
import mvvm.example.orders.MockOrders;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.OrderHeaderViewModel")
class OrderHeaderViewModelTest {

    private static final LocalDate A_DATE = LocalDate.of(2025, 1, 15);

    private static OrderHeaderViewModel viewModelFor(Order order, Customer customer) {
        return new OrderHeaderViewModel(order, customer, request -> {});
    }

    private static OrderHeaderViewModel validViewModel() {
        return viewModelFor(MockOrders.validOrderWithLineItems(), MockOrders.ACME_CUSTOMER);
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("the selected customer property is populated from the given customer")
        void customerPopulated() {
            var vm = validViewModel();

            assertEquals(MockOrders.ACME_CUSTOMER, vm.selectedCustomerProperty().get());
        }

        @Test
        @DisplayName("the order date property is populated from the order")
        void orderDatePopulated() {
            var vm = validViewModel();

            assertEquals(A_DATE, vm.orderDateProperty().get());
        }

        @Test
        @DisplayName("the reference property is populated from the order")
        void referencePopulated() {
            var vm = validViewModel();

            assertEquals("REF-001", vm.referenceProperty().get());
        }
    }

    @Nested
    @DisplayName("when all required fields are populated")
    class WhenAllFieldsPopulated {

        @Test
        @DisplayName("the header is valid")
        void headerIsValid() {
            assertTrue(validViewModel().validProperty().get());
        }
    }

    @Nested
    @DisplayName("when a required field is missing")
    class WhenAFieldIsMissing {

        @Test
        @DisplayName("the header is invalid when no customer is selected")
        void invalidWhenNoCustomer() {
            var vm = viewModelFor(MockOrders.validOrderWithLineItems(), null);

            assertFalse(vm.validProperty().get());
        }

        @Test
        @DisplayName("the header is invalid when the reference is blank")
        void invalidWhenReferenceBlank() {
            var order = new Order("id-1", MockOrders.ACME_CUSTOMER_ID, "Acme Ltd", A_DATE, "", OrderStatus.WIP, null, List.of());
            var vm = viewModelFor(order, MockOrders.ACME_CUSTOMER);

            assertFalse(vm.validProperty().get());
        }

        @Test
        @DisplayName("the header is invalid when the order date is null")
        void invalidWhenOrderDateNull() {
            var order = new Order("id-1", MockOrders.ACME_CUSTOMER_ID, "Acme Ltd", null, "REF-001", OrderStatus.WIP, null, List.of());
            var vm = viewModelFor(order, MockOrders.ACME_CUSTOMER);

            assertFalse(vm.validProperty().get());
        }
    }

    @Nested
    @DisplayName("when fields are edited")
    class WhenFieldsAreEdited {

        @Test
        @DisplayName("the header becomes invalid when the customer is cleared")
        void becomesInvalidWhenCustomerCleared() {
            var vm = validViewModel();

            vm.selectedCustomerProperty().set(null);

            assertFalse(vm.validProperty().get());
        }

        @Test
        @DisplayName("the header becomes valid when a customer is selected")
        void becomesValidWhenCustomerSelected() {
            var vm = viewModelFor(MockOrders.validOrderWithLineItems(), null);

            vm.selectedCustomerProperty().set(MockOrders.ACME_CUSTOMER);

            assertTrue(vm.validProperty().get());
        }

        @Test
        @DisplayName("the header becomes invalid when the reference is cleared")
        void becomesInvalidWhenReferenceCleared() {
            var vm = validViewModel();

            vm.referenceProperty().set("");

            assertFalse(vm.validProperty().get());
        }

        @Test
        @DisplayName("the header becomes invalid when the order date is cleared")
        void becomesInvalidWhenOrderDateCleared() {
            var vm = validViewModel();

            vm.orderDateProperty().set(null);

            assertFalse(vm.validProperty().get());
        }
    }

    @Nested
    @DisplayName("when select customer is triggered")
    class WhenSelectCustomerTriggered {

        @Test
        @DisplayName("the host callback is invoked")
        void hostCallbackInvoked() {
            var hostCalled = new boolean[]{false};
            var vm = new OrderHeaderViewModel(MockOrders.validOrderWithLineItems(), MockOrders.ACME_CUSTOMER, request -> hostCalled[0] = true);

            vm.selectCustomer.execute();

            assertTrue(hostCalled[0]);
        }

        @Test
        @DisplayName("the request carries the currently selected customer")
        void requestCarriesCurrentCustomer() {
            var capturedRequest = new mvvm.example.orders.requests.SelectCustomerRequest[]{null};
            var vm = new OrderHeaderViewModel(MockOrders.validOrderWithLineItems(), MockOrders.ACME_CUSTOMER, request -> capturedRequest[0] = request);

            vm.selectCustomer.execute();

            assertEquals(MockOrders.ACME_CUSTOMER, capturedRequest[0].getCurrent());
        }
    }

    @Nested
    @DisplayName("when the header record is built")
    class WhenHeaderRecordIsBuilt {

        @Test
        @DisplayName("the header record reflects the current property values")
        void reflectsCurrentPropertyValues() {
            var vm = validViewModel();
            var newCustomer = new Customer("cust-2", "New Customer", "new@example.com", CustomerStatus.ACTIVE);
            vm.selectedCustomerProperty().set(newCustomer);
            vm.referenceProperty().set("REF-999");
            var newDate = LocalDate.of(2025, 6, 1);
            vm.orderDateProperty().set(newDate);

            var header = vm.buildHeader();

            assertEquals("cust-2", header.customerId());
            assertEquals("New Customer", header.customerName());
            assertEquals("REF-999", header.reference());
            assertEquals(newDate, header.date());
        }
    }
}
