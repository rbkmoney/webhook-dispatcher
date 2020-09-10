package com.rbkmoney.webhook.dispatcher.handler;

import com.rbkmoney.webhook.dispatcher.WebhookMessageServiceSrv;
import com.rbkmoney.webhook.dispatcher.WebhookNotFound;
import com.rbkmoney.webhook.dispatcher.service.WebhookMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookMessageServiceHandler implements WebhookMessageServiceSrv.Iface {

    private final WebhookMessageService webhookMessageService;

    @Override
    public void resend(
            long webhookId,
            String sourceId,
            long eventId) throws WebhookNotFound {
        webhookMessageService.resend(webhookId, sourceId, eventId);
    }
}
