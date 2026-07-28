package com.dbtraining.reconx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Enables population of fields annotated with Spring Data audit timestamps. */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
