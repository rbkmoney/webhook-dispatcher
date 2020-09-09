package com.rbkmoney.webhook.dispatcher.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebhookMessageConverter {

    private final ObjectMapper objectMapper;

    public MapSqlParameterSource convert(WebhookMessage message, String key) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("id", key);
        paramsMap.put("webhook_id", message.getWebhookId());
        paramsMap.put("source_id", message.getSourceId());
        paramsMap.put("event_id", message.getEventId());
        paramsMap.put("parent_event_id", message.getParentEventId());
        paramsMap.put("created_at", TypeUtil.stringToLocalDateTime(message.getCreatedAt()));
        paramsMap.put("url", message.getUrl());
        paramsMap.put("content_type", message.getContentType());
        paramsMap.put("additional_headers", toJson(message.getAdditionalHeaders()));
        paramsMap.put("request_body", message.getRequestBody());
        paramsMap.put("retry_count", message.getRetryCount());

        return new MapSqlParameterSource(paramsMap);
    }

    @SneakyThrows
    private String toJson(Map<String, String> map) {
        return objectMapper.writeValueAsString(map);
    }
}
