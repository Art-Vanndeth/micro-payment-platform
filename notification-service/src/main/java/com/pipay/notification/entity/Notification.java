package com.pipay.notification.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Builder
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private String transactionId;
    private String message;
    private String status;
    private Boolean isRead;
    private long timestamp;
    private String type;
    private String recipient;
}
