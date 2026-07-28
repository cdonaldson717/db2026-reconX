package com.dbtraining.reconx.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TradeRepositoryTest {

    @MockBean
    private ReconResultRepository reconResultRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void findByFilters_withDateRangeAndNoOptionalFilters_returnsSeedTradesInRange() {
        LocalDate from = LocalDate.of(2026, 4, 1);
        LocalDate to = LocalDate.of(2026, 4, 10);

        Page<com.dbtraining.reconx.repository.entity.Trade> page =
                tradeRepository.findByFilters(from, to, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent())
                .allMatch(trade -> !trade.getTradeDate().isBefore(from) && !trade.getTradeDate().isAfter(to));
    }

    @Test
    void findByFilters_withStatusAndCounterparty_filtersRows() {
        LocalDate from = LocalDate.of(2026, 4, 1);
        LocalDate to = LocalDate.of(2026, 7, 31);
        String status = "PENDING";
        Long counterpartyId = 1L;

        Page<com.dbtraining.reconx.repository.entity.Trade> page =
                tradeRepository.findByFilters(from, to, status, counterpartyId, PageRequest.of(0, 20));

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent())
                .allMatch(trade -> status.equals(trade.getStatus()))
                .allMatch(trade -> trade.getCounterparty() != null
                        && counterpartyId.equals(trade.getCounterparty().getId()))
                .allMatch(trade -> !trade.getTradeDate().isBefore(from) && !trade.getTradeDate().isAfter(to));
    }
}
