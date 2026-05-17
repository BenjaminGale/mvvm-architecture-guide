package mvvm.example.customers.explorer;

import mvvm.example.customers.domain.Customer;

import java.util.List;
import mvvm.example.customers.domain.CustomerStatus;
import mvvm.example.customers.requests.EditCustomerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Customers.CustomersExplorerViewModel")
class CustomersExplorerViewModelTest {

    private static Customer customer(String id, String name) {
        return new Customer(id, name, name.toLowerCase().replace(" ", ".") + "@example.com", CustomerStatus.ACTIVE);
    }

    private static Customer inactiveCustomer(String id, String name) {
        return new Customer(id, name, name.toLowerCase().replace(" ", ".") + "@example.com", CustomerStatus.INACTIVE);
    }

    private static CustomersExplorerViewModel viewModelWith(Customer... customers) {
        return new CustomersExplorerViewModel(() -> List.of(customers), request -> {});
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("all customers are loaded from the service")
        void allCustomersLoaded() {
            var vm = viewModelWith(customer("1", "Acme Ltd"), customer("2", "Beta Corp"));

            assertEquals(2, vm.getCustomers().size());
        }

        @Test
        @DisplayName("customers are sorted alphabetically by name")
        void customersSortedAlphabetically() {
            var vm = viewModelWith(customer("1", "Zebra Inc"), customer("2", "Acme Ltd"));

            assertEquals("Acme Ltd", vm.getCustomers().getFirst().name());
            assertEquals("Zebra Inc", vm.getCustomers().getLast().name());
        }

        @Test
        @DisplayName("inactive customers are not shown")
        void inactiveCustomersExcluded() {
            var vm = viewModelWith(customer("1", "Acme Ltd"), inactiveCustomer("2", "Beta Corp"));

            assertEquals(1, vm.getCustomers().size());
            assertEquals("Acme Ltd", vm.getCustomers().getFirst().name());
        }
    }

    @Nested
    @DisplayName("when a customer is opened")
    class WhenACustomerIsOpened {

        @Test
        @DisplayName("the navigation callback is invoked with the selected customer id")
        void navigationCallbackInvoked() {
            var host = mock(CustomerExplorerHost.class);
            var customer = customer("1", "Acme Ltd");
            var vm = new CustomersExplorerViewModel(() -> List.of(customer), host);

            vm.openCustomer(customer);

            var captor = ArgumentCaptor.forClass(EditCustomerRequest.class);
            verify(host).editCustomer(captor.capture());
            assertEquals(customer.id(), captor.getValue().customerId());
        }

        @Test
        @DisplayName("the navigation callback is not invoked when called with null")
        void navigationCallbackNotInvokedForNull() {
            var host = mock(CustomerExplorerHost.class);
            var vm = new CustomersExplorerViewModel(List::of, host);

            vm.openCustomer(null);

            verify(host, never()).editCustomer(any());
        }
    }

    @Nested
    @DisplayName("when a new customer is added")
    class WhenANewCustomerIsAdded {

        @Test
        @DisplayName("the navigation callback is invoked with a new customer request")
        void navigationCallbackInvoked() {
            var host = mock(CustomerExplorerHost.class);
            var vm = new CustomersExplorerViewModel(List::of, host);

            vm.addCustomer();

            var captor = ArgumentCaptor.forClass(EditCustomerRequest.class);
            verify(host).editCustomer(captor.capture());
            assertTrue(captor.getValue().isNew());
        }
    }
}
