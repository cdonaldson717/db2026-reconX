package com.dbtraining.reconx.audit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditAutoConfiguration.class));

    @Test
    void bootDiscoveryMetadataListsAutoConfiguration() {
        ImportCandidates candidates = ImportCandidates.load(
                AutoConfiguration.class, getClass().getClassLoader());

        assertThat(candidates).contains(AuditAutoConfiguration.class.getName());
    }

    @Test
    void autoConfiguresPublisherWithDefaultsWhenPropertyIsMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuditEventPublisher.class);
            assertThat(context).hasSingleBean(AuditProperties.class);
            assertThat(context.getBean(AuditEventPublisher.class).topic())
                    .isEqualTo("audit-events");
        });
    }

    @Test
    void bindsConsumerProperties() {
        contextRunner
                .withPropertyValues("reconx.audit.topic=service-audit-events")
                .run(context -> assertThat(context.getBean(AuditEventPublisher.class).topic())
                        .isEqualTo("service-audit-events"));
    }

    @Test
    void disablingPropertyRemovesAutoConfiguredPublisher() {
        contextRunner
                .withPropertyValues("reconx.audit.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(AuditEventPublisher.class));
    }

    @Test
    void consumerPublisherTakesPrecedence() {
        AuditProperties properties = new AuditProperties();
        AuditEventPublisher customPublisher = new AuditEventPublisher(event -> { }, properties);

        contextRunner
                .withBean(AuditEventPublisher.class, () -> customPublisher)
                .run(context -> assertThat(context.getBean(AuditEventPublisher.class))
                        .isSameAs(customPublisher));
    }

    @Test
    void publisherDelegatesToApplicationEventPublisher() {
        List<Object> published = new ArrayList<>();
        AuditEventPublisher publisher = new AuditEventPublisher(published::add, new AuditProperties());
        Object event = new Object();

        publisher.publish(event);

        assertThat(published).containsExactly(event);
    }
}
