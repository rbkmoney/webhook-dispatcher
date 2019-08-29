package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.WebhookDispatcherApplication;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.dao.WebHookDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeadRetryDispatchFilter.class)
public class DeadRetryDispatchFilterTest {

    @MockBean
    WebHookDao webHookDao;

    @Autowired
    DeadRetryDispatchFilter dispatchFilter;

    @Test
    public void filter() {
        WebhookMessage webhookMessage = new WebhookMessage();
        webhookMessage.setCreatedAt(Instant.now().minusSeconds(60 * 60 * 24 + 1).toString());
        Mockito.when(webHookDao.isCommitted(webhookMessage)).thenReturn(true);
        Boolean filter = dispatchFilter.filter(webhookMessage);

        Assert.assertTrue(filter);

        webhookMessage = new WebhookMessage();
        webhookMessage.setCreatedAt(Instant.now().minusSeconds(60 * 60 * 24 + 1).toString());
        Mockito.when(webHookDao.isCommitted(webhookMessage)).thenReturn(false);
        filter = dispatchFilter.filter(webhookMessage);

        Assert.assertTrue(filter);

        webhookMessage = new WebhookMessage();
        webhookMessage.setCreatedAt(Instant.now().minusSeconds(60 * 60 * 23).toString());
        Mockito.when(webHookDao.isCommitted(webhookMessage)).thenReturn(false);
        filter = dispatchFilter.filter(webhookMessage);

        Assert.assertFalse(filter);
    }
}