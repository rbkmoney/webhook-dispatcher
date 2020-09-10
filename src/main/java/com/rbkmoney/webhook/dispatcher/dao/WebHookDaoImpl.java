package com.rbkmoney.webhook.dispatcher.dao;

import com.rbkmoney.kafka.common.exception.RetryableException;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.converter.CommitLogConverter;
import com.rbkmoney.webhook.dispatcher.converter.DeadHookConverter;
import com.rbkmoney.webhook.dispatcher.entity.CommitLogEntity;
import com.rbkmoney.webhook.dispatcher.entity.DeadHookEntity;
import com.rbkmoney.webhook.dispatcher.repository.CommitLogRepository;
import com.rbkmoney.webhook.dispatcher.repository.DeadHookRepository;
import com.rbkmoney.webhook.dispatcher.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebHookDaoImpl implements WebHookDao {

    private final DeadHookConverter deadHookConverter;
    private final DeadHookRepository deadHookRepository;
    private final CommitLogConverter commitLogConverter;
    private final CommitLogRepository commitLogRepository;

    @Override
    public void commit(WebhookMessage webhookMessage) {
        CommitLogEntity commitLog = commitLogConverter.convert(webhookMessage);

        try {
            log.info("Commit webhook with id={}", commitLog.getId());
            commitLogRepository.save(commitLog);
        } catch (Exception e) {
            log.error("Exception during committing webhook with id={}", commitLog.getId(), e);
            throw new RetryableException(e);
        }
    }

    @Override
    public void bury(WebhookMessage webhookMessage) {
        DeadHookEntity deadHook = deadHookConverter.convert(webhookMessage);

        try {
            log.info("Bury webhook with id={}", deadHook.getId());
            deadHookRepository.save(deadHook);
        } catch (Exception e) {
            log.error("Exception during burying webhook with id={}", deadHook.getId(), e);
            throw new RetryableException(e);
        }
    }

    @Override
    public Boolean isParentCommitted(WebhookMessage webhookMessage) {
        return isCommitted(IdGenerator.generate(
                webhookMessage.getWebhookId(),
                webhookMessage.getSourceId(),
                webhookMessage.getParentEventId()));
    }

    @Override
    public Boolean isCommitted(WebhookMessage webhookMessage) {
        return isCommitted(IdGenerator.generate(
                webhookMessage.getWebhookId(),
                webhookMessage.getSourceId(),
                webhookMessage.getEventId()));
    }

    private Boolean isCommitted(String id) {
        try {
            Boolean isCommitted = commitLogRepository.existsById(id);
            log.info("Webhook with id={}: isCommitted={}", id, isCommitted);
            return isCommitted;
        } catch (Exception e) {
            log.error("Exception during looking for parent event with id={}", id, e);
            throw new RetryableException(e);
        }
    }
}
