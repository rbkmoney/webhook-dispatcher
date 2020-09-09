package com.rbkmoney.webhook.dispatcher.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dead_hooks")
public class DeadHookEntity implements Serializable {

    @Id
    private String id;

    private Long webhookId;
    private String sourceId;
    private Long eventId;
    private Long parentEventId;
    private LocalDateTime createdAt;
    private String url;
    private String contentType;
    private String additionalHeaders;
    private Byte[] requestBody;
    private Long retryCount;
}

