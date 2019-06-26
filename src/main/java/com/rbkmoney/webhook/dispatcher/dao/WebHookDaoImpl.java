package com.rbkmoney.webhook.dispatcher.dao;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.rbkmoney.webhook.dispatcher.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebHookDaoImpl implements WebHookDao {

    private static final String TEXT_PLAIN = "text/plain";
    private final RiakClient client;

    @Value("${riak.bucket}")
    private String bucket;

    @Override
    public void commit(Webhook webHook) {

    }

    @Override
    public boolean isCommitParent(Webhook webHook) {
        return false;
    }

    private Location createLocation(String bucketName, String key) {
        Namespace quotesBucket = new Namespace(bucketName);
        return new Location(quotesBucket, key);
    }
}
