package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

/** Resolves trade relationships without nullable intermediate values. */
@Service
@Transactional(readOnly = true)
public class TradeLookupService {

    private final TradeRepository tradeRepo;
    private final CounterpartyRepository counterpartyRepo;

    public TradeLookupService(TradeRepository tradeRepo,
                              CounterpartyRepository counterpartyRepo) {
        this.tradeRepo = tradeRepo;
        this.counterpartyRepo = counterpartyRepo;
    }

    public Counterparty counterpartyForTradeRef(String tradeRef) {
        return tradeRepo.findByTradeRef(tradeRef)
                .map(trade -> trade.getCounterparty().getId())
                .flatMap(counterpartyRepo::findById)
                .orElseThrow(() -> new NoSuchElementException(
                        "No counterparty resolvable for tradeRef=" + tradeRef));
    }
}
