package com.example.digigoods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.digigoods.dto.NotificationResponse;
import com.example.digigoods.exception.NotificationException;
import com.example.digigoods.model.NotificationType;
import com.example.digigoods.model.User;
import com.example.digigoods.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private NotificationService notificationService;

  private User testUser;
  private User anotherUser;

  @BeforeEach
  void setUp() {
    testUser = new User(1L, "john.smith", "password123");
    anotherUser = new User(2L, "maria.garcia", "password456");
  }

  @Nested
  @DisplayName("Send Notification Tests")
  class SendNotificationTests {

    @Test
    @DisplayName("Given valid user and notification data, when sending notification, "
        + "then return success response")
    void givenValidUserAndNotificationData_whenSendingNotification_thenReturnSuccessResponse() {
      // Arrange
      Long userId = 1L;
      NotificationType type = NotificationType.ORDER_CONFIRMATION;
      String message = "Your order has been confirmed!";
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      // Act
      NotificationResponse response = notificationService.sendNotification(userId, type, message);

      // Assert
      assertNotNull(response);
      assertTrue(response.isSuccess());
      assertEquals("Notification sent successfully", response.getMessage());
      assertNotNull(response.getNotificationId());
      assertTrue(response.getNotificationId().startsWith("NOTIF_"));
      assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("Given null user ID, when sending notification, then throw NotificationException")
    void givenNullUserId_whenSendingNotification_thenThrowNotificationException() {
      // Arrange
      Long userId = null;
      NotificationType type = NotificationType.ORDER_CONFIRMATION;
      String message = "Test message";

      // Act & Assert
      NotificationException exception = assertThrows(NotificationException.class,
          () -> notificationService.sendNotification(userId, type, message));
      assertEquals("User ID cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Given null notification type, when sending notification, "
        + "then throw NotificationException")
    void givenNullNotificationType_whenSendingNotification_thenThrowNotificationException() {
      // Arrange
      Long userId = 1L;
      NotificationType type = null;
      String message = "Test message";

      // Act & Assert
      NotificationException exception = assertThrows(NotificationException.class,
          () -> notificationService.sendNotification(userId, type, message));
      assertEquals("Notification type cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Given null message, when sending notification, then throw NotificationException")
    void givenNullMessage_whenSendingNotification_thenThrowNotificationException() {
      // Arrange
      Long userId = 1L;
      NotificationType type = NotificationType.ORDER_CONFIRMATION;
      String message = null;

      // Act & Assert
      NotificationException exception = assertThrows(NotificationException.class,
          () -> notificationService.sendNotification(userId, type, message));
      assertEquals("Notification message cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Given empty message, when sending notification, "
        + "then throw NotificationException")
    void givenEmptyMessage_whenSendingNotification_thenThrowNotificationException() {
      // Arrange
      Long userId = 1L;
      NotificationType type = NotificationType.ORDER_CONFIRMATION;
      String message = "   ";

      // Act & Assert
      NotificationException exception = assertThrows(NotificationException.class,
          () -> notificationService.sendNotification(userId, type, message));
      assertEquals("Notification message cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Given non-existent user ID, when sending notification, "
        + "then throw NotificationException")
    void givenNonExistentUserId_whenSendingNotification_thenThrowNotificationException() {
      // Arrange
      Long userId = 999L;
      NotificationType type = NotificationType.ORDER_CONFIRMATION;
      String message = "Test message";
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // Act & Assert
      NotificationException exception = assertThrows(NotificationException.class,
          () -> notificationService.sendNotification(userId, type, message));
      assertEquals("User not found with ID: 999", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("Send Bulk Notifications Tests")
  class SendBulkNotificationsTests {

    @Test
    @DisplayName("Given valid user IDs, when sending bulk notifications, "
        + "then return success responses")
    void givenValidUserIds_whenSendingBulkNotifications_thenReturnSuccessResponses() {
      // Arrange
      List<Long> userIds = List.of(1L, 2L);
      NotificationType type = NotificationType.PROMOTIONAL_OFFER;
      String message = "Special discount available!";
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userRepository.findById(2L)).thenReturn(Optional.of(anotherUser));

      // Act
      List<NotificationResponse> responses = notificationService.sendBulkNotifications(userIds,
          type, message);

      // Assert
      assertNotNull(responses);
      assertEquals(2, responses.size());
      assertTrue(responses.get(0).isSuccess());
      assertTrue(responses.get(1).isSuccess());
      assertEquals("Notification sent successfully", responses.get(0).getMessage());
      assertEquals("Notification sent successfully", responses.get(1).getMessage());
    }

    @Test
    @DisplayName("Given null user IDs list, when sending bulk notifications, "
        + "then throw NotificationException")
    void givenNullUserIdsList_whenSendingBulkNotifications_thenThrowNotificationException() {
      // Arrange
      List<Long> userIds = null;
      NotificationType type = NotificationType.PROMOTIONAL_OFFER;
      String message = "Test message";

      // Act & Assert
      NotificationException exception = assertThrows(NotificationException.class,
          () -> notificationService.sendBulkNotifications(userIds, type, message));
      assertEquals("User IDs list cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Given empty user IDs list, when sending bulk notifications, "
        + "then throw NotificationException")
    void givenEmptyUserIdsList_whenSendingBulkNotifications_thenThrowNotificationException() {
      // Arrange
      List<Long> userIds = List.of();
      NotificationType type = NotificationType.PROMOTIONAL_OFFER;
      String message = "Test message";

      // Act & Assert
      NotificationException exception = assertThrows(NotificationException.class,
          () -> notificationService.sendBulkNotifications(userIds, type, message));
      assertEquals("User IDs list cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Given mixed valid and invalid user IDs, when sending bulk notifications, "
        + "then return mixed responses")
    void givenMixedValidAndInvalidUserIds_whenSendingBulkNotifications_thenReturnMixedResponses() {
      // Arrange
      List<Long> userIds = List.of(1L, 999L);
      NotificationType type = NotificationType.PROMOTIONAL_OFFER;
      String message = "Test message";
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      // Act
      List<NotificationResponse> responses = notificationService.sendBulkNotifications(userIds,
          type, message);

      // Assert
      assertNotNull(responses);
      assertEquals(2, responses.size());
      assertTrue(responses.get(0).isSuccess());
      assertFalse(responses.get(1).isSuccess());
      assertEquals("Notification sent successfully", responses.get(0).getMessage());
      assertTrue(responses.get(1).getMessage().contains("Failed to send notification"));
    }
  }

  @Nested
  @DisplayName("Send Order Confirmation Tests")
  class SendOrderConfirmationTests {

    @Test
    @DisplayName("Given valid order data, when sending order confirmation, "
        + "then return success response")
    void givenValidOrderData_whenSendingOrderConfirmation_thenReturnSuccessResponse() {
      // Arrange
      Long userId = 1L;
      Long orderId = 12345L;
      String totalAmount = "$99.99";
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      // Act
      NotificationResponse response = notificationService.sendOrderConfirmation(userId, orderId,
          totalAmount);

      // Assert
      assertNotNull(response);
      assertTrue(response.isSuccess());
      assertEquals("Notification sent successfully", response.getMessage());
      assertNotNull(response.getNotificationId());
    }

    @Test
    @DisplayName("Given non-existent user, when sending order confirmation, "
        + "then throw NotificationException")
    void givenNonExistentUser_whenSendingOrderConfirmation_thenThrowNotificationException() {
      // Arrange
      Long userId = 999L;
      Long orderId = 12345L;
      String totalAmount = "$99.99";
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(NotificationException.class,
          () -> notificationService.sendOrderConfirmation(userId, orderId, totalAmount));
    }
  }

  @Nested
  @DisplayName("Send Discount Expiry Reminder Tests")
  class SendDiscountExpiryReminderTests {

    @Test
    @DisplayName("Given valid discount data, when sending expiry reminder, "
        + "then return success response")
    void givenValidDiscountData_whenSendingExpiryReminder_thenReturnSuccessResponse() {
      // Arrange
      Long userId = 1L;
      String discountCode = "SAVE20";
      String expiryDate = "2024-12-31";
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      // Act
      NotificationResponse response = notificationService.sendDiscountExpiryReminder(userId,
          discountCode, expiryDate);

      // Assert
      assertNotNull(response);
      assertTrue(response.isSuccess());
      assertEquals("Notification sent successfully", response.getMessage());
      assertNotNull(response.getNotificationId());
    }

    @Test
    @DisplayName("Given non-existent user, when sending discount reminder, "
        + "then throw NotificationException")
    void givenNonExistentUser_whenSendingDiscountReminder_thenThrowNotificationException() {
      // Arrange
      Long userId = 999L;
      String discountCode = "SAVE20";
      String expiryDate = "2024-12-31";
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(NotificationException.class,
          () -> notificationService.sendDiscountExpiryReminder(userId, discountCode, expiryDate));
    }
  }

  @Nested
  @DisplayName("Are Notifications Enabled Tests")
  class AreNotificationsEnabledTests {

    @Test
    @DisplayName("Given valid user ID, when checking notifications enabled, then return true")
    void givenValidUserId_whenCheckingNotificationsEnabled_thenReturnTrue() {
      // Arrange
      Long userId = 1L;
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      // Act
      boolean enabled = notificationService.areNotificationsEnabled(userId);

      // Assert
      assertTrue(enabled);
    }

    @Test
    @DisplayName("Given non-existent user ID, when checking notifications enabled, "
        + "then throw NotificationException")
    void givenNonExistentUserId_whenCheckingNotificationsEnabled_thenThrowNotificationException() {
      // Arrange
      Long userId = 999L;
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(NotificationException.class,
          () -> notificationService.areNotificationsEnabled(userId));
    }
  }
}
