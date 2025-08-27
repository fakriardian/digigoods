package com.example.digigoods.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.example.digigoods.dto.ErrorResponse;
import com.example.digigoods.exception.ExcessiveDiscountException;
import com.example.digigoods.exception.InsufficientStockException;
import com.example.digigoods.exception.InvalidDiscountException;
import com.example.digigoods.exception.MissingJwtTokenException;
import com.example.digigoods.exception.NotificationException;
import com.example.digigoods.exception.ProductNotFoundException;
import com.example.digigoods.exception.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private MethodArgumentNotValidException methodArgumentNotValidException;

  @Mock
  private BindingResult bindingResult;

  @InjectMocks
  private GlobalExceptionHandler globalExceptionHandler;

  private static final String TEST_REQUEST_URI = "/api/test";

  @BeforeEach
  void setUp() {
    when(httpServletRequest.getRequestURI()).thenReturn(TEST_REQUEST_URI);
  }

  @Nested
  @DisplayName("Product Not Found Exception Tests")
  class ProductNotFoundExceptionTests {

    @Test
    @DisplayName("Given ProductNotFoundException, when handling exception, then return 404 response")
    void givenProductNotFoundException_whenHandlingException_thenReturn404Response() {
      // Arrange
      ProductNotFoundException exception = new ProductNotFoundException(123L);

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleProductNotFoundException(exception, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(404, response.getBody().getStatus());
      assertEquals("Not Found", response.getBody().getError());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }
  }

  @Nested
  @DisplayName("Bad Request Exception Tests")
  class BadRequestExceptionTests {

    @Test
    @DisplayName("Given InvalidDiscountException, when handling exception, then return 400 response")
    void givenInvalidDiscountException_whenHandlingException_thenReturn400Response() {
      // Arrange
      String discountCode = "INVALID10";
      String reason = "discount code not found";
      InvalidDiscountException exception = new InvalidDiscountException(discountCode, reason);

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleBadRequestExceptions(exception, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(400, response.getBody().getStatus());
      assertEquals("Bad Request", response.getBody().getError());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }

    @Test
    @DisplayName("Given ExcessiveDiscountException, when handling exception, then return 400 response")
    void givenExcessiveDiscountException_whenHandlingException_thenReturn400Response() {
      // Arrange
      ExcessiveDiscountException exception = new ExcessiveDiscountException("Discount exceeds 75% limit");

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleBadRequestExceptions(exception, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(400, response.getBody().getStatus());
      assertEquals("Bad Request", response.getBody().getError());
      assertEquals("Discount exceeds 75% limit", response.getBody().getMessage());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }

    @Test
    @DisplayName("Given InsufficientStockException, when handling exception, then return 400 response")
    void givenInsufficientStockException_whenHandlingException_thenReturn400Response() {
      // Arrange
      InsufficientStockException exception = new InsufficientStockException("Insufficient stock for product");

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleBadRequestExceptions(exception, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(400, response.getBody().getStatus());
      assertEquals("Bad Request", response.getBody().getError());
      assertEquals("Insufficient stock for product", response.getBody().getMessage());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }

    @Test
    @DisplayName("Given NotificationException, when handling exception, then return 400 response")
    void givenNotificationException_whenHandlingException_thenReturn400Response() {
      // Arrange
      NotificationException exception = new NotificationException("Failed to send notification");

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleBadRequestExceptions(exception, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(400, response.getBody().getStatus());
      assertEquals("Bad Request", response.getBody().getError());
      assertEquals("Failed to send notification", response.getBody().getMessage());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }
  }

  @Nested
  @DisplayName("Unauthorized Access Exception Tests")
  class UnauthorizedAccessExceptionTests {

    @Test
    @DisplayName("Given UnauthorizedAccessException, when handling exception, then return 403 response")
    void givenUnauthorizedAccessException_whenHandlingException_thenReturn403Response() {
      // Arrange
      UnauthorizedAccessException exception = new UnauthorizedAccessException("Access denied");

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleUnauthorizedAccessException(exception, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(403, response.getBody().getStatus());
      assertEquals("Forbidden", response.getBody().getError());
      assertEquals("Access denied", response.getBody().getMessage());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }
  }

  @Nested
  @DisplayName("Authentication Exception Tests")
  class AuthenticationExceptionTests {

    @Test
    @DisplayName("Given BadCredentialsException, when handling exception, then return 401 response")
    void givenBadCredentialsException_whenHandlingException_thenReturn401Response() {
      // Arrange
      BadCredentialsException exception = new BadCredentialsException("Bad credentials");

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleBadCredentialsException(exception, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(401, response.getBody().getStatus());
      assertEquals("Unauthorized", response.getBody().getError());
      assertEquals("Invalid username or password", response.getBody().getMessage());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }

    @Test
    @DisplayName("Given MissingJwtTokenException, when handling exception, then return 401 response")
    void givenMissingJwtTokenException_whenHandlingException_thenReturn401Response() {
      // Arrange
      MissingJwtTokenException exception = new MissingJwtTokenException();

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleMissingJwtTokenException(exception, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(401, response.getBody().getStatus());
      assertEquals("Unauthorized", response.getBody().getError());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }
  }

  @Nested
  @DisplayName("Validation Exception Tests")
  class ValidationExceptionTests {

    @Test
    @DisplayName("Given MethodArgumentNotValidException with single field error, when handling exception, then return 400 response")
    void givenMethodArgumentNotValidExceptionWithSingleFieldError_whenHandlingException_thenReturn400Response() {
      // Arrange
      FieldError fieldError = new FieldError("checkoutRequest", "productIds", "Product IDs cannot be empty");
      when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
      when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleValidationExceptions(methodArgumentNotValidException, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(400, response.getBody().getStatus());
      assertEquals("Bad Request", response.getBody().getError());
      assertEquals("Product IDs cannot be empty", response.getBody().getMessage());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }

    @Test
    @DisplayName("Given MethodArgumentNotValidException with multiple field errors, when handling exception, then return 400 response with combined message")
    void givenMethodArgumentNotValidExceptionWithMultipleFieldErrors_whenHandlingException_thenReturn400ResponseWithCombinedMessage() {
      // Arrange
      FieldError fieldError1 = new FieldError("checkoutRequest", "productIds", "Product IDs cannot be empty");
      FieldError fieldError2 = new FieldError("checkoutRequest", "userId", "User ID is required");
      when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
      when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleValidationExceptions(methodArgumentNotValidException, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(400, response.getBody().getStatus());
      assertEquals("Bad Request", response.getBody().getError());
      assertEquals("Product IDs cannot be empty, User ID is required", response.getBody().getMessage());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }

    @Test
    @DisplayName("Given MethodArgumentNotValidException with no field errors, when handling exception, then return 400 response with empty message")
    void givenMethodArgumentNotValidExceptionWithNoFieldErrors_whenHandlingException_thenReturn400ResponseWithEmptyMessage() {
      // Arrange
      when(bindingResult.getFieldErrors()).thenReturn(List.of());
      when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleValidationExceptions(methodArgumentNotValidException, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(400, response.getBody().getStatus());
      assertEquals("Bad Request", response.getBody().getError());
      assertEquals("", response.getBody().getMessage());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }
  }

  @Nested
  @DisplayName("Generic Exception Tests")
  class GenericExceptionTests {

    @Test
    @DisplayName("Given generic Exception, when handling exception, then return 500 response")
    void givenGenericException_whenHandlingException_thenReturn500Response() {
      // Arrange
      Exception exception = new RuntimeException("Unexpected error occurred");

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleGenericException(exception, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(500, response.getBody().getStatus());
      assertEquals("Internal Server Error", response.getBody().getError());
      assertEquals("An unexpected error occurred", response.getBody().getMessage());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }

    @Test
    @DisplayName("Given NullPointerException, when handling exception, then return 500 response")
    void givenNullPointerException_whenHandlingException_thenReturn500Response() {
      // Arrange
      NullPointerException exception = new NullPointerException("Null pointer error");

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleGenericException(exception, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(500, response.getBody().getStatus());
      assertEquals("Internal Server Error", response.getBody().getError());
      assertEquals("An unexpected error occurred", response.getBody().getMessage());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }

    @Test
    @DisplayName("Given IllegalArgumentException, when handling exception, then return 500 response")
    void givenIllegalArgumentException_whenHandlingException_thenReturn500Response() {
      // Arrange
      IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

      // Act
      ResponseEntity<ErrorResponse> response = globalExceptionHandler
          .handleGenericException(exception, httpServletRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(500, response.getBody().getStatus());
      assertEquals("Internal Server Error", response.getBody().getError());
      assertEquals("An unexpected error occurred", response.getBody().getMessage());
      assertEquals(TEST_REQUEST_URI, response.getBody().getPath());
    }
  }
}
