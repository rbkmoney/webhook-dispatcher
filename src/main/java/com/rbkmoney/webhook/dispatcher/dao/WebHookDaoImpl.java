package com.rbkmoney.webhook.dispatcher.dao;

import com.rbkmoney.kafka.common.exception.RetryableException;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.converter.WebhookMessageConverter;
import com.rbkmoney.webhook.dispatcher.utils.KeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Slf4j
@Service
public class WebHookDaoImpl extends NamedParameterJdbcDaoSupport implements WebHookDao {

    private static final String ID = "id";

    private final WebhookMessageConverter webhookMessageConverter;

    public WebHookDaoImpl(
            DataSource ds,
            WebhookMessageConverter webhookMessageConverter) {
        setDataSource(ds);
        this.webhookMessageConverter = webhookMessageConverter;
    }

    @Override
    public void commit(WebhookMessage webhookMessage) {
        String key = KeyGenerator.generateKey(webhookMessage.getWebhookId(), webhookMessage.getSourceId(), webhookMessage.getEventId());

        try {
            log.info("Commit webhook with key={}", key);
            String sqlQuery = "INSERT INTO wb_dispatch.commit_log(id) VALUES (?)";
            getJdbcTemplate().update(sqlQuery, key);
        } catch (Exception e) {
            log.error("Exception during committing webhook with key={}", key, e);
            throw new RetryableException(e);
        }
    }

    @Override
    public void bury(WebhookMessage webhookMessage) {
        String key = KeyGenerator.generateKey(webhookMessage.getWebhookId(), webhookMessage.getSourceId(), webhookMessage.getEventId());

        try {
            log.info("Bury webhook with key={}", key);
            String sqlQuery = "INSERT INTO wb_dispatch.dead_hooks(id, webhook_id, source_id, event_id, parent_event_id, " +
                    "created_at, url, content_type, additional_headers, request_body, retry_count) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            MapSqlParameterSource params = webhookMessageConverter.convert(webhookMessage, key);
            getNamedParameterJdbcTemplate().update(sqlQuery, params);
        } catch (Exception e) {
            log.error("Exception during burying webhook with key={}", key, e);
            throw new RetryableException(e);
        }
    }

    @Override
    public Boolean isParentCommitted(WebhookMessage webhookMessage) {
        String key = KeyGenerator.generateKey(webhookMessage.getWebhookId(), webhookMessage.getSourceId(), webhookMessage.getParentEventId());
        return checkIsCommit(webhookMessage, key);
    }

    @Override
    public Boolean isCommitted(WebhookMessage webhookMessage) {
        String key = KeyGenerator.generateKey(webhookMessage.getWebhookId(), webhookMessage.getSourceId(), webhookMessage.getEventId());
        return checkIsCommit(webhookMessage, key);
    }

    private Boolean checkIsCommit(WebhookMessage webhookMessage, String key) {
        try {
            String sqlQuery = "SELECT EXISTS (" +
                    "SELECT * FROM wb_dispatch.commit_log WHERE id = :id" +
                    ")";
            MapSqlParameterSource params = new MapSqlParameterSource(ID, key);
            Boolean isExist = getNamedParameterJdbcTemplate().queryForObject(sqlQuery, params, Boolean.class);
            log.info("Row for source_id={}, hook_id={} with key={} is already exists: {}",
                    webhookMessage.getSourceId(), webhookMessage.getWebhookId(), key, isExist);
            return isExist;
        } catch (Exception e) {
            log.error("Exception during looking for parent event with key={}", key, e);
            throw new RetryableException(e);
        }
    }
}
