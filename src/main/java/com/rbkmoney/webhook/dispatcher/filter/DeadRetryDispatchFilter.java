package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.dao.WebHookDao;
import com.rbkmoney.webhook.dispatcher.utils.TimeoutUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DeadRetryDispatchFilter implements DispatchFilter {

    private final WebHookDao webHookDao;

    @Value("${retry.dead.time.hours:24}")
    private int deadRetryTimeout;

    @Override
    public boolean filter(WebhookMessage webhookMessage) {
        return TimeoutUtils.calculateTimeFromCreated(webhookMessage.getCreatedAt()) > TimeUnit.HOURS.toMillis(deadRetryTimeout)
                || webHookDao.isCommitted(webhookMessage);
    }
}
