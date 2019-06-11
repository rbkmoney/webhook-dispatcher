package com.rbkmoney.webhook.dispatcher.dao;

import com.rbkmoney.webhook.dispatcher.Webhook;
import org.junit.Assert;
import org.junit.Test;

public class MapWebHookDaoImplTest {

    public static final String SOURCE_ID = "1";
    private WebHookDao webHookDao = new MapWebHookDaoImpl();

    @Test
    public void commit() {
        Webhook webHook = new Webhook();
        webHook.setSourceId(SOURCE_ID);
        int firstEvent = 0;
        webHook.setEventId(firstEvent);
        webHookDao.commit(webHook);

        webHook = new Webhook();
        webHook.setEventId(1);
        webHook.setSourceId(SOURCE_ID);
        webHook.setParentEventId(firstEvent);

        Assert.assertTrue(webHookDao.isCommitParent(webHook));
    }
}