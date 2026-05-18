package mvvm.example.customers.explorer;

import mvvm.example.core.viewmodel.ExplorerViewModelTest;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;
import mvvm.example.customers.requests.EditCustomerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Customers.CustomersExplorerViewModel")
class CustomersExplorerViewModelTest extends ExplorerViewModelTest<Customer, CustomersExplorerViewModel> {

    private static Customer customer(String id, String name) {
        return new Customer(id, name, name.toLowerCase().replace(" ", ".") + "@example.com", CustomerStatus.ACTIVE);
    }

    private static Customer inactiveCustomer(String id, String name) {
        return new Customer(id, name, name.toLowerCase().replace(" ", ".") + "@example.com", CustomerStatus.INACTIVE);
    }

    @Override
    protected CustomersExplorerViewModel createViewModel() {
        return new CustomersExplorerViewModel(List::of, request -> {});
    }

    @Override
    protected Customer createItem() {
        return customer("1", "Acme Ltd");
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("all customers are loaded from the service")
        void allCustomersLoaded() {
            var vm = new CustomersExplorerViewModel(
                () -> List.of(customer("1", "Acme Ltd"), customer("2", "Beta Corp")),
                request -> {}
            );
            executeFetch(vm);

            assertEquals(2, vm.items().size());
        }

        @Test
        @DisplayName("customers are sorted alphabetically by name")
        void customersSortedAlphabetically() {
            var vm = new CustomersExplorerViewModel(
                () -> List.of(customer("1", "Zebra Inc"), customer("2", "Acme Ltd")),
                request -> {}
            );
            executeFetch(vm);

            assertEquals("Acme Ltd", vm.items().getFirst().name());
            assertEquals("Zebra Inc", vm.items().getLast().name());
        }

        @Test
        @DisplayName("inactive customers are not shown")
        void inactiveCustomersExcluded() {
            var vm = new CustomersExplorerViewModel(
                () -> List.of(customer("1", "Acme Ltd"), inactiveCustomer("2", "Beta Corp")),
                request -> {}
            );
            executeFetch(vm);

            assertEquals(1, vm.items().size());
            assertEquals("Acme Ltd", vm.items().getFirst().name());
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
            executeFetch(vm);

            vm.selectedItemProperty().set(customer);
            vm.editItemAction().execute();

            var captor = ArgumentCaptor.forClass(EditCustomerRequest.class);
            verify(host).editCustomer(captor.capture());
            assertEquals(customer.id(), captor.getValue().customerId());
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

            vm.addItemAction().execute();

            var captor = ArgumentCaptor.forClass(EditCustomerRequest.class);
            verify(host).editCustomer(captor.capture());
            assertTrue(captor.getValue().isNew());
        }
    }
}
