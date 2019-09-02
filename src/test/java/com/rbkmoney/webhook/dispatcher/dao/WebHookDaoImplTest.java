package com.rbkmoney.webhook.dispatcher.dao;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {WebHookDaoPgImpl.class})
public class WebHookDaoImplTest extends DaoTestBase {

    @Value("${riak.bucket}")
    private String BUCKET_NAME;

    @Autowired
    private WebHookDao webHookDaoPgImpl;

    @Test
    public void riakTest() throws InterruptedException {
        Thread.sleep(20000L);
        WebhookMessage webHook = new WebhookMessage();
        webHook.setSourceId("test");
        webHook.setEventId(1);
        webHook.setUrl("/test");
        webHook.setParentEventId(-1);
        webHookDaoPgImpl.commit(webHook);

        webHook.setParentEventId(1);

        boolean commitParent = webHookDaoPgImpl.isParentCommitted(webHook);

        Assert.assertTrue(commitParent);

        boolean commit = webHookDaoPgImpl.isCommitted(webHook);

        Assert.assertTrue(commit);

        webHook.setEventId(666L);
        Assert.assertFalse(webHookDaoPgImpl.isCommitted(webHook));

    }

}