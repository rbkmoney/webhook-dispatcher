package com.rbkmoney.webhook.dispatcher.repository;

import com.rbkmoney.webhook.dispatcher.entity.DeadWebhookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadWebhookRepository extends JpaRepository<DeadWebhookEntity, String> {
}