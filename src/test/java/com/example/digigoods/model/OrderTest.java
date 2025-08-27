package com.example.digigoods.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderTest {

  private Order order;
  private User testUser;
  private Product testProduct1;
  private Product testProduct2;
  private Discount testDiscount1;
  private Discount testDiscount2;

  @BeforeEach
  void setUp() {
    order = new Order();
    
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setPassword("password");

    testProduct1 = new Product();
    testProduct1.setId(1L);
    testProduct1.setName("Product 1");
    testProduct1.setPrice(new BigDecimal("50.00"));
    testProduct1.setStock(10);

    testProduct2 = new Product();
    testProduct2.setId(2L);
    testProduct2.setName("Product 2");
    testProduct2.setPrice(new BigDecimal("30.00"));
    testProduct2.setStock(5);

    testDiscount1 = new Discount();
    testDiscount1.setId(1L);
    testDiscount1.setCode("SAVE10");
    testDiscount1.setPercentage(new BigDecimal("10.00"));

    testDiscount2 = new Discount();
    testDiscount2.setId(2L);
    testDiscount2.setCode("SAVE20");
    testDiscount2.setPercentage(new BigDecimal("20.00"));
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Given no-args constructor, when creating order, then initialize with default values")
    void givenNoArgsConstructor_whenCreatingOrder_thenInitializeWithDefaultValues() {
      // Act
      Order newOrder = new Order();

      // Assert
      assertNull(newOrder.getId());
      assertNull(newOrder.getUser());
      assertNotNull(newOrder.getProducts());
      assertTrue(newOrder.getProducts().isEmpty());
      assertNotNull(newOrder.getAppliedDiscounts());
      assertTrue(newOrder.getAppliedDiscounts().isEmpty());
      assertNull(newOrder.getOriginalSubtotal());
      assertNull(newOrder.getFinalPrice());
      assertNull(newOrder.getOrderDate());
    }

    @Test
    @DisplayName("Given all-args constructor, when creating order, then initialize with provided values")
    void givenAllArgsConstructor_whenCreatingOrder_thenInitializeWithProvidedValues() {
      // Arrange
      Long orderId = 1L;
      Set<Product> products = Set.of(testProduct1, testProduct2);
      Set<Discount> discounts = Set.of(testDiscount1);
      BigDecimal originalSubtotal = new BigDecimal("80.00");
      BigDecimal finalPrice = new BigDecimal("72.00");
      LocalDateTime orderDate = LocalDateTime.now();

      // Act
      Order newOrder = new Order(orderId, testUser, products, discounts, originalSubtotal, finalPrice, orderDate);

      // Assert
      assertEquals(orderId, newOrder.getId());
      assertEquals(testUser, newOrder.getUser());
      assertEquals(products, newOrder.getProducts());
      assertEquals(discounts, newOrder.getAppliedDiscounts());
      assertEquals(originalSubtotal, newOrder.getOriginalSubtotal());
      assertEquals(finalPrice, newOrder.getFinalPrice());
      assertEquals(orderDate, newOrder.getOrderDate());
    }
  }

  @Nested
  @DisplayName("Property Tests")
  class PropertyTests {

    @Test
    @DisplayName("Given order, when setting and getting properties, then properties are correctly stored")
    void givenOrder_whenSettingAndGettingProperties_thenPropertiesAreCorrectlyStored() {
      // Arrange
      Long orderId = 123L;
      BigDecimal originalSubtotal = new BigDecimal("100.00");
      BigDecimal finalPrice = new BigDecimal("85.00");
      LocalDateTime orderDate = LocalDateTime.now();

      // Act
      order.setId(orderId);
      order.setUser(testUser);
      order.setOriginalSubtotal(originalSubtotal);
      order.setFinalPrice(finalPrice);
      order.setOrderDate(orderDate);

      // Assert
      assertEquals(orderId, order.getId());
      assertEquals(testUser, order.getUser());
      assertEquals(originalSubtotal, order.getOriginalSubtotal());
      assertEquals(finalPrice, order.getFinalPrice());
      assertEquals(orderDate, order.getOrderDate());
    }

    @Test
    @DisplayName("Given order, when adding products, then products are correctly stored")
    void givenOrder_whenAddingProducts_thenProductsAreCorrectlyStored() {
      // Act
      order.getProducts().add(testProduct1);
      order.getProducts().add(testProduct2);

      // Assert
      assertEquals(2, order.getProducts().size());
      assertTrue(order.getProducts().contains(testProduct1));
      assertTrue(order.getProducts().contains(testProduct2));
    }

    @Test
    @DisplayName("Given order, when adding applied discounts, then discounts are correctly stored")
    void givenOrder_whenAddingAppliedDiscounts_thenDiscountsAreCorrectlyStored() {
      // Act
      order.getAppliedDiscounts().add(testDiscount1);
      order.getAppliedDiscounts().add(testDiscount2);

      // Assert
      assertEquals(2, order.getAppliedDiscounts().size());
      assertTrue(order.getAppliedDiscounts().contains(testDiscount1));
      assertTrue(order.getAppliedDiscounts().contains(testDiscount2));
    }

    @Test
    @DisplayName("Given order, when setting products collection, then collection is correctly replaced")
    void givenOrder_whenSettingProductsCollection_thenCollectionIsCorrectlyReplaced() {
      // Arrange
      Set<Product> newProducts = new HashSet<>();
      newProducts.add(testProduct1);

      // Act
      order.setProducts(newProducts);

      // Assert
      assertEquals(newProducts, order.getProducts());
      assertEquals(1, order.getProducts().size());
      assertTrue(order.getProducts().contains(testProduct1));
    }

    @Test
    @DisplayName("Given order, when setting applied discounts collection, then collection is correctly replaced")
    void givenOrder_whenSettingAppliedDiscountsCollection_thenCollectionIsCorrectlyReplaced() {
      // Arrange
      Set<Discount> newDiscounts = new HashSet<>();
      newDiscounts.add(testDiscount2);

      // Act
      order.setAppliedDiscounts(newDiscounts);

      // Assert
      assertEquals(newDiscounts, order.getAppliedDiscounts());
      assertEquals(1, order.getAppliedDiscounts().size());
      assertTrue(order.getAppliedDiscounts().contains(testDiscount2));
    }
  }

  @Nested
  @DisplayName("PrePersist Tests")
  class PrePersistTests {

    @Test
    @DisplayName("Given order without order date, when calling onCreate, then order date is set to current time")
    void givenOrderWithoutOrderDate_whenCallingOnCreate_thenOrderDateIsSetToCurrentTime() {
      // Arrange
      assertNull(order.getOrderDate());
      LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

      // Act
      order.onCreate();

      // Assert
      LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);
      assertNotNull(order.getOrderDate());
      assertTrue(order.getOrderDate().isAfter(beforeCreate));
      assertTrue(order.getOrderDate().isBefore(afterCreate));
    }

    @Test
    @DisplayName("Given order with existing order date, when calling onCreate, then order date is updated")
    void givenOrderWithExistingOrderDate_whenCallingOnCreate_thenOrderDateIsUpdated() {
      // Arrange
      LocalDateTime originalDate = LocalDateTime.now().minusDays(1);
      order.setOrderDate(originalDate);
      LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

      // Act
      order.onCreate();

      // Assert
      LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);
      assertNotNull(order.getOrderDate());
      assertTrue(order.getOrderDate().isAfter(beforeCreate));
      assertTrue(order.getOrderDate().isBefore(afterCreate));
      assertTrue(order.getOrderDate().isAfter(originalDate));
    }
  }

  @Nested
  @DisplayName("Equals and HashCode Tests")
  class EqualsAndHashCodeTests {

    @Test
    @DisplayName("Given two orders with same properties, when comparing, then they are equal")
    void givenTwoOrdersWithSameProperties_whenComparing_thenTheyAreEqual() {
      // Arrange
      Order order1 = new Order();
      order1.setId(1L);
      order1.setUser(testUser);
      order1.setOriginalSubtotal(new BigDecimal("100.00"));
      order1.setFinalPrice(new BigDecimal("90.00"));

      Order order2 = new Order();
      order2.setId(1L);
      order2.setUser(testUser);
      order2.setOriginalSubtotal(new BigDecimal("100.00"));
      order2.setFinalPrice(new BigDecimal("90.00"));

      // Act & Assert
      assertEquals(order1, order2);
      assertEquals(order1.hashCode(), order2.hashCode());
    }
  }
}
