package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.dao.WebHookDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Instant;

public class DeadRetryDispatchFilterTest {

    @Mock
    WebHookDao webHookDao;

    DispatchFilter dispatchFilter;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        dispatchFilter = new DeadRetryDispatchFilter(webHookDao);
    }

    @Test
    public void filter() {
        WebhookMessage webhookMessage = new WebhookMessage();
        webhookMessage.setCreatedAt(Instant.now().minusSeconds(60 * 60 * 24 + 1).toString());
        Mockito.when(webHookDao.isCommitted(webhookMessage)).thenReturn(true);
        Boolean filter = dispatchFilter.filter(webhookMessage);

        Assert.assertFalse(filter);
    }
}