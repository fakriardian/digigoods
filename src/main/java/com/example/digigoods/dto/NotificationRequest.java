package com.example.digigoods.dto;

import com.example.digigoods.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification request.
 * Contains all necessary information to send a notification to a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

  @NotNull(message = "User ID is required")
  private Long userId;

  @NotNull(message = "Notification type is required")
  private NotificationType type;

  @NotBlank(message = "Message cannot be blank")
  private String message;

}
