package com.dbtraining.reconx.dto;

import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagedResponseTest {

    @Test
    void of_mapsItemsAndCopiesPageMetadata() {
        Trade first = new Trade();
        first.setTradeRef("ref1");
        Trade second = new Trade();
        second.setTradeRef("ref2");

        Page<Trade> page = new PageImpl<>(
                List.of(first, second),
                PageRequest.of(0, 2),
                2);

        PagedResponse<String> response = PagedResponse.of(page, Trade::getTradeRef);

        assertThat(response.items()).containsExactly("ref1", "ref2");
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.totalPages()).isEqualTo(1);
    }
}
