package com.example.digigoods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.digigoods.exception.InvalidDiscountException;
import com.example.digigoods.model.Discount;
import com.example.digigoods.model.DiscountType;
import com.example.digigoods.repository.DiscountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

  @Mock
  private DiscountRepository discountRepository;

  @InjectMocks
  private DiscountService discountService;

  private Discount validDiscount;
  private Discount expiredDiscount;

  @BeforeEach
  void setUp() {
    validDiscount = new Discount();
    validDiscount.setId(1L);
    validDiscount.setCode("VALID10");
    validDiscount.setPercentage(new BigDecimal("10.00"));
    validDiscount.setType(DiscountType.GENERAL);
    validDiscount.setValidFrom(LocalDate.now().minusDays(1));
    validDiscount.setValidUntil(LocalDate.now().plusDays(30));
    validDiscount.setRemainingUses(5);

    expiredDiscount = new Discount();
    expiredDiscount.setId(2L);
    expiredDiscount.setCode("EXPIRED10");
    expiredDiscount.setPercentage(new BigDecimal("10.00"));
    expiredDiscount.setType(DiscountType.GENERAL);
    expiredDiscount.setValidFrom(LocalDate.now().minusDays(30));
    expiredDiscount.setValidUntil(LocalDate.now().minusDays(1)); // Expired
    expiredDiscount.setRemainingUses(5);
  }

  @Test
  @DisplayName("Given discounts exist, when getting all discounts, then return all discounts")
  void givenDiscountsExist_whenGettingAllDiscounts_thenReturnAllDiscounts() {
    // Arrange
    List<Discount> expectedDiscounts = List.of(validDiscount, expiredDiscount);
    when(discountRepository.findAll()).thenReturn(expectedDiscounts);

    // Act
    List<Discount> actualDiscounts = discountService.getAllDiscounts();

    // Assert
    assertNotNull(actualDiscounts);
    assertEquals(2, actualDiscounts.size());
    assertEquals(expectedDiscounts, actualDiscounts);
    verify(discountRepository).findAll();
  }

  @Test
  @DisplayName("Given null discount codes, when validating, then return empty list")
  void givenNullDiscountCodes_whenValidating_thenReturnEmptyList() {
    // Act
    List<Discount> result = discountService.validateAndGetDiscounts(null);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Given empty discount codes, when validating, then return empty list")
  void givenEmptyDiscountCodes_whenValidating_thenReturnEmptyList() {
    // Act
    List<Discount> result = discountService.validateAndGetDiscounts(List.of());

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Given valid discount codes, when validating, then return valid discounts")
  void givenValidDiscountCodes_whenValidating_thenReturnValidDiscounts() {
    // Arrange
    List<String> discountCodes = List.of("VALID10");
    when(discountRepository.findAllByCodeIn(discountCodes)).thenReturn(List.of(validDiscount));

    // Act
    List<Discount> result = discountService.validateAndGetDiscounts(discountCodes);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(validDiscount, result.get(0));
  }

  @Test
  @DisplayName("Given non-existent discount code, when validating, then throw InvalidDiscountException")
  void givenNonExistentDiscountCode_whenValidating_thenThrowInvalidDiscountException() {
    // Arrange
    List<String> discountCodes = List.of("NONEXISTENT");
    when(discountRepository.findAllByCodeIn(discountCodes)).thenReturn(List.of());

    // Act & Assert
    InvalidDiscountException exception = assertThrows(InvalidDiscountException.class,
        () -> discountService.validateAndGetDiscounts(discountCodes));
    assertTrue(exception.getMessage().contains("NONEXISTENT"));
    assertTrue(exception.getMessage().contains("discount code not found"));
  }

  @Test
  @DisplayName("Given expired discount code, when validating, then throw InvalidDiscountException")
  void givenExpiredDiscountCode_whenValidating_thenThrowInvalidDiscountException() {
    // Arrange
    List<String> discountCodes = List.of("EXPIRED10");
    when(discountRepository.findAllByCodeIn(discountCodes)).thenReturn(List.of(expiredDiscount));

    // Act & Assert
    InvalidDiscountException exception = assertThrows(InvalidDiscountException.class,
        () -> discountService.validateAndGetDiscounts(discountCodes));
    assertTrue(exception.getMessage().contains("EXPIRED10"));
    assertTrue(exception.getMessage().contains("discount has expired"));
  }

  @Test
  @DisplayName("Given valid discounts, when updating usage, then decrement remaining uses")
  void givenValidDiscounts_whenUpdatingUsage_thenDecrementRemainingUses() {
    // Arrange
    Discount discount1 = new Discount();
    discount1.setRemainingUses(5);
    Discount discount2 = new Discount();
    discount2.setRemainingUses(3);

    List<Discount> discounts = List.of(discount1, discount2);

    // Act
    discountService.updateDiscountUsage(discounts);

    // Assert
    assertEquals(4, discount1.getRemainingUses());
    assertEquals(2, discount2.getRemainingUses());
    verify(discountRepository, times(2)).save(any(Discount.class));
  }

  @Test
  @DisplayName("Given empty discount list, when updating usage, then do nothing")
  void givenEmptyDiscountList_whenUpdatingUsage_thenDoNothing() {
    // Act
    discountService.updateDiscountUsage(List.of());

    // Assert
    verify(discountRepository, times(0)).save(any(Discount.class));
  }

  @Test
  @DisplayName("Given discount not yet valid, when validating, then throw InvalidDiscountException")
  void givenDiscountNotYetValid_whenValidating_thenThrowInvalidDiscountException() {
    // Arrange
    Discount futureDiscount = new Discount();
    futureDiscount.setCode("FUTURE10");
    futureDiscount.setValidFrom(LocalDate.now().plusDays(1));
    futureDiscount.setValidUntil(LocalDate.now().plusDays(30));
    futureDiscount.setRemainingUses(5);

    List<String> discountCodes = List.of("FUTURE10");
    when(discountRepository.findAllByCodeIn(discountCodes)).thenReturn(List.of(futureDiscount));

    // Act & Assert
    InvalidDiscountException exception = assertThrows(InvalidDiscountException.class,
        () -> discountService.validateAndGetDiscounts(discountCodes));
    assertTrue(exception.getMessage().contains("FUTURE10"));
    assertTrue(exception.getMessage().contains("discount is not yet valid"));
  }

  @Test
  @DisplayName("Given discount with no remaining uses, when validating, then throw InvalidDiscountException")
  void givenDiscountWithNoRemainingUses_whenValidating_thenThrowInvalidDiscountException() {
    // Arrange
    Discount noUsesDiscount = new Discount();
    noUsesDiscount.setCode("NOUSES10");
    noUsesDiscount.setValidFrom(LocalDate.now().minusDays(1));
    noUsesDiscount.setValidUntil(LocalDate.now().plusDays(30));
    noUsesDiscount.setRemainingUses(0);

    List<String> discountCodes = List.of("NOUSES10");
    when(discountRepository.findAllByCodeIn(discountCodes)).thenReturn(List.of(noUsesDiscount));

    // Act & Assert
    InvalidDiscountException exception = assertThrows(InvalidDiscountException.class,
        () -> discountService.validateAndGetDiscounts(discountCodes));
    assertTrue(exception.getMessage().contains("NOUSES10"));
    assertTrue(exception.getMessage().contains("discount has no remaining uses"));
  }

  @Test
  @DisplayName("Given multiple discount codes with one invalid, when validating, then throw InvalidDiscountException")
  void givenMultipleDiscountCodesWithOneInvalid_whenValidating_thenThrowInvalidDiscountException() {
    // Arrange
    List<String> discountCodes = List.of("VALID10", "INVALID");
    when(discountRepository.findAllByCodeIn(discountCodes)).thenReturn(List.of(validDiscount));

    // Act & Assert
    InvalidDiscountException exception = assertThrows(InvalidDiscountException.class,
        () -> discountService.validateAndGetDiscounts(discountCodes));
    assertTrue(exception.getMessage().contains("INVALID"));
    assertTrue(exception.getMessage().contains("discount code not found"));
  }
}
