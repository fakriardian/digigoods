package com.example.digigoods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.digigoods.dto.CheckoutRequest;
import com.example.digigoods.dto.OrderResponse;
import com.example.digigoods.exception.ExcessiveDiscountException;
import com.example.digigoods.exception.UnauthorizedAccessException;
import com.example.digigoods.model.Discount;
import com.example.digigoods.model.DiscountType;
import com.example.digigoods.model.Product;
import com.example.digigoods.model.User;
import com.example.digigoods.repository.OrderRepository;
import com.example.digigoods.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

  @Mock
  private ProductService productService;

  @Mock
  private DiscountService discountService;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CheckoutService checkoutService;

  private User testUser;
  private Product product1;
  private Product product2;
  private Discount generalDiscount;
  private Discount productSpecificDiscount;
  private CheckoutRequest checkoutRequest;

  @BeforeEach
  void setUp() {
    testUser = new User(1L, "testuser", "password");
    
    product1 = new Product();
    product1.setId(1L);
    product1.setName("Product 1");
    product1.setPrice(new BigDecimal("100.00"));
    product1.setStock(10);
    
    product2 = new Product();
    product2.setId(2L);
    product2.setName("Product 2");
    product2.setPrice(new BigDecimal("50.00"));
    product2.setStock(5);
    
    generalDiscount = new Discount();
    generalDiscount.setId(1L);
    generalDiscount.setCode("GENERAL10");
    generalDiscount.setPercentage(new BigDecimal("10.00"));
    generalDiscount.setType(DiscountType.GENERAL);
    generalDiscount.setValidFrom(LocalDate.now().minusDays(1));
    generalDiscount.setValidUntil(LocalDate.now().plusDays(30));
    generalDiscount.setRemainingUses(10);
    
    productSpecificDiscount = new Discount();
    productSpecificDiscount.setId(2L);
    productSpecificDiscount.setCode("PRODUCT20");
    productSpecificDiscount.setPercentage(new BigDecimal("20.00"));
    productSpecificDiscount.setType(DiscountType.PRODUCT_SPECIFIC);
    productSpecificDiscount.setValidFrom(LocalDate.now().minusDays(1));
    productSpecificDiscount.setValidUntil(LocalDate.now().plusDays(30));
    productSpecificDiscount.setRemainingUses(5);
    
    Set<Product> applicableProducts = new HashSet<>();
    applicableProducts.add(product1);
    productSpecificDiscount.setApplicableProducts(applicableProducts);
    
    checkoutRequest = new CheckoutRequest();
    checkoutRequest.setUserId(1L);
    checkoutRequest.setProductIds(List.of(1L, 2L));
    checkoutRequest.setDiscountCodes(List.of("GENERAL10"));
  }

  @Nested
  @DisplayName("Process Checkout Tests")
  class ProcessCheckoutTests {

    @Test
    @DisplayName("Given valid checkout request, when processing checkout, then return success response")
    void givenValidCheckoutRequest_whenProcessingCheckout_thenReturnSuccessResponse() {
      // Arrange
      List<Product> products = List.of(product1, product2);
      List<Discount> discounts = List.of(generalDiscount);
      
      when(productService.getProductsByIds(anyList())).thenReturn(products);
      when(discountService.validateAndGetDiscounts(anyList())).thenReturn(discounts);
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(orderRepository.save(any())).thenReturn(null);

      // Act
      OrderResponse response = checkoutService.processCheckout(checkoutRequest, 1L);

      // Assert
      assertNotNull(response);
      assertEquals("Order created successfully!", response.getMessage());
      assertEquals(new BigDecimal("135.00"), response.getFinalPrice()); // 150 - 10% = 135
      
      verify(productService).validateAndUpdateStock(checkoutRequest.getProductIds());
      verify(discountService).updateDiscountUsage(discounts);
    }

    @Test
    @DisplayName("Given unauthorized user, when processing checkout, then throw UnauthorizedAccessException")
    void givenUnauthorizedUser_whenProcessingCheckout_thenThrowUnauthorizedAccessException() {
      // Arrange
      Long authenticatedUserId = 2L; // Different from request user ID

      // Act & Assert
      UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
          () -> checkoutService.processCheckout(checkoutRequest, authenticatedUserId));
      assertEquals("User cannot place order for another user", exception.getMessage());
    }

    @Test
    @DisplayName("Given excessive discount, when processing checkout, then throw ExcessiveDiscountException")
    void givenExcessiveDiscount_whenProcessingCheckout_thenThrowExcessiveDiscountException() {
      // Arrange
      Discount excessiveDiscount = new Discount();
      excessiveDiscount.setCode("EXCESSIVE80");
      excessiveDiscount.setPercentage(new BigDecimal("80.00")); // More than 75% limit
      excessiveDiscount.setType(DiscountType.GENERAL);
      
      List<Product> products = List.of(product1, product2);
      List<Discount> discounts = List.of(excessiveDiscount);
      
      when(productService.getProductsByIds(anyList())).thenReturn(products);
      when(discountService.validateAndGetDiscounts(anyList())).thenReturn(discounts);

      // Act & Assert
      assertThrows(ExcessiveDiscountException.class,
          () -> checkoutService.processCheckout(checkoutRequest, 1L));
    }

    @Test
    @DisplayName("Given product specific discount, when processing checkout, then apply discount correctly")
    void givenProductSpecificDiscount_whenProcessingCheckout_thenApplyDiscountCorrectly() {
      // Arrange
      checkoutRequest.setDiscountCodes(List.of("PRODUCT20"));
      List<Product> products = List.of(product1, product2);
      List<Discount> discounts = List.of(productSpecificDiscount);
      
      when(productService.getProductsByIds(anyList())).thenReturn(products);
      when(discountService.validateAndGetDiscounts(anyList())).thenReturn(discounts);
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(orderRepository.save(any())).thenReturn(null);

      // Act
      OrderResponse response = checkoutService.processCheckout(checkoutRequest, 1L);

      // Assert
      assertNotNull(response);
      // Product1: 100 - 20% = 80, Product2: 50 (no discount) = Total: 130
      assertEquals(new BigDecimal("130.00"), response.getFinalPrice());
    }

    @Test
    @DisplayName("Given mixed discounts, when processing checkout, then apply both discounts")
    void givenMixedDiscounts_whenProcessingCheckout_thenApplyBothDiscounts() {
      // Arrange
      checkoutRequest.setDiscountCodes(List.of("GENERAL10", "PRODUCT20"));
      List<Product> products = List.of(product1, product2);
      List<Discount> discounts = List.of(generalDiscount, productSpecificDiscount);
      
      when(productService.getProductsByIds(anyList())).thenReturn(products);
      when(discountService.validateAndGetDiscounts(anyList())).thenReturn(discounts);
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(orderRepository.save(any())).thenReturn(null);

      // Act
      OrderResponse response = checkoutService.processCheckout(checkoutRequest, 1L);

      // Assert
      assertNotNull(response);
      // Product1: 100 - 20% = 80, Product2: 50, Subtotal: 130, General 10%: 130 - 13 = 117
      assertEquals(new BigDecimal("117.00"), response.getFinalPrice());
    }

    @Test
    @DisplayName("Given no discounts, when processing checkout, then return original price")
    void givenNoDiscounts_whenProcessingCheckout_thenReturnOriginalPrice() {
      // Arrange
      checkoutRequest.setDiscountCodes(List.of());
      List<Product> products = List.of(product1, product2);
      List<Discount> discounts = List.of();
      
      when(productService.getProductsByIds(anyList())).thenReturn(products);
      when(discountService.validateAndGetDiscounts(anyList())).thenReturn(discounts);
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(orderRepository.save(any())).thenReturn(null);

      // Act
      OrderResponse response = checkoutService.processCheckout(checkoutRequest, 1L);

      // Assert
      assertNotNull(response);
      assertEquals(new BigDecimal("150.00"), response.getFinalPrice()); // 100 + 50
    }
  }
}
