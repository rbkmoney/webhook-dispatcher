package com.rbkmoney.webhook.dispatcher.dao;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.cap.Quorum;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.basho.riak.client.core.util.BinaryValue;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.exception.RiakExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebHookDaoImpl implements WebHookDao {

    public static final String DELIMETER = "_";
    private final RiakClient client;

    @Value("${riak.bucket}")
    private String bucket;

    @Override
    public void commit(WebhookMessage webhookMessage) {
        try {
            log.debug("WebHookDaoImpl create in bucket: {} webHook: {}", bucket, webhookMessage);
            RiakObject quoteObject = new RiakObject()
                    .setContentType(MediaType.TEXT_PLAIN_VALUE)
                    .setValue(BinaryValue.create(webhookMessage.url));
            String key = generateKey(webhookMessage.getWebhookId(), webhookMessage.getSourceId(), webhookMessage.getEventId());
            Location quoteObjectLocation = createLocation(bucket, key);
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

    private String generateKey(Long hookId, String sourceId, long eventId) {
        return hookId + DELIMETER + sourceId + DELIMETER + eventId;
    }

    @Override
    public Boolean isParentCommitted(WebhookMessage webhookMessage) {
        try {
            String key = generateKey(webhookMessage.getWebhookId(), webhookMessage.getSourceId(), webhookMessage.getParentEventId());
            log.debug("WebHookDaoImpl get bucket: {} key: {}", bucket, key);
            Location quoteObjectLocation = createLocation(bucket, key);
            return isCommit(quoteObjectLocation);
        } catch (InterruptedException e) {
            log.error("InterruptedException in WebHookDaoImpl when get e: ", e);
            Thread.currentThread().interrupt();
            throw new RiakExecutionException(e);
        } catch (Exception e) {
            log.error("Exception in WebHookDaoImpl when get e: ", e);
            throw new RiakExecutionException(e);
        }
    }

    private boolean isCommit(Location quoteObjectLocation) throws java.util.concurrent.ExecutionException, InterruptedException {
        FetchValue fetch = new FetchValue.Builder(quoteObjectLocation)
                .withOption(FetchValue.Option.R, new Quorum(3))
                .build();
        FetchValue.Response response = client.execute(fetch);
        RiakObject obj = response.getValue(RiakObject.class);
        return obj != null && obj.getValue() != null;
    }

    @Override
    public Boolean isCommitted(WebhookMessage webhookMessage) {
        try {
            String key = generateKey(webhookMessage.getWebhookId(), webhookMessage.getSourceId(), webhookMessage.getEventId());
            log.debug("WebHookDaoImpl get bucket: {} key: {}", bucket, key);
            Location quoteObjectLocation = createLocation(bucket, key);
            return isCommit(quoteObjectLocation);
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
