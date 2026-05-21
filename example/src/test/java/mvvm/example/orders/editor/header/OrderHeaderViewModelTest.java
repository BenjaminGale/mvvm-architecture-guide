package mvvm.example.orders.editor.header;

import mvvm.example.customers.domain.Customer;
import mvvm.example.orders.MockOrders;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderStatus;
import mvvm.example.orders.domain.queries.OrderHeaderSummary;
import mvvm.example.orders.editor.EditOrderRequest;
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
        var summary = new OrderHeaderSummary(order.createdDate(), order.status(), customer, order.plannedShipDate(), order.reference());
        return new OrderHeaderViewModel(EditOrderRequest.of(order.id()), req -> summary, req -> {});
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
        @DisplayName("the planned ship date property is populated from the order")
        void plannedShipDatePopulated() {
            var vm = validViewModel();

            assertEquals(A_DATE, vm.plannedShipDateProperty().get());
        }

        @Test
        @DisplayName("the reference property is populated from the order")
        void referencePopulated() {
            var vm = validViewModel();

            assertEquals("REF-001", vm.referenceProperty().get());
        }

        @Test
        @DisplayName("the created date is populated from the order")
        void createdDatePopulated() {
            var vm = validViewModel();

            assertEquals(A_DATE, vm.createdDate());
        }

        @Test
        @DisplayName("the status is populated from the order")
        void statusPopulated() {
            var vm = validViewModel();

            assertEquals(OrderStatus.PENDING, vm.status());
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
            var order = new Order("id-1", MockOrders.ACME_CUSTOMER_ID, A_DATE, A_DATE, "", OrderStatus.PENDING, null, List.of());
            var vm = viewModelFor(order, MockOrders.ACME_CUSTOMER);

            assertFalse(vm.validProperty().get());
        }

        @Test
        @DisplayName("the header is invalid when the planned ship date is null")
        void invalidWhenPlannedShipDateNull() {
            var order = new Order("id-1", MockOrders.ACME_CUSTOMER_ID, A_DATE, null, "REF-001", OrderStatus.PENDING, null, List.of());
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
        @DisplayName("the header becomes invalid when the planned ship date is cleared")
        void becomesInvalidWhenPlannedShipDateCleared() {
            var vm = validViewModel();

            vm.plannedShipDateProperty().set(null);

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
            var order = MockOrders.validOrderWithLineItems();
            var summary = new OrderHeaderSummary(order.createdDate(), order.status(), MockOrders.ACME_CUSTOMER, order.plannedShipDate(), order.reference());
            var vm = new OrderHeaderViewModel(EditOrderRequest.of(order.id()), req -> summary, request -> hostCalled[0] = true);

            vm.selectCustomer.execute();

            assertTrue(hostCalled[0]);
        }

        @Test
        @DisplayName("the request carries the currently selected customer")
        void requestCarriesCurrentCustomer() {
            var capturedRequest = new SelectCustomerRequest[]{null};
            var order = MockOrders.validOrderWithLineItems();
            var summary = new OrderHeaderSummary(order.createdDate(), order.status(), MockOrders.ACME_CUSTOMER, order.plannedShipDate(), order.reference());
            var vm = new OrderHeaderViewModel(EditOrderRequest.of(order.id()), req -> summary, request -> capturedRequest[0] = request);

            vm.selectCustomer.execute();

            assertEquals(MockOrders.ACME_CUSTOMER, capturedRequest[0].current());
        }
    }

}
