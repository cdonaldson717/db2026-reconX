package com.dbtraining.reconx.service;

/** Immutable aggregate of reconciliation outcomes. */
public record ReconSummary(long total, long matched, long broken) {

    public static ReconSummary empty() {
        return new ReconSummary(0, 0, 0);
    }

    /** Mutable accumulation type used only while collecting a stream. */
    public static final class Builder {
        long total;
        long matched;
        long broken;
    }
}
