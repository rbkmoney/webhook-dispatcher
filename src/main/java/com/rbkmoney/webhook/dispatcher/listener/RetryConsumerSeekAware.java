package com.rbkmoney.webhook.dispatcher.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ConsumerSeekAware;

import java.util.Map;

@Slf4j
public abstract class RetryConsumerSeekAware implements ConsumerSeekAware {

    protected ThreadLocal<ConsumerSeekAware.ConsumerSeekCallback> consumerSeekCallback;

    @Override
    public void registerSeekCallback(ConsumerSeekCallback consumerSeekCallback) {
        this.consumerSeekCallback = ThreadLocal.withInitial(() -> consumerSeekCallback);
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> map, ConsumerSeekCallback consumerSeekCallback) {
        // do nothing
    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> map, ConsumerSeekCallback consumerSeekCallback) {
        // do nothing
    }
}
