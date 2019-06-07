package com.rbkmoney.webhook.dispatcher.dao;

import com.rbkmoney.webhook.dispatcher.Webhook;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MapWebHookDaoImpl implements WebHookDao {

    private Map<String, Boolean> map = new ConcurrentHashMap<>();

    @Override
    public void commit(Webhook webHook) {
        map.put(generateKey(webHook), true);
    }

    @Override
    public boolean isCommitParent(Webhook webHook) {
        return map.get(generateKey(webHook));
    }

    private String generateKey(Webhook webHook) {
        return webHook.source_id + webHook.event_id;
    }
}
