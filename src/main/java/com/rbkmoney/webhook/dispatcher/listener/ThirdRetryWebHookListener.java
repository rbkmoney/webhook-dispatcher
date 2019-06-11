package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.Webhook;
import com.rbkmoney.webhook.dispatcher.handler.RetryWebHookHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

//@Slf4j
//@Component
//@RequiredArgsConstructor
public class ThirdRetryWebHookListener {

//    private static final long TIMEOUT = 1800L;
//
//    private final RetryWebHookHandler handler;
//
//    @Value("${kafka.topic.webhook.last.retry}")
//    private String postponedTopic;
//
//    @KafkaListener(topics = "${kafka.topic.webhook.third.retry}", containerFactory = "kafkaListenerContainerFactory")
//    public void listen(Webhook webhook, Acknowledgment acknowledgment) {
//        handler.handle(postponedTopic, acknowledgment, webhook, TIMEOUT);
//    }

}
