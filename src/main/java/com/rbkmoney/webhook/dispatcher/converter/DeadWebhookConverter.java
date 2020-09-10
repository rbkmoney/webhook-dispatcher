package com.rbkmoney.webhook.dispatcher.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.entity.DeadWebhookEntity;
import com.rbkmoney.webhook.dispatcher.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeadWebhookConverter {

    private final ObjectMapper objectMapper;

    public DeadWebhookEntity convert(WebhookMessage webhookMessage) {
        return DeadWebhookEntity.builder()
                .id(IdGenerator.generate(
                        webhookMessage.getWebhookId(),
                        webhookMessage.getSourceId(),
                        webhookMessage.getEventId()))
                .webhookId(webhookMessage.getWebhookId())
                .sourceId(webhookMessage.getSourceId())
                .eventId(webhookMessage.getEventId())
                .parentEventId(webhookMessage.getParentEventId())
                .createdAt(TypeUtil.stringToLocalDateTime(webhookMessage.getCreatedAt()))
                .url(webhookMessage.getUrl())
                .contentType(webhookMessage.getContentType())
                .additionalHeaders(toJson(webhookMessage.getAdditionalHeaders()))
                .requestBody(webhookMessage.getRequestBody())
                .retryCount(webhookMessage.getRetryCount())
                .build();
    }

    @SneakyThrows
    private String toJson(Map<String, String> map) {
        return objectMapper.writeValueAsString(map);
    }
}
