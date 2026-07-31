package com.dbtraining.reconx.audit;

import org.springframework.context.ApplicationEventPublisher;

import java.util.Objects;

/** Publishes audit events through Spring's application event infrastructure. */
public class AuditEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final AuditProperties properties;

    public AuditEventPublisher(ApplicationEventPublisher eventPublisher,
                               AuditProperties properties) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public void publish(Object event) {
        eventPublisher.publishEvent(Objects.requireNonNull(event, "event"));
    }

    public String topic() {
        return properties.getTopic();
    }
}
