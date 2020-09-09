package com.rbkmoney.webhook.dispatcher.repository;

import com.rbkmoney.webhook.dispatcher.entity.DeadHookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadHookRepository extends JpaRepository<DeadHookEntity, String> {
}