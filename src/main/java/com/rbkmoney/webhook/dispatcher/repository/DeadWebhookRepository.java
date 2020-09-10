package com.rbkmoney.webhook.dispatcher.repository;

import com.rbkmoney.webhook.dispatcher.entity.DeadWebhookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeadWebhookRepository extends JpaRepository<DeadWebhookEntity, String> {

    Optional<DeadWebhookEntity> findByWebhookIdAndSourceIdAndEventId(long webhookId, String sourceId, long eventId);
}