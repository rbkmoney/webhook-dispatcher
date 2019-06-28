package com.rbkmoney.webhook.dispatcher.dao;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.config.RiakConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = {WebHookDaoImpl.class, RiakConfig.class})
public class WebHookDaoImplTest extends AbstractRiakIntegrationTest {

    @Value("${riak.bucket}")
    private String BUCKET_NAME;

    @Autowired
    private WebHookDao webHookDao;

    @Test
    public void riakTest() throws InterruptedException {
        Thread.sleep(20000L);
        WebhookMessage webHook = new WebhookMessage();
        webHook.setSourceId("test");
        webHook.setEventId(1);
        webHook.setUrl("/test");
        webHookDao.commit(webHook);

        webHook.setParentEventId(1);

        boolean commitParent = webHookDao.isParentCommitted(webHook);

        Assert.assertTrue(commitParent);
    }

}