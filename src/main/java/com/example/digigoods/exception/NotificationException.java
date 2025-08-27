package com.example.digigoods.exception;

/**
 * Exception thrown when notification operations fail.
 * This includes scenarios like user not found, invalid notification parameters,
 * or failures in the notification delivery system.
 */
public class NotificationException extends RuntimeException {

  /**
   * Constructs a new NotificationException with a default message.
   */
  public NotificationException() {
    super("Notification operation failed");
  }

  /**
   * Constructs a new NotificationException with the specified detail message.
   *
   * @param message the detail message explaining the cause of the exception
   */
  public NotificationException(String message) {
    super(message);
  }

  /**
   * Constructs a new NotificationException with the specified detail message and cause.
   *
   * @param message the detail message explaining the cause of the exception
   * @param cause the cause of the exception
   */
  public NotificationException(String message, Throwable cause) {
    super(message, cause);
  }
}
