package com.rbkmoney.webhook.dispatcher.dao;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {WebHookDaoImpl.class, FlywayAutoConfiguration.class})
public class WebHookDaoImplTest extends DaoTestBase {

    @Autowired
    private WebHookDao webHookDao;

    @Test
    public void pgTest() throws InterruptedException {
        Thread.sleep(20000L);
        WebhookMessage webHook = new WebhookMessage();
        webHook.setSourceId("test");
        webHook.setEventId(1);
        webHook.setUrl("/test");
        webHook.setParentEventId(-1);
        webHookDao.commit(webHook);

        webHook.setParentEventId(1);

        boolean commitParent = webHookDao.isParentCommitted(webHook);

        Assert.assertTrue(commitParent);

        boolean commit = webHookDao.isCommitted(webHook);

        Assert.assertTrue(commit);

        webHook.setEventId(666L);
        Assert.assertFalse(webHookDao.isCommitted(webHook));

    }

}