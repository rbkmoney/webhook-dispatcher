package com.rbkmoney.webhook.dispatcher.service;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.WebhookNotFound;
import com.rbkmoney.webhook.dispatcher.converter.DeadWebhookConverter;
import com.rbkmoney.webhook.dispatcher.entity.DeadWebhookEntity;
import com.rbkmoney.webhook.dispatcher.repository.DeadWebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookMessageService {

    @Value("${kafka.topic.webhook.forward}")
    private String forwardTopic;

    private final DeadWebhookRepository deadWebhookRepository;
    private final DeadWebhookConverter deadWebhookConverter;
    private final KafkaTemplate<String, WebhookMessage> kafkaTemplate;

    public void resend(
            long webhookId,
            String sourceId,
            long eventId) throws WebhookNotFound {
        Optional<DeadWebhookEntity> webhook = deadWebhookRepository.findByWebhookIdAndSourceIdAndEventId(
                webhookId,
                sourceId,
                eventId);

        if (webhook.isEmpty()) {
            log.warn("No dead webhook was found for webhookId={}, sourceId={} and eventId={}",
                    webhookId, sourceId, eventId);
            throw new WebhookNotFound();
        }

        WebhookMessage webhookMessage = deadWebhookConverter.toDomain(webhook.get());

        log.info("Resending webhook with webhookId={}, sourceId={} and eventId={} back to 'forward' topic",
                webhookId, sourceId, eventId);
        kafkaTemplate.send(forwardTopic, webhookMessage.getSourceId(), webhookMessage);
    }
}
