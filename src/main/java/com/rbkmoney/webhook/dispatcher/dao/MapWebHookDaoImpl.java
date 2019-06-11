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
        if (webHook.source_id == null) {
            throw new NullPointerException();
        }
        map.put(webHook.source_id + webHook.event_id, true);
    }

    @Override
    public boolean isCommitParent(Webhook webHook) {
        return webHook.event_id == 0 || map.containsKey(webHook.source_id + webHook.parent_event_id);
    }

}
