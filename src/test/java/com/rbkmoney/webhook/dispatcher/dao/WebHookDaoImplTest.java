package com.rbkmoney.webhook.dispatcher.dao;

import com.rbkmoney.webhook.dispatcher.WebhookDispatcherApplication;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.repository.DeadHookRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebhookDispatcherApplication.class)
public class WebHookDaoImplTest extends DaoTestBase {

    @Autowired
    private WebHookDao webHookDao;

    @Autowired
    private DeadHookRepository deadHookRepository;

    @Test
    public void shouldCommitWebHooks() {
        // Given
        WebhookMessage webHook = new WebhookMessage();
        webHook.setSourceId("test");
        webHook.setEventId(1);
        webHook.setUrl("/test");
        webHook.setParentEventId(-1);

        // When
        webHookDao.commit(webHook);

        // Then
        webHook.setParentEventId(1);
        boolean commitParent = webHookDao.isParentCommitted(webHook);
        assertTrue(commitParent);

        boolean commit = webHookDao.isCommitted(webHook);
        assertTrue(commit);

        webHook.setEventId(666L);
        assertFalse(webHookDao.isCommitted(webHook));
    }

    @Test
    public void shouldBuryWebHooks() {
        // Given
        WebhookMessage webHook = new WebhookMessage();
        webHook.setWebhookId(0L);
        webHook.setSourceId("source");
        webHook.setEventId(1L);
        webHook.setParentEventId(2L);
        webHook.setCreatedAt("2016-03-22T06:12:27Z");
        webHook.setUrl("/url");
        webHook.setContentType("contentType");
        webHook.setAdditionalHeaders(Map.of("a", "b"));
        webHook.setRequestBody("body".getBytes());
        webHook.setRetryCount(3L);

        // When
        webHookDao.bury(webHook);

        // Then
        assertTrue(deadHookRepository.findById("0_source_1").isPresent());
    }
}