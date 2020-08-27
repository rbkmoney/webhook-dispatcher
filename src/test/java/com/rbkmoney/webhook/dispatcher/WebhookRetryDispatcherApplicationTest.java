package com.rbkmoney.webhook.dispatcher;

import com.rbkmoney.kafka.common.exception.RetryableException;
import com.rbkmoney.webhook.dispatcher.dao.WebHookDao;
import com.rbkmoney.webhook.dispatcher.service.WebHookDispatcherService;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebhookDispatcherApplication.class)
@TestPropertySource(properties = {
        "merchant.timeout=1",
        "retry.first.seconds=1",
        "retry.second.seconds=2",
        "retry.third.seconds=3",
        "retry.last.seconds=4",
        "retry.dead.time.hours=1"
})
public class WebhookRetryDispatcherApplicationTest extends AbstractKafkaIntegrationTest {

    public static final long EVENT_ID = 123L;
    public static final String URL = "http://localhost:8089";
    public static final String APPLICATION_JSON = "application/json";

    @Autowired
    WebHookDao webHookDao;

    @MockBean
    private WebHookDispatcherService webHookDispatcherService;

    @Test
    public void listenCreatedTimeout() throws ExecutionException, InterruptedException, IOException {
        Mockito.when(webHookDispatcherService.dispatch(Mockito.any())).thenThrow(RetryableException.class);

        String sourceId = "123";
        WebhookMessage webhook = createWebhook(sourceId, Instant.now().toString(), 0);
        ProducerRecord producerRecord = new ProducerRecord<>(Initializer.WEBHOOK_FORWARD, webhook.source_id, webhook);
        Producer<String, WebhookMessage> producer = createProducer();

        producer.send(producerRecord).get();
        producer.close();

        Thread.sleep(20000L);

        Mockito.verify(webHookDispatcherService, Mockito.times(7)).dispatch(Mockito.any());
    }

    @NotNull
    private WebhookMessage createWebhook(String sourceId, String createdAt, long eventId) {
        WebhookMessage webhook = new WebhookMessage();
        webhook.setSourceId(sourceId);
        webhook.setCreatedAt(createdAt);
        webhook.setUrl(URL);
        webhook.setContentType(APPLICATION_JSON);
        webhook.setRequestBody("\\{\\}".getBytes());
        webhook.setEventId(eventId);
        webhook.setAdditionalHeaders(new HashMap<>());
        webhook.setParentEventId(-1);
        return webhook;
    }

}
