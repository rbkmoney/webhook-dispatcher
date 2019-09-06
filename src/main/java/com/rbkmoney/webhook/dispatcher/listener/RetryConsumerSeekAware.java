package com.rbkmoney.webhook.dispatcher.listener;

import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ConsumerSeekAware;

import java.util.Map;

public abstract class RetryConsumerSeekAware implements ConsumerSeekAware {

    protected ConsumerSeekAware.ConsumerSeekCallback consumerSeekCallback;

    @Override
    public void registerSeekCallback(ConsumerSeekCallback consumerSeekCallback) {
        this.consumerSeekCallback = consumerSeekCallback;
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> map, ConsumerSeekCallback consumerSeekCallback) {

        // nothing is needed here for this program
    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> map, ConsumerSeekCallback consumerSeekCallback) {

        // nothing is needed here for this program
    }
}
