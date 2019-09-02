package com.rbkmoney.webhook.dispatcher.dao;

import com.rbkmoney.kafka.common.exception.RetryableException;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.utils.KeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Slf4j
@Service
public class WebHookDaoPgImpl extends NamedParameterJdbcDaoSupport implements WebHookDao {

    public WebHookDaoPgImpl(DataSource ds) {
        setDataSource(ds);
    }

    @Override
    public void commit(WebhookMessage webhookMessage) {
        try {
            String key = KeyGenerator.generateKey(webhookMessage.getWebhookId(), webhookMessage.getSourceId(), webhookMessage.getEventId());
            log.info("WebHookDaoImpl commit key: {} webHook: {}", key, webhookMessage);
            String sqlQuery = "insert into wb_dispatch.commit_log(id) values (?)";
            getJdbcTemplate().update(sqlQuery, key);
        } catch (Exception e) {
            log.error("Exception in WebHookDao when commit e: ", e);
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
            log.info("WebHookDaoImpl is commit key: {} webHook: {}", key, webhookMessage);
            String sqlQuery = "SELECT EXISTS (" +
                    "select * from wb_dispatch.commit_log where id = :id" +
                    ")";
            MapSqlParameterSource params = new MapSqlParameterSource("id", key);
            Boolean isExist = getNamedParameterJdbcTemplate()
                    .queryForObject(sqlQuery, params, Boolean.class);
            log.info("Row for {} with key: {} is exist: {}", webhookMessage, key, isExist);
            return isExist;
        } catch (Exception e) {
            log.error("Exception when find parent event ", e);
            throw new RetryableException(e);
        }
    }
}
