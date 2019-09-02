package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.utils.TimeoutUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TimeDispatchFilter {

    public Boolean filter(WebhookMessage webhookMessage, long timeout) {
        return TimeoutUtils.calculateTimeFromCreated(webhookMessage.getCreatedAt()) > TimeUnit.SECONDS.toMillis(timeout);
    }

}
