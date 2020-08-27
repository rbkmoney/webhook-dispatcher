package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.dao.WebHookDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeadRetryDispatchFilter.class)
public class DeadRetryDispatchFilterTest {

    @MockBean
    private WebHookDao webHookDao;

    @Autowired
    private DeadRetryDispatchFilter dispatchFilter;

    @Test
    public void filter() {
        WebhookMessage webhookMessage = new WebhookMessage();
        webhookMessage.setCreatedAt(Instant.now().minusSeconds(60 * 60 * 24 + 1).toString());
        when(webHookDao.isCommitted(webhookMessage)).thenReturn(true);
        boolean filter = dispatchFilter.filter(webhookMessage);

        assertTrue(filter);

        webhookMessage = new WebhookMessage();
        webhookMessage.setCreatedAt(Instant.now().minusSeconds(60 * 60 * 24 + 1).toString());
        when(webHookDao.isCommitted(webhookMessage)).thenReturn(false);
        filter = dispatchFilter.filter(webhookMessage);

        assertTrue(filter);

        webhookMessage = new WebhookMessage();
        webhookMessage.setCreatedAt(Instant.now().minusSeconds(60 * 60 * 23).toString());
        when(webHookDao.isCommitted(webhookMessage)).thenReturn(false);
        filter = dispatchFilter.filter(webhookMessage);

        assertFalse(filter);
    }
}