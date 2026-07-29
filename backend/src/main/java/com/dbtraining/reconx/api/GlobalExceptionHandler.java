package com.dbtraining.reconx.api;

import com.dbtraining.reconx.exception.DuplicateTradeRefException;
import com.dbtraining.reconx.exception.ReconException;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String ERROR_BASE =
            "https://reconx.dbtraining.com/errors/";

    @ExceptionHandler(TradeNotFoundException.class)
    public ProblemDetail handleTradeNotFound(
            TradeNotFoundException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage());

        problem.setType(URI.create(ERROR_BASE + "trade-not-found"));
        problem.setTitle("Trade not found");
        problem.setProperty("timestamp", OffsetDateTime.now());

        return problem;
    }

    @ExceptionHandler(DuplicateTradeRefException.class)
    public ProblemDetail handleDuplicateTradeRef(
            DuplicateTradeRefException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());

        problem.setType(URI.create(ERROR_BASE + "duplicate-trade-ref"));
        problem.setTitle("Duplicate trade reference");
        problem.setProperty("timestamp", OffsetDateTime.now());

        return problem;
    }

    @ExceptionHandler(ReconException.class)
    public ProblemDetail handleReconException(
            ReconException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                exception.getMessage());

        problem.setType(URI.create(ERROR_BASE + "reconciliation-error"));
        problem.setTitle("Reconciliation failed");
        problem.setProperty("reconBreakId", UUID.randomUUID().toString());
        problem.setProperty("timestamp", OffsetDateTime.now());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception) {

        String detail = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                detail);

        problem.setType(URI.create(ERROR_BASE + "validation-failed"));
        problem.setTitle("Validation failed");
        problem.setProperty("timestamp", OffsetDateTime.now());

        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(
            Exception exception) {

        LOGGER.error("Unexpected application error", exception);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");

        problem.setType(URI.create(ERROR_BASE + "internal-error"));
        problem.setTitle("Internal server error");
        problem.setProperty("timestamp", OffsetDateTime.now());

        return problem;
    }
}