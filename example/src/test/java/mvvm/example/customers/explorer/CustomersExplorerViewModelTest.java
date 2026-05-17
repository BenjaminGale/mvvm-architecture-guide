package mvvm.example.customers.explorer;

import mvvm.example.customers.StubCustomerRepository;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerService;
import mvvm.example.customers.domain.CustomerStatus;
import mvvm.example.customers.editor.EditCustomerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomersExplorerViewModel")
class CustomersExplorerViewModelTest {

    private static Customer customer(String id, String name) {
        return new Customer(id, name, name.toLowerCase().replace(" ", ".") + "@example.com", CustomerStatus.ACTIVE);
    }

    private static Customer inactiveCustomer(String id, String name) {
        return new Customer(id, name, name.toLowerCase().replace(" ", ".") + "@example.com", CustomerStatus.INACTIVE);
    }

    private static CustomersExplorerViewModel viewModelWith(Customer... customers) {
        var service = new CustomerService(new StubCustomerRepository(customers));
        return new CustomersExplorerViewModel(service, request -> {});
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
            var selected = new AtomicReference<String>();
            var customer = customer("1", "Acme Ltd");
            var service = new CustomerService(new StubCustomerRepository(customer));
            var vm = new CustomersExplorerViewModel(service, request -> selected.set(request.customerId()));

            vm.openCustomer(customer);

            assertEquals(customer.id(), selected.get());
        }

        @Test
        @DisplayName("the navigation callback is not invoked when called with null")
        void navigationCallbackNotInvokedForNull() {
            var selected = new AtomicReference<String>();
            var service = new CustomerService(new StubCustomerRepository());
            var vm = new CustomersExplorerViewModel(service, request -> selected.set(request.customerId()));

            vm.openCustomer(null);

            assertNull(selected.get());
        }
    }
}
