package com.rbkmoney.webhook.dispatcher.repository;

import com.rbkmoney.webhook.dispatcher.entity.CommitLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitLogRepository extends JpaRepository<CommitLogEntity, String> {
}