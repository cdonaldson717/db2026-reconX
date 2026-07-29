package com.dbtraining.reconx.repository.entity;

/** Lifecycle state persisted for a trade. */
public enum TradeStatus {
    PENDING,
    MATCHED,
    BREAK,
    CANCELLED
}
