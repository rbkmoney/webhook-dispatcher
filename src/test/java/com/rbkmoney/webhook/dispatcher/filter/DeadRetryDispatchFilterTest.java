package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.Webhook;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;

public class DeadRetryDispatchFilterTest {

    DispatchFilter dispatchFilter = new DeadRetryDispatchFilter(24);

    @Test
    public void filter() {
        Webhook webhook = new Webhook();
        webhook.setCreatedAt(Instant.now().minusSeconds(60 * 60 * 24 + 1).toString());
        Boolean filter = dispatchFilter.filter(webhook);

        Assert.assertFalse(filter);
    }
}