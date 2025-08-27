package com.example.digigoods.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.digigoods.dto.CheckoutRequest;
import com.example.digigoods.dto.OrderResponse;
import com.example.digigoods.exception.MissingJwtTokenException;
import com.example.digigoods.service.CheckoutService;
import com.example.digigoods.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

  @Mock
  private CheckoutService checkoutService;

  @Mock
  private JwtService jwtService;

  @Mock
  private HttpServletRequest httpServletRequest;

  @InjectMocks
  private CheckoutController checkoutController;

  private CheckoutRequest validCheckoutRequest;
  private OrderResponse mockOrderResponse;

  @BeforeEach
  void setUp() {
    validCheckoutRequest = new CheckoutRequest();
    validCheckoutRequest.setProductIds(List.of(1L, 2L));
    validCheckoutRequest.setDiscountCodes(List.of("SAVE10"));

    mockOrderResponse = new OrderResponse();
    mockOrderResponse.setMessage("Order created successfully");
    mockOrderResponse.setFinalPrice(new BigDecimal("90.00"));
  }

  @Nested
  @DisplayName("Create Order Tests")
  class CreateOrderTests {

    @Test
    @DisplayName("Given valid request with Bearer token, when creating order, "
        + "then return order response")
    void givenValidRequestWithBearerToken_whenCreatingOrder_thenReturnOrderResponse() {
      // Arrange
      String token = "valid.jwt.token";
      String bearerToken = "Bearer " + token;
      Long userId = 123L;

      when(httpServletRequest.getHeader("Authorization")).thenReturn(bearerToken);
      when(jwtService.extractUserId(token)).thenReturn(userId);
      when(checkoutService.processCheckout(any(CheckoutRequest.class), eq(userId)))
          .thenReturn(mockOrderResponse);

      // Act
      ResponseEntity<OrderResponse> response = checkoutController.createOrder(
          validCheckoutRequest, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(mockOrderResponse.getMessage(), response.getBody().getMessage());
      assertEquals(mockOrderResponse.getFinalPrice(), response.getBody().getFinalPrice());

      verify(jwtService).extractUserId(token);
      verify(checkoutService).processCheckout(validCheckoutRequest, userId);
    }

    @Test
    @DisplayName("Given request without Authorization header, when creating order, "
        + "then throw MissingJwtTokenException")
    void givenRequestWithoutAuthorizationHeader_whenCreatingOrder_thenThrowMissingJwtTokenException() {
      // Arrange
      when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

      // Act & Assert
      assertThrows(MissingJwtTokenException.class,
          () -> checkoutController.createOrder(validCheckoutRequest, httpServletRequest));
    }

    @Test
    @DisplayName("Given request with empty Authorization header, when creating order, then throw MissingJwtTokenException")
    void givenRequestWithEmptyAuthorizationHeader_whenCreatingOrder_thenThrowMissingJwtTokenException() {
      // Arrange
      when(httpServletRequest.getHeader("Authorization")).thenReturn("");

      // Act & Assert
      assertThrows(MissingJwtTokenException.class,
          () -> checkoutController.createOrder(validCheckoutRequest, httpServletRequest));
    }

    @Test
    @DisplayName("Given request with non-Bearer token, when creating order, then throw MissingJwtTokenException")
    void givenRequestWithNonBearerToken_whenCreatingOrder_thenThrowMissingJwtTokenException() {
      // Arrange
      when(httpServletRequest.getHeader("Authorization")).thenReturn("Basic sometoken");

      // Act & Assert
      assertThrows(MissingJwtTokenException.class,
          () -> checkoutController.createOrder(validCheckoutRequest, httpServletRequest));
    }

    @Test
    @DisplayName("Given request with Bearer prefix only, when creating order, then extract empty token and call JWT service")
    void givenRequestWithBearerPrefixOnly_whenCreatingOrder_thenExtractEmptyTokenAndCallJwtService() {
      // Arrange
      String emptyToken = "";
      Long userId = 456L;

      when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer ");
      when(jwtService.extractUserId(emptyToken)).thenReturn(userId);
      when(checkoutService.processCheckout(any(CheckoutRequest.class), eq(userId)))
          .thenReturn(mockOrderResponse);

      // Act
      ResponseEntity<OrderResponse> response = checkoutController.createOrder(validCheckoutRequest, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(jwtService).extractUserId(emptyToken);
      verify(checkoutService).processCheckout(validCheckoutRequest, userId);
    }

    @Test
    @DisplayName("Given valid Bearer token, when creating order, then extract correct token")
    void givenValidBearerToken_whenCreatingOrder_thenExtractCorrectToken() {
      // Arrange
      String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
      String bearerToken = "Bearer " + expectedToken;
      Long userId = 456L;

      when(httpServletRequest.getHeader("Authorization")).thenReturn(bearerToken);
      when(jwtService.extractUserId(expectedToken)).thenReturn(userId);
      when(checkoutService.processCheckout(any(CheckoutRequest.class), eq(userId)))
          .thenReturn(mockOrderResponse);

      // Act
      ResponseEntity<OrderResponse> response = checkoutController.createOrder(validCheckoutRequest, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(jwtService).extractUserId(expectedToken);
      verify(checkoutService).processCheckout(validCheckoutRequest, userId);
    }
  }

  @Nested
  @DisplayName("Token Extraction Tests")
  class TokenExtractionTests {

    @Test
    @DisplayName("Given valid Bearer token, when extracting token, then return token without Bearer prefix")
    void givenValidBearerToken_whenExtractingToken_thenReturnTokenWithoutBearerPrefix() {
      // Arrange
      String token = "valid.jwt.token";
      String bearerToken = "Bearer " + token;
      Long userId = 789L;

      when(httpServletRequest.getHeader("Authorization")).thenReturn(bearerToken);
      when(jwtService.extractUserId(token)).thenReturn(userId);
      when(checkoutService.processCheckout(any(CheckoutRequest.class), eq(userId)))
          .thenReturn(mockOrderResponse);

      // Act
      checkoutController.createOrder(validCheckoutRequest, httpServletRequest);

      // Assert
      verify(jwtService).extractUserId(token); // Verify the token was extracted correctly
    }

    @Test
    @DisplayName("Given Authorization header with extra spaces, when extracting token, then handle correctly")
    void givenAuthorizationHeaderWithExtraSpaces_whenExtractingToken_thenHandleCorrectly() {
      // Arrange
      String token = "valid.jwt.token";
      String bearerToken = "Bearer  " + token; // Extra space
      Long userId = 999L;

      when(httpServletRequest.getHeader("Authorization")).thenReturn(bearerToken);
      when(jwtService.extractUserId(" " + token)).thenReturn(userId); // Token will include the extra space
      when(checkoutService.processCheckout(any(CheckoutRequest.class), eq(userId)))
          .thenReturn(mockOrderResponse);

      // Act
      checkoutController.createOrder(validCheckoutRequest, httpServletRequest);

      // Assert
      verify(jwtService).extractUserId(" " + token);
    }
  }
}
