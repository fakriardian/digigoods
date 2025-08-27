package com.example.digigoods.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification response.
 * Contains the result of a notification sending operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

  private String notificationId;

  private String message;

  @JsonSerialize(using = LocalDateTimeSerializer.class)
  private LocalDateTime timestamp;

  private boolean success;

}
