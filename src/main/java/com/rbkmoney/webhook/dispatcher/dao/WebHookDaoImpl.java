package com.rbkmoney.webhook.dispatcher.dao;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.cap.Quorum;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.basho.riak.client.core.util.BinaryValue;
import com.rbkmoney.webhook.dispatcher.Webhook;
import com.rbkmoney.webhook.dispatcher.exception.RiakExecutionException;
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
        try {
            log.debug("WebHookDaoImpl create in bucket: {} webHook: {}", bucket, webHook);
            RiakObject quoteObject = new RiakObject()
                    .setContentType(TEXT_PLAIN)
                    .setValue(BinaryValue.create(webHook.url));
            Location quoteObjectLocation = createLocation(bucket, webHook.getSourceId() + webHook.getEventId());
            StoreValue storeOp = new StoreValue.Builder(quoteObject)
                    .withOption(StoreValue.Option.W, Quorum.oneQuorum())
                    .withLocation(quoteObjectLocation)
                    .build();
            client.execute(storeOp);
        } catch (InterruptedException e) {
            log.error("InterruptedException in ListRepository when create e: ", e);
            Thread.currentThread().interrupt();
            throw new RiakExecutionException(e);
        } catch (Exception e) {
            log.error("Exception in ListRepository when create e: ", e);
            throw new RiakExecutionException(e);
        }
    }

    private String generateKey(Webhook webHook) {
        return webHook.getSourceId() + webHook.getParentEventId();
    }

    @Override
    public boolean isParentCommitted(Webhook webHook) {
        try {
            log.debug("WebHookDaoImpl get bucket: {} key: {}", bucket, generateKey(webHook));
            Location quoteObjectLocation = createLocation(bucket, generateKey(webHook));
            FetchValue fetch = new FetchValue.Builder(quoteObjectLocation)
                    .withOption(FetchValue.Option.R, new Quorum(3))
                    .build();
            FetchValue.Response response = client.execute(fetch);
            RiakObject obj = response.getValue(RiakObject.class);
            return obj != null && obj.getValue() != null;
        } catch (InterruptedException e) {
            log.error("InterruptedException in WebHookDaoImpl when get e: ", e);
            Thread.currentThread().interrupt();
            throw new RiakExecutionException(e);
        } catch (Exception e) {
            log.error("Exception in WebHookDaoImpl when get e: ", e);
            throw new RiakExecutionException(e);
        }
    }

    private Location createLocation(String bucketName, String key) {
        Namespace quotesBucket = new Namespace(bucketName);
        return new Location(quotesBucket, key);
    }
}
