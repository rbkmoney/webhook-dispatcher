package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DeadRetryDispatchFilter implements DispatchFilter {

    private int deadRetryTimeout;

    public DeadRetryDispatchFilter(@Value("${retry.dead.time.hours}") int deadRetryTimeout) {
        this.deadRetryTimeout = deadRetryTimeout;
    }

    @Override
    public Boolean filter(Webhook webhook) {
        return Instant.now().toEpochMilli() - Instant.parse(webhook.getCreatedAt()).toEpochMilli() < deadRetryTimeout * 1000 * 60 * 60;
    }
}
