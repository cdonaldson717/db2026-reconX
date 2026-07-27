package com.dbtraining.reconx.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TradeRequestValidationTest {

    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    @Test
    void wellFormedRequest_hasNoViolations() {
        assertThat(validator.validate(validRequest(new BigDecimal("100")))).isEmpty();
    }

    @Test
    void negativeQuantity_hasOnePositiveViolation() {
        Set<ConstraintViolation<TradeRequest>> violations =
                validator.validate(validRequest(new BigDecimal("-1")));

        assertThat(violations).singleElement().satisfies(violation -> {
            assertThat(violation.getPropertyPath().toString()).isEqualTo("quantity");
            assertThat(violation.getMessage()).isEqualTo("must be greater than 0");
        });
    }

    @Test
    void malformedTradeRef_hasDocumentedPatternViolation() {
        TradeRequest request = new TradeRequest(
                "foo",
                1L,
                1L,
                "EQUITY",
                "BUY",
                new BigDecimal("100"),
                new BigDecimal("25"),
                LocalDate.of(2026, 6, 3));

        assertThat(validator.validate(request)).singleElement().satisfies(violation -> {
            assertThat(violation.getPropertyPath().toString()).isEqualTo("tradeRef");
            assertThat(violation.getMessage())
                    .isEqualTo("tradeRef must match AAA-YYYYMMDD-NNNN");
        });
    }

    private TradeRequest validRequest(BigDecimal quantity) {
        return new TradeRequest(
                "EQU-20260603-0001",
                1L,
                1L,
                "EQUITY",
                "BUY",
                quantity,
                new BigDecimal("25"),
                LocalDate.of(2026, 6, 3));
    }
}
