package com.rbkmoney.webhook.dispatcher.converter;

import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.entity.CommitLogEntity;
import com.rbkmoney.webhook.dispatcher.utils.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CommitLogConverter {

    public CommitLogEntity convert(WebhookMessage webhookMessage) {
        return CommitLogEntity.builder()
                .id(IdGenerator.generate(
                        webhookMessage.getWebhookId(),
                        webhookMessage.getSourceId(),
                        webhookMessage.getEventId()))
                .creationTime(TypeUtil.toLocalDateTime(Instant.now()))
                .build();
    }
}
