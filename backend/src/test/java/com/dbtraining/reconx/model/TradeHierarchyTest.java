package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TradeHierarchyTest {

    private static final Set<Class<?>> LEAVES = Set.of(
            EquityTrade.class, FXTrade.class, BondTrade.class, DerivativeTrade.class);

    @Test
    void publicContractIsSealedToTheFourTradeTypes() {
        assertThat(TradeType.class.isSealed()).isTrue();
        assertThat(Set.of(TradeType.class.getPermittedSubclasses())).isEqualTo(LEAVES);
        assertThat(LEAVES).allSatisfy(type -> {
            assertThat(Modifier.isFinal(type.getModifiers())).isTrue();
            assertThat(Trade.class.isAssignableFrom(type)).isTrue();
            assertThat(TradeType.class.isAssignableFrom(type)).isTrue();
        });
    }

    @Test
    void sharedBaseIsInternalAbstractAndSealed() {
        assertThat(Modifier.isPublic(Trade.class.getModifiers())).isFalse();
        assertThat(Modifier.isAbstract(Trade.class.getModifiers())).isTrue();
        assertThat(Trade.class.isSealed()).isTrue();
        assertThat(Set.of(Trade.class.getPermittedSubclasses())).isEqualTo(LEAVES);
    }
}
