package mvvm.example.orders.editor.header;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;
import mvvm.example.orders.requests.SelectCustomerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Orders.CustomerSelectorViewModel")
class CustomerSelectorViewModelTest {

    private static final Customer ACME = new Customer("c-1", "Acme Corp", "acme@example.com", CustomerStatus.ACTIVE);
    private static final Customer GLOBEX = new Customer("c-2", "Globex Inc", "globex@example.com", CustomerStatus.ACTIVE);
    private static final Customer INACTIVE = new Customer("c-3", "Defunct Ltd", "defunct@example.com", CustomerStatus.INACTIVE);

    private static final List<Customer> ALL_CUSTOMERS = List.of(ACME, GLOBEX, INACTIVE);

    private static CustomerSelectorViewModel viewModelFor(Customer current) {
        return new CustomerSelectorViewModel(new SelectCustomerRequest(current, confirmed -> {}), ALL_CUSTOMERS);
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("the selected customer is pre-populated from the request")
        void preSelectsCurrentCustomer() {
            var vm = viewModelFor(ACME);

            assertEquals(ACME, vm.selectedCustomerProperty().get());
        }

        @Test
        @DisplayName("only active customers are shown")
        void onlyActiveCustomersShown() {
            var vm = viewModelFor(null);

            assertFalse(vm.getCustomers().contains(INACTIVE));
            assertEquals(2, vm.getCustomers().size());
        }
    }

    @Nested
    @DisplayName("when searching")
    class WhenSearching {

        @Test
        @DisplayName("the list is filtered to customers whose name contains the search text")
        void filtersOnName() {
            var vm = viewModelFor(null);

            vm.searchTextProperty().set("acme");

            assertEquals(List.of(ACME), vm.getCustomers());
        }

        @Test
        @DisplayName("matching is case-insensitive")
        void caseInsensitiveMatch() {
            var vm = viewModelFor(null);

            vm.searchTextProperty().set("GLOBEX");

            assertEquals(List.of(GLOBEX), vm.getCustomers());
        }

        @Test
        @DisplayName("clearing the search restores all active customers")
        void clearingSearchRestoresAll() {
            var vm = viewModelFor(null);
            vm.searchTextProperty().set("acme");

            vm.searchTextProperty().set("");

            assertEquals(2, vm.getCustomers().size());
        }
    }

    @Nested
    @DisplayName("when confirmed")
    class WhenConfirmed {

        @Test
        @DisplayName("the request callback is invoked with the selected customer")
        void callbackInvokedWithSelection() {
            Consumer<Customer> listener = mock();
            var vm = new CustomerSelectorViewModel(new SelectCustomerRequest(null, listener), ALL_CUSTOMERS);
            vm.selectedCustomerProperty().set(ACME);

            vm.confirm();

            verify(listener).accept(ACME);
        }

        @Test
        @DisplayName("the callback is not invoked when no customer is selected")
        void callbackNotInvokedWithoutSelection() {
            Consumer<Customer> listener = mock();
            var vm = new CustomerSelectorViewModel(new SelectCustomerRequest(null, listener), ALL_CUSTOMERS);

            vm.confirm();

            verifyNoInteractions(listener);
        }
    }
}
