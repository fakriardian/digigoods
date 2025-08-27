package com.example.digigoods.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NotificationExceptionTest {

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Given default constructor, when creating exception, then use default message")
    void givenDefaultConstructor_whenCreatingException_thenUseDefaultMessage() {
      // Act
      NotificationException exception = new NotificationException();

      // Assert
      assertNotNull(exception);
      assertEquals("Notification operation failed", exception.getMessage());
      assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Given custom message, when creating exception, then use custom message")
    void givenCustomMessage_whenCreatingException_thenUseCustomMessage() {
      // Arrange
      String customMessage = "Failed to send notification to user: 123";

      // Act
      NotificationException exception = new NotificationException(customMessage);

      // Assert
      assertNotNull(exception);
      assertEquals(customMessage, exception.getMessage());
      assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Given null message, when creating exception, then accept null message")
    void givenNullMessage_whenCreatingException_thenAcceptNullMessage() {
      // Act
      NotificationException exception = new NotificationException((String) null);

      // Assert
      assertNotNull(exception);
      assertNull(exception.getMessage());
      assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Given empty message, when creating exception, then use empty message")
    void givenEmptyMessage_whenCreatingException_thenUseEmptyMessage() {
      // Arrange
      String emptyMessage = "";

      // Act
      NotificationException exception = new NotificationException(emptyMessage);

      // Assert
      assertNotNull(exception);
      assertEquals(emptyMessage, exception.getMessage());
      assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Given message and cause, when creating exception, then use both message and cause")
    void givenMessageAndCause_whenCreatingException_thenUseBothMessageAndCause() {
      // Arrange
      String message = "Notification service unavailable";
      Throwable cause = new RuntimeException("Database connection failed");

      // Act
      NotificationException exception = new NotificationException(message, cause);

      // Assert
      assertNotNull(exception);
      assertEquals(message, exception.getMessage());
      assertSame(cause, exception.getCause());
    }

    @Test
    @DisplayName("Given null message and cause, when creating exception, then accept both null values")
    void givenNullMessageAndCause_whenCreatingException_thenAcceptBothNullValues() {
      // Act
      NotificationException exception = new NotificationException(null, null);

      // Assert
      assertNotNull(exception);
      assertNull(exception.getMessage());
      assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Given message and null cause, when creating exception, then use message with null cause")
    void givenMessageAndNullCause_whenCreatingException_thenUseMessageWithNullCause() {
      // Arrange
      String message = "User not found for notification";

      // Act
      NotificationException exception = new NotificationException(message, null);

      // Assert
      assertNotNull(exception);
      assertEquals(message, exception.getMessage());
      assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Given null message and cause, when creating exception, then use null message with cause")
    void givenNullMessageAndCause_whenCreatingException_thenUseNullMessageWithCause() {
      // Arrange
      Throwable cause = new IllegalArgumentException("Invalid notification type");

      // Act
      NotificationException exception = new NotificationException(null, cause);

      // Assert
      assertNotNull(exception);
      assertNull(exception.getMessage());
      assertSame(cause, exception.getCause());
    }
  }

  @Nested
  @DisplayName("Exception Behavior Tests")
  class ExceptionBehaviorTests {

    @Test
    @DisplayName("Given NotificationException, when checking inheritance, then extends RuntimeException")
    void givenNotificationException_whenCheckingInheritance_thenExtendsRuntimeException() {
      // Act
      NotificationException exception = new NotificationException();

      // Assert
      assertNotNull(exception);
      assertEquals(RuntimeException.class, exception.getClass().getSuperclass());
    }

    @Test
    @DisplayName("Given NotificationException with cause, when getting cause, then return correct cause")
    void givenNotificationExceptionWithCause_whenGettingCause_thenReturnCorrectCause() {
      // Arrange
      RuntimeException originalCause = new RuntimeException("Original error");
      NotificationException exception = new NotificationException("Wrapper message", originalCause);

      // Act
      Throwable retrievedCause = exception.getCause();

      // Assert
      assertSame(originalCause, retrievedCause);
      assertEquals("Original error", retrievedCause.getMessage());
    }

    @Test
    @DisplayName("Given NotificationException with nested cause, when getting root cause, then traverse cause chain")
    void givenNotificationExceptionWithNestedCause_whenGettingRootCause_thenTraverseCauseChain() {
      // Arrange
      IllegalArgumentException rootCause = new IllegalArgumentException("Root cause");
      RuntimeException intermediateCause = new RuntimeException("Intermediate cause", rootCause);
      NotificationException exception = new NotificationException("Top level message", intermediateCause);

      // Act
      Throwable cause = exception.getCause();
      Throwable rootCauseRetrieved = cause.getCause();

      // Assert
      assertSame(intermediateCause, cause);
      assertSame(rootCause, rootCauseRetrieved);
      assertEquals("Root cause", rootCauseRetrieved.getMessage());
    }
  }

  @Nested
  @DisplayName("Real-world Usage Tests")
  class RealWorldUsageTests {

    @Test
    @DisplayName("Given notification service failure, when creating exception, then provide meaningful message")
    void givenNotificationServiceFailure_whenCreatingException_thenProvideMeaningfulMessage() {
      // Arrange
      String userSpecificMessage = "Failed to send order confirmation to user ID: 12345";

      // Act
      NotificationException exception = new NotificationException(userSpecificMessage);

      // Assert
      assertEquals(userSpecificMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Given external service timeout, when wrapping in NotificationException, then preserve original cause")
    void givenExternalServiceTimeout_whenWrappingInNotificationException_thenPreserveOriginalCause() {
      // Arrange
      RuntimeException timeoutException = new RuntimeException("Connection timeout after 30 seconds");
      String wrapperMessage = "Email service unavailable";

      // Act
      NotificationException exception = new NotificationException(wrapperMessage, timeoutException);

      // Assert
      assertEquals(wrapperMessage, exception.getMessage());
      assertSame(timeoutException, exception.getCause());
      assertEquals("Connection timeout after 30 seconds", exception.getCause().getMessage());
    }
  }
}
