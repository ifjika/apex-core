package com.apex.common.outbox;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * MappedSuperclass / Generic Entity for Transactional Outbox Pattern.
 * Sub-modules (procurement, finance, hris) map this table within their respective schemas.
 */
@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_unpub", columnList = "published, created_at")
})
public class OutboxEvent {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "aggregate_type", length = 64, nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 64, nullable = false)
    private String aggregateId;

    @Column(name = "event_type", length = 128, nullable = false)
    private String eventType;

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "published", nullable = false)
    private boolean published = false;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "published_at")
    private Instant publishedAt;

    public OutboxEvent() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.published = false;
        this.retryCount = 0;
    }

    public OutboxEvent(String aggregateType, String aggregateId, String eventType, String payload) {
        this();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public void markAsPublished() {
        this.published = true;
        this.publishedAt = Instant.now();
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
}

