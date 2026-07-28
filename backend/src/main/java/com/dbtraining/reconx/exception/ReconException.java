package com.dbtraining.reconx.exception;

/**
 * ============================================================================
 * TICKET-ADV025 — Root of the exception hierarchy
 *
 * WHAT:    Abstract parent for every domain-level exception raised by the
 *          reconciliation service.
 * HOW:     Extends RuntimeException (we don't want checked-exception noise
 *          on the controller signatures). All subclasses go in this package.
 * WHY:     One root means @RestControllerAdvice can {@code catch (ReconException)}
 *          and map every domain-specific subtype to an RFC-7807 ProblemDetail
 *          without an explicit handler per type.
 * ============================================================================
 */
public abstract class ReconException extends RuntimeException {
    /**
     * Create a domain exception with a human-readable problem description.
     *
     * @param message the error message presented to callers and logs.
     */
    protected ReconException(String message) { super(message); }

    /**
     * Create a domain exception with both a message and an underlying cause.
     *
     * @param message the high-level problem description.
     * @param cause the underlying failure that triggered this domain exception.
     */
    protected ReconException(String message, Throwable cause) { super(message, cause); }
}
