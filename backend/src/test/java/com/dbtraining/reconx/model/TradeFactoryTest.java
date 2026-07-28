package com.dbtraining.reconx.model;

import com.dbtraining.reconx.exception.InvalidTradeException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeFactoryTest {

    @Test
    void create_routesEveryAssetClassToItsBuilder() {
        assertThat(TradeFactory.create("equity", equityPayload())).isInstanceOf(EquityTrade.class);
        assertThat(TradeFactory.create("FX", fxPayload())).isInstanceOf(FXTrade.class);
        assertThat(TradeFactory.create("BOND", bondPayload())).isInstanceOf(BondTrade.class);
        assertThat(TradeFactory.create("DERIVATIVE", derivativePayload()))
                .isInstanceOf(DerivativeTrade.class);
    }

    @Test
    void create_unknownAssetClass_throwsInvalidTradeException() {
        assertThatThrownBy(() -> TradeFactory.create("FOO", Map.of()))
                .isInstanceOf(InvalidTradeException.class)
                .hasMessageContaining("FOO");
    }

    @Test
    void create_missingField_namesFieldInInvalidTradeException() {
        Map<String, Object> payload = equityPayload();
        payload.remove("price");

        assertThatThrownBy(() -> TradeFactory.create("EQUITY", payload))
                .isInstanceOf(InvalidTradeException.class)
                .hasMessageContaining("price");
    }

    @Test
    void create_wrongType_translatesClassCastException() {
        Map<String, Object> payload = equityPayload();
        payload.put("counterpartyId", "not-a-number");

        assertThatThrownBy(() -> TradeFactory.create("EQUITY", payload))
                .isInstanceOf(InvalidTradeException.class)
                .isNotInstanceOf(ClassCastException.class)
                .hasMessageContaining("String");
    }

    @Test
    void factoryIsFinalWithPrivateConstructor() {
        Constructor<?>[] constructors = TradeFactory.class.getDeclaredConstructors();

        assertThat(Modifier.isFinal(TradeFactory.class.getModifiers())).isTrue();
        assertThat(constructors).hasSize(1);
        assertThat(Modifier.isPrivate(constructors[0].getModifiers())).isTrue();
    }

    private Map<String, Object> equityPayload() {
        return payload(Map.of(
                "tradeRef", "EQU-20260603-0001", "symbol", "SAP.DE",
                "quantity", "100", "price", "50", "currency", "EUR"));
    }

    private Map<String, Object> fxPayload() {
        return payload(Map.of(
                "tradeRef", "FXS-20260603-0001", "ccy1", "EUR", "ccy2", "USD",
                "notionalCcy1", "1000", "fxRate", "1.10"));
    }

    private Map<String, Object> bondPayload() {
        Map<String, Object> payload = payload(Map.of(
                "tradeRef", "BND-20260603-0001", "isin", "DE0001102341",
                "faceValue", "100000", "couponRate", "0.025", "currency", "EUR"));
        payload.put("maturityDate", "2036-06-03");
        return payload;
    }

    private Map<String, Object> derivativePayload() {
        Map<String, Object> payload = payload(Map.of(
                "tradeRef", "DRV-20260603-0001", "underlying", "SAP.DE",
                "strike", "50", "quantity", "100", "currency", "EUR"));
        payload.put("expiry", "2027-06-03");
        payload.put("optionType", "CALL");
        return payload;
    }

    private Map<String, Object> payload(Map<String, Object> assetFields) {
        Map<String, Object> payload = new HashMap<>(assetFields);
        payload.put("side", "BUY");
        payload.put("tradeDate", "2026-06-03");
        payload.put("counterpartyId", 1L);
        return payload;
    }
}
