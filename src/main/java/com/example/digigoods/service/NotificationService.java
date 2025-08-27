package com.example.digigoods.service;

import com.example.digigoods.dto.NotificationResponse;
import com.example.digigoods.exception.NotificationException;
import com.example.digigoods.model.NotificationType;
import com.example.digigoods.model.User;
import com.example.digigoods.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Service for notification operations.
 * Handles sending various types of notifications to users including order
 * confirmations,
 * discount alerts, and system notifications.
 */
@Service
public class NotificationService {

  private final UserRepository userRepository;

  public NotificationService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Send a notification to a specific user.
   *
   * @param userId  the ID of the user to notify
   * @param type    the type of notification
   * @param message the notification message
   * @return notification response with status
   * @throws NotificationException if user not found or notification fails
   */
  public NotificationResponse sendNotification(Long userId, NotificationType type, String message) {
    // Arrange - validate input parameters
    validateNotificationInput(userId, type, message);

    // Act - find user and send notification
    User user = findUserById(userId);
    String notificationId = generateNotificationId();

    // Simulate sending notification (in real implementation, this would integrate
    // with email/SMS)
    boolean success = processNotification(user, type, message, notificationId);

    // Assert - return appropriate response
    if (success) {
      return new NotificationResponse(
          notificationId,
          "Notification sent successfully",
          LocalDateTime.now(),
          true);
    } else {
      throw new NotificationException("Failed to send notification to user: " + userId);
    }
  }

  /**
   * Send bulk notifications to multiple users.
   *
   * @param userIds the list of user IDs to notify
   * @param type    the type of notification
   * @param message the notification message
   * @return list of notification responses
   * @throws NotificationException if any notification fails
   */
  public List<NotificationResponse> sendBulkNotifications(List<Long> userIds,
      NotificationType type,
      String message) {
    // Arrange - validate input
    if (userIds == null || userIds.isEmpty()) {
      throw new NotificationException("User IDs list cannot be null or empty");
    }

    // Act - send notifications to all users
    return userIds.stream()
        .map(userId -> {
          try {
            return sendNotification(userId, type, message);
          } catch (NotificationException e) {
            // Log error but continue with other notifications
            return new NotificationResponse(
                null,
                "Failed to send notification: " + e.getMessage(),
                LocalDateTime.now(),
                false);
          }
        })
        .toList();
  }

  /**
   * Send order confirmation notification.
   *
   * @param userId      the user ID
   * @param orderId     the order ID
   * @param totalAmount the total order amount
   * @return notification response
   * @throws NotificationException if notification fails
   */
  public NotificationResponse sendOrderConfirmation(Long userId, Long orderId, String totalAmount) {
    String message = String.format(
        "Your order #%d has been confirmed! Total amount: %s. Thank you for your purchase!",
        orderId, totalAmount);
    return sendNotification(userId, NotificationType.ORDER_CONFIRMATION, message);
  }

  /**
   * Send discount expiry reminder notification.
   *
   * @param userId       the user ID
   * @param discountCode the discount code
   * @param expiryDate   the expiry date
   * @return notification response
   * @throws NotificationException if notification fails
   */
  public NotificationResponse sendDiscountExpiryReminder(Long userId, String discountCode,
      String expiryDate) {
    String message = String.format(
        "Reminder: Your discount code '%s' expires on %s. Use it before it's too late!",
        discountCode, expiryDate);
    return sendNotification(userId, NotificationType.DISCOUNT_REMINDER, message);
  }

  /**
   * Check if notifications are enabled for a user.
   *
   * @param userId the user ID
   * @return true if notifications are enabled, false otherwise
   * @throws NotificationException if user not found
   */
  public boolean areNotificationsEnabled(Long userId) {
    findUserById(userId); // Validate user exists
    // In a real implementation, this would check user preferences
    // For now, assume notifications are always enabled
    return true;
  }

  private void validateNotificationInput(Long userId, NotificationType type, String message) {
    if (userId == null) {
      throw new NotificationException("User ID cannot be null");
    }
    if (type == null) {
      throw new NotificationException("Notification type cannot be null");
    }
    if (message == null || message.trim().isEmpty()) {
      throw new NotificationException("Notification message cannot be null or empty");
    }
  }

  private User findUserById(Long userId) {
    Optional<User> userOptional = userRepository.findById(userId);
    if (userOptional.isEmpty()) {
      throw new NotificationException("User not found with ID: " + userId);
    }
    return userOptional.get();
  }

  private String generateNotificationId() {
    // Simple notification ID generation (in real implementation, use UUID or
    // similar)
    return "NOTIF_" + System.currentTimeMillis();
  }

  private boolean processNotification(User user, NotificationType type, String message,
      String notificationId) {
    // Simulate notification processing
    // In real implementation, this would integrate with email service, SMS service,
    // etc.

    // Log the notification (in real implementation, save to database)
    System.out.printf("Sending %s notification to user %s: %s (ID: %s)%n",
        type, user.getUsername(), message, notificationId);

    // Simulate success (in real implementation, handle actual sending logic)
    return true;
  }
}
