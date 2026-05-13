package mvvm.example.customers.explorer;

import mvvm.example.customers.StubCustomerRepository;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomersExplorerViewModel")
class CustomersExplorerViewModelTest {

    private static Customer customer(String id, String name) {
        return new Customer(id, name, name.toLowerCase().replace(" ", ".") + "@example.com");
    }

    private static CustomersExplorerViewModel viewModelWith(Customer... customers) {
        var service = new CustomerService(new StubCustomerRepository(customers));
        return new CustomersExplorerViewModel(service, c -> {});
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
    }

    @Nested
    @DisplayName("when a customer is opened")
    class WhenACustomerIsOpened {

        @Test
        @DisplayName("the navigation callback is invoked with the selected customer")
        void navigationCallbackInvoked() {
            var selected = new AtomicReference<Customer>();
            var customer = customer("1", "Acme Ltd");
            var service = new CustomerService(new StubCustomerRepository(customer));
            var vm = new CustomersExplorerViewModel(service, selected::set);

            vm.openCustomer(customer);

            assertEquals(customer, selected.get());
        }

        @Test
        @DisplayName("the navigation callback is not invoked when called with null")
        void navigationCallbackNotInvokedForNull() {
            var selected = new AtomicReference<Customer>();
            var service = new CustomerService(new StubCustomerRepository());
            var vm = new CustomersExplorerViewModel(service, selected::set);

            vm.openCustomer(null);

            assertNull(selected.get());
        }
    }
}
