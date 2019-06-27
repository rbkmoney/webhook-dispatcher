package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.Webhook;
import com.rbkmoney.webhook.dispatcher.utils.TimeoutUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
public class DeadRetryDispatchFilter implements DispatchFilter {

    private int deadRetryTimeout;

    public DeadRetryDispatchFilter(@Value("${retry.dead.time.hours}") int deadRetryTimeout) {
        this.deadRetryTimeout = deadRetryTimeout;
    }

    @Override
    public Boolean filter(Webhook webhook) {
        return TimeoutUtils.calculateTimeFromCreated(webhook.getCreatedAt()) < TimeUnit.HOURS.toHours(deadRetryTimeout);
    }
}
