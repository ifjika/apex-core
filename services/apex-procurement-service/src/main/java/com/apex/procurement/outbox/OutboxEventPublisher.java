package com.apex.procurement.outbox;

import com.apex.common.outbox.OutboxEvent;
import com.apex.common.outbox.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduled background worker reading uncommitted events from outbox_events
 * and streaming them reliably to Kafka/Redpanda with At-Least-Once Delivery.
 */
@Component
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName;

    public OutboxEventPublisher(
        OutboxEventRepository outboxEventRepository,
        KafkaTemplate<String, String> kafkaTemplate,
        @Value("${apex.kafka.topics.procurement-events:apex.procurement.events}") String topicName
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Scheduled(fixedDelayString = "${apex.outbox.polling-interval-ms:2000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents(PageRequest.of(0, 50));

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to publish to topic {}", pendingEvents.size(), topicName);

        for (OutboxEvent event : pendingEvents) {
            try {
                // Keyed by aggregate ID to guarantee partition-level ordering
                kafkaTemplate.send(topicName, event.getAggregateId(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            event.markAsPublished();
                            outboxEventRepository.save(event);
                            log.debug("Successfully published outbox event [{}] to Kafka", event.getId());
                        } else {
                            event.incrementRetry();
                            outboxEventRepository.save(event);
                            log.error("Failed to publish outbox event [{}] to Kafka: {}", event.getId(), ex.getMessage());
                        }
                    });
            } catch (Exception ex) {
                event.incrementRetry();
                outboxEventRepository.save(event);
                log.error("Exception dispatching outbox event [{}]: {}", event.getId(), ex.getMessage());
            }
        }
    }
}

