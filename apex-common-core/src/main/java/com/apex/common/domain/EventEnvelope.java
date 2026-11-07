package com.apex.common.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Universal CloudEvents-compliant Event Envelope for Asynchronous Message Dispatching.
 *
 * @param <T> Payload data type
 */
public record EventEnvelope<T>(
    UUID id,
    String source,
    String type,
    String specversion,
    Instant time,
    String datacontenttype,
    T data
) implements Serializable {

    public static final String CLOUDEVENTS_SPEC_VERSION = "1.0";
    public static final String DEFAULT_CONTENT_TYPE = "application/json";

    public static <E> EventEnvelope<E> create(String source, String type, E data) {
        return new EventEnvelope<>(
            UUID.randomUUID(),
            source,
            type,
            CLOUDEVENTS_SPEC_VERSION,
            Instant.now(),
            DEFAULT_CONTENT_TYPE,
            data
        );
    }
}

