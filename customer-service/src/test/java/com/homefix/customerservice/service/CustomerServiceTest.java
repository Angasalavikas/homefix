package com.homefix.customerservice.service;

import com.homefix.customerservice.dto.AddressRequest;
import com.homefix.customerservice.dto.AddressResponse;
import com.homefix.customerservice.dto.CustomerRequest;
import com.homefix.customerservice.dto.CustomerResponse;
import com.homefix.customerservice.entity.Address;
import com.homefix.customerservice.entity.Customer;
import com.homefix.customerservice.repository.AddressRepository;
import com.homefix.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressRepository addressRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, addressRepository);
    }

    @Test
    void createOrUpdateProfile_ShouldCreateNewCustomer_WhenNotFound() {
        Long userId = 1L;
        CustomerRequest request = CustomerRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+12025551234")
                .build();

        when(customerRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(100L);
            saved.setCreatedAt(java.time.LocalDateTime.now());
            saved.setUpdatedAt(java.time.LocalDateTime.now());
            return saved;
        });

        CustomerResponse response = customerService.createOrUpdateProfile(userId, request);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    }

    @Test
    void createOrUpdateProfile_ShouldUpdateExistingCustomer_WhenFound() {
        Long userId = 1L;
        Customer existing = Customer.builder()
                .id(100L)
                .userId(userId)
                .fullName("Old Name")
                .email("old@example.com")
                .phone("+12025551234")
                .build();
        existing.setCreatedAt(java.time.LocalDateTime.now());
        existing.setUpdatedAt(java.time.LocalDateTime.now());

        CustomerRequest request = CustomerRequest.builder()
                .fullName("New Name")
                .email("new@example.com")
                .build();

        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.createOrUpdateProfile(userId, request);

        assertThat(response.getFullName()).isEqualTo("New Name");
        assertThat(response.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void getProfile_ShouldReturnCustomer_WhenFound() {
        Long userId = 1L;
        Customer customer = Customer.builder()
                .id(100L)
                .userId(userId)
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+12025551234")
                .build();
        customer.setCreatedAt(java.time.LocalDateTime.now());
        customer.setUpdatedAt(java.time.LocalDateTime.now());

        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getProfile(userId);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getFullName()).isEqualTo("John Doe");
    }

    @Test
    void getProfile_ShouldThrowException_WhenNotFound() {
        when(customerRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getProfile(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void addAddress_ShouldAddAddressToCustomer() {
        Long userId = 1L;
        Customer customer = Customer.builder()
                .id(100L)
                .userId(userId)
                .fullName("John Doe")
                .addresses(new ArrayList<>())
                .build();

        AddressRequest request = AddressRequest.builder()
                .label("Home")
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .zip("10001")
                .isDefault(true)
                .build();

        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponse response = customerService.addAddress(userId, request);

        assertThat(response.getLabel()).isEqualTo("Home");
        assertThat(response.getStreet()).isEqualTo("123 Main St");
        assertThat(response.getIsDefault()).isTrue();
    }

    @Test
    void addAddress_ShouldThrowException_WhenCustomerNotFound() {
        when(customerRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.addAddress(99L, new AddressRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateAddress_ShouldUpdateExistingAddress() {
        Long userId = 1L;
        Address address = Address.builder()
                .id(10L)
                .label("Home")
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .zip("10001")
                .isDefault(false)
                .build();

        Customer customer = Customer.builder()
                .id(100L)
                .userId(userId)
                .fullName("John Doe")
                .addresses(new ArrayList<>(List.of(address)))
                .build();
        address.setCustomer(customer);

        AddressRequest request = AddressRequest.builder()
                .label("Work")
                .city("Brooklyn")
                .isDefault(true)
                .build();

        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponse response = customerService.updateAddress(userId, 10L, request);

        assertThat(response.getLabel()).isEqualTo("Work");
        assertThat(response.getCity()).isEqualTo("Brooklyn");
        assertThat(response.getIsDefault()).isTrue();
    }

    @Test
    void updateAddress_ShouldThrowException_WhenAddressNotFound() {
        Customer customer = Customer.builder()
                .id(100L)
                .userId(1L)
                .fullName("John Doe")
                .addresses(new ArrayList<>())
                .build();

        when(customerRepository.findByUserId(1L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> customerService.updateAddress(1L, 999L, new AddressRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteAddress_ShouldRemoveAddress() {
        Long userId = 1L;
        Address address = Address.builder()
                .id(10L)
                .label("Home")
                .street("123 Main St")
                .city("NYC")
                .state("NY")
                .zip("10001")
                .isDefault(false)
                .build();

        Customer customer = Customer.builder()
                .id(100L)
                .userId(userId)
                .fullName("John Doe")
                .addresses(new ArrayList<>(List.of(address)))
                .build();
        address.setCustomer(customer);

        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        customerService.deleteAddress(userId, 10L);

        assertThat(customer.getAddresses()).isEmpty();
        verify(customerRepository).save(customer);
    }

    @Test
    void getAddresses_ShouldReturnAllAddresses() {
        Long userId = 1L;
        Address address1 = Address.builder().id(10L).label("Home").street("123 Main").city("NYC").state("NY").zip("10001").isDefault(true).build();
        Address address2 = Address.builder().id(11L).label("Work").street("456 Office").city("NYC").state("NY").zip("10002").isDefault(false).build();

        Customer customer = Customer.builder()
                .id(100L)
                .userId(userId)
                .fullName("John Doe")
                .addresses(new ArrayList<>(List.of(address1, address2)))
                .build();
        address1.setCustomer(customer);
        address2.setCustomer(customer);

        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));

        List<AddressResponse> addresses = customerService.getAddresses(userId);

        assertThat(addresses).hasSize(2);
        assertThat(addresses.get(0).getLabel()).isEqualTo("Home");
        assertThat(addresses.get(1).getLabel()).isEqualTo("Work");
    }
}
