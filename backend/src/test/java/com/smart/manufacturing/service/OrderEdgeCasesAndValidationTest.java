package com.smart.manufacturing.service;

import com.smart.manufacturing.dto.OrderItemDto;
import com.smart.manufacturing.dto.OrderRequestDto;
import com.smart.manufacturing.entity.Customer;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.Product;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.exception.CustomerNotFoundException;
import com.smart.manufacturing.exception.InvalidOrderException;
import com.smart.manufacturing.exception.InvalidOrderStatusException;
import com.smart.manufacturing.exception.OrderNotFoundException;
import com.smart.manufacturing.exception.ProductNotFoundException;
import com.smart.manufacturing.repository.CustomerRepository;
import com.smart.manufacturing.repository.ManufacturingOrderRepository;
import com.smart.manufacturing.repository.OrderStatusHistoryRepository;
import com.smart.manufacturing.repository.ProductRepository;
import com.smart.manufacturing.repository.UserRepository;
import com.smart.manufacturing.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Edge Case & Boundary Condition Unit Tests")
class OrderEdgeCasesAndValidationTest {

    @Mock
    private ManufacturingOrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderStatusHistoryRepository statusHistoryRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private SchedulingService schedulingService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer activeCustomer;
    private Customer inactiveCustomer;
    private Product activeProduct;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        activeCustomer = new Customer("C-100", "Acme Corp", "acme@example.com", "1234567890", "Acme Corp", "Mumbai");
        activeCustomer.setId(1L);
        activeCustomer.setActive(true);

        inactiveCustomer = new Customer("C-101", "Dormant Corp", "dormant@example.com", "0000000000", "Dormant Corp", "Pune");
        inactiveCustomer.setId(2L);
        inactiveCustomer.setActive(false);

        activeProduct = new Product("P-100", "Hydraulic Pump", "Heavy Duty", "Hydraulics", BigDecimal.valueOf(500), 2, 10);
        activeProduct.setId(10L);
        activeProduct.setActive(true);

        inactiveProduct = new Product("P-101", "Legacy Gear", "Obsolete", "Mechanical", BigDecimal.valueOf(150), 1, 5);
        inactiveProduct.setId(11L);
        inactiveProduct.setActive(false);
    }

    @Test
    @DisplayName("Edge Case 1: Non-existent Customer throws CustomerNotFoundException")
    void testNonExistentCustomerThrowsException() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        OrderRequestDto dto = new OrderRequestDto();
        dto.setCustomerId(999L);
        dto.setRequiredDeliveryDate(LocalDate.now().plusDays(5));
        dto.setItems(List.of(new OrderItemDto(10L, 1)));

        assertThrows(CustomerNotFoundException.class, () -> orderService.createOrder(dto, "admin"));
    }

    @Test
    @DisplayName("Edge Case 2: Inactive Customer throws InvalidOrderException")
    void testInactiveCustomerThrowsException() {
        when(customerRepository.findById(2L)).thenReturn(Optional.of(inactiveCustomer));

        OrderRequestDto dto = new OrderRequestDto();
        dto.setCustomerId(2L);
        dto.setRequiredDeliveryDate(LocalDate.now().plusDays(5));
        dto.setItems(List.of(new OrderItemDto(10L, 1)));

        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.createOrder(dto, "admin"));
        assertTrue(ex.getMessage().contains("inactive customer"));
    }

    @Test
    @DisplayName("Edge Case 3: Null item list throws InvalidOrderException")
    void testNullItemsThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));

        OrderRequestDto dto = new OrderRequestDto();
        dto.setCustomerId(1L);
        dto.setRequiredDeliveryDate(LocalDate.now().plusDays(5));
        dto.setItems(null);

        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.createOrder(dto, "admin"));
        assertTrue(ex.getMessage().contains("at least one item"));
    }

    @Test
    @DisplayName("Edge Case 4: Past delivery date throws InvalidOrderException")
    void testPastDeliveryDateThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));

        OrderRequestDto dto = new OrderRequestDto();
        dto.setCustomerId(1L);
        dto.setRequiredDeliveryDate(LocalDate.now().minusDays(1)); // yesterday!
        dto.setItems(List.of(new OrderItemDto(10L, 1)));

        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.createOrder(dto, "admin"));
        assertTrue(ex.getMessage().contains("cannot be before today"));
    }

    @Test
    @DisplayName("Edge Case 5: Zero or negative item quantity throws InvalidOrderException")
    void testZeroQuantityThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));

        OrderRequestDto dto = new OrderRequestDto();
        dto.setCustomerId(1L);
        dto.setRequiredDeliveryDate(LocalDate.now().plusDays(5));
        dto.setItems(List.of(new OrderItemDto(10L, 0))); // zero quantity

        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.createOrder(dto, "admin"));
        assertTrue(ex.getMessage().contains("strictly greater than zero"));
    }

    @Test
    @DisplayName("Edge Case 6: Inactive Product throws InvalidOrderException")
    void testInactiveProductThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));
        when(productRepository.findById(11L)).thenReturn(Optional.of(inactiveProduct));

        OrderRequestDto dto = new OrderRequestDto();
        dto.setCustomerId(1L);
        dto.setRequiredDeliveryDate(LocalDate.now().plusDays(5));
        dto.setItems(List.of(new OrderItemDto(11L, 3))); // ordering inactive product

        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.createOrder(dto, "admin"));
        assertTrue(ex.getMessage().contains("Cannot order inactive product"));
    }

    @Test
    @DisplayName("Edge Case 7: Non-existent Order ID throws OrderNotFoundException")
    void testNonExistentOrderThrowsException() {
        when(orderRepository.findById(888L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(888L));
        assertThrows(OrderNotFoundException.class, () -> orderService.validateOrder(888L, "admin"));
    }

    @Test
    @DisplayName("Edge Case 8: Starting production on an unapproved order throws InvalidOrderStatusException")
    void testStartProductionOnCreatedOrderThrowsException() {
        ManufacturingOrder order = new ManufacturingOrder("ORD-001", activeCustomer, LocalDate.now(), LocalDate.now().plusDays(5), Priority.NORMAL, "Test");
        order.setId(100L);
        order.setStatus(OrderStatus.CREATED); // Not SCHEDULED

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class, () -> orderService.startProduction(100L, "admin"));
    }

    @Test
    @DisplayName("Edge Case 9: Updating an already COMPLETED order throws InvalidOrderException")
    void testUpdateCompletedOrderThrowsException() {
        ManufacturingOrder order = new ManufacturingOrder("ORD-002", activeCustomer, LocalDate.now(), LocalDate.now().plusDays(5), Priority.NORMAL, "Test");
        order.setId(102L);
        order.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(102L)).thenReturn(Optional.of(order));

        OrderRequestDto dto = new OrderRequestDto();
        dto.setNotes("Try editing completed order");

        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.updateOrder(102L, dto, "admin"));
        assertTrue(ex.getMessage().contains("Cannot update an order in status: COMPLETED"));
    }

    @Test
    @DisplayName("Edge Case 10: Cancelling an already COMPLETED order throws InvalidOrderException")
    void testCancelCompletedOrderThrowsException() {
        ManufacturingOrder order = new ManufacturingOrder("ORD-003", activeCustomer, LocalDate.now(), LocalDate.now().plusDays(5), Priority.NORMAL, "Test");
        order.setId(103L);
        order.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(103L)).thenReturn(Optional.of(order));

        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.cancelOrder(103L, "Wrong cancellation", "admin"));
        assertTrue(ex.getMessage().contains("Completed orders cannot be cancelled"));
    }
}
