package com.rbkmoney.webhook.dispatcher.utils;

public class KeyGenerator {

    private static final String DELIMITER = "_";

    public static String generateKey(Long hookId, String sourceId, long eventId) {
        return hookId + DELIMITER + sourceId + DELIMITER + eventId;
    }

}
