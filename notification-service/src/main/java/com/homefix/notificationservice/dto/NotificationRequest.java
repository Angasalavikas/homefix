package com.homefix.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Message is required")
    private String message;
}
