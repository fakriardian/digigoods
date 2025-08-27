package com.example.digigoods.model;

/**
 * Enumeration representing different types of notifications in the system.
 * Used to categorize notifications for proper handling and routing.
 */
public enum NotificationType {
  
  /**
   * Notification sent when an order is successfully created and confirmed.
   */
  ORDER_CONFIRMATION,
  
  /**
   * Notification sent to remind users about expiring discount codes.
   */
  DISCOUNT_REMINDER,
  
  /**
   * General system notifications for maintenance, updates, etc.
   */
  SYSTEM_NOTIFICATION,
  
  /**
   * Notification sent when a product is back in stock.
   */
  STOCK_ALERT,
  
  /**
   * Notification sent for promotional offers and marketing campaigns.
   */
  PROMOTIONAL_OFFER
}
