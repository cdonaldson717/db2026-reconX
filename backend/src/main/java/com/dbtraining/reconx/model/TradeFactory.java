package com.dbtraining.reconx.model;

import com.dbtraining.reconx.exception.InvalidTradeException;

import com.dbtraining.reconx.exception.InvalidTradeException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * ============================================================================
 * TICKET-ADV023 — TradeFactory: build a TradeType by asset-class string
 *
 * WHAT:    Single entry point that takes an asset-class string + a map of
 *          field values and returns the right TradeType impl.
 * HOW:     Switch on the asset-class string, dispatch to the correct
 *          builder. Map values are cast/parsed per asset class.
 * WHY:     The Kafka consumer + REST POST endpoint both need to convert an
 *          untyped payload into a typed TradeType. Centralising the
 *          construction here means the parsing logic lives in one place.
 * OBSERVE: TradeFactoryTest.create_unknownAssetClass_throws fails when a
 *          new TradeType impl is added without updating the switch.
 * HINT:    Sealed hierarchy guarantees that every concrete TradeType MUST be
 *          listed in TradeType.permits — so this switch can be made
 *          exhaustive over assetClass enum.
 * ============================================================================
 */
public final class TradeFactory {

    private TradeFactory() { }

    /**
     * Parse a loosely-typed payload into one concrete {@link TradeType}.
     *
     * @param assetClass the asset-class discriminator, matched case-insensitively
     *                   against {@link TradeType.AssetClass}.
     * @param p the raw payload map containing the fields required by the chosen trade type.
     * @return the concrete {@code TradeType} built from the supplied payload.
     * @throws NullPointerException if {@code assetClass} or {@code p} is {@code null}.
     * @throws InvalidTradeException if the discriminator is unknown, required fields are missing,
     *                               or any field cannot be parsed into a valid trade.
     */
    public static TradeType create(String assetClass, Map<String, Object> p) {
        try {
            String discriminator = Objects.requireNonNull(assetClass, "assetClass")
                    .toUpperCase(Locale.ROOT);
            Map<String, Object> parameters = Objects.requireNonNull(p, "parameters");
            TradeType.AssetClass parsed = TradeType.AssetClass.valueOf(discriminator);

            return switch (parsed) {
                case EQUITY -> equity(parameters);
                case FX -> fx(parameters);
                case BOND -> bond(parameters);
                case DERIVATIVE -> derivative(parameters);
            };
        } catch (InvalidTradeException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            String detail = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            throw new InvalidTradeException("Invalid trade payload: " + detail);
        }
    }

    private static EquityTrade equity(Map<String, Object> p) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(string(p, "tradeRef")))
                .instrumentSymbol(string(p, "symbol"))
                .quantity(decimal(p, "quantity"))
                .price(decimal(p, "price"))
                .currency(string(p, "currency"))
                .side(enumValue(p, "side", Side.class))
                .tradeDate(date(p, "tradeDate"))
                .counterpartyId(number(p, "counterpartyId").longValue())
                .build();
    }

    private static FXTrade fx(Map<String, Object> p) {
        return FXTrade.builder()
                .tradeRef(TradeRef.of(string(p, "tradeRef")))
                .ccy1(string(p, "ccy1"))
                .ccy2(string(p, "ccy2"))
                .notionalCcy1(decimal(p, "notionalCcy1"))
                .fxRate(decimal(p, "fxRate"))
                .side(enumValue(p, "side", Side.class))
                .tradeDate(date(p, "tradeDate"))
                .counterpartyId(number(p, "counterpartyId").longValue())
                .build();
    }

    private static BondTrade bond(Map<String, Object> p) {
        return BondTrade.builder()
                .tradeRef(TradeRef.of(string(p, "tradeRef")))
                .isin(string(p, "isin"))
                .faceValue(decimal(p, "faceValue"))
                .couponRate(decimal(p, "couponRate"))
                .maturityDate(date(p, "maturityDate"))
                .currency(string(p, "currency"))
                .side(enumValue(p, "side", Side.class))
                .tradeDate(date(p, "tradeDate"))
                .counterpartyId(number(p, "counterpartyId").longValue())
                .build();
    }

    private static DerivativeTrade derivative(Map<String, Object> p) {
        return DerivativeTrade.builder()
                .tradeRef(TradeRef.of(string(p, "tradeRef")))
                .underlying(string(p, "underlying"))
                .strike(decimal(p, "strike"))
                .quantity(decimal(p, "quantity"))
                .expiry(date(p, "expiry"))
                .optionType(enumValue(p, "optionType", DerivativeTrade.OptionType.class))
                .currency(string(p, "currency"))
                .side(enumValue(p, "side", Side.class))
                .tradeDate(date(p, "tradeDate"))
                .counterpartyId(number(p, "counterpartyId").longValue())
                .build();
    }

    private static Object required(Map<String, Object> p, String key) {
        return Objects.requireNonNull(p.get(key), key);
    }

    private static String string(Map<String, Object> p, String key) {
        return (String) required(p, key);
    }

    private static Number number(Map<String, Object> p, String key) {
        return (Number) required(p, key);
    }

    private static BigDecimal decimal(Map<String, Object> p, String key) {
        return new BigDecimal(required(p, key).toString());
    }

    private static LocalDate date(Map<String, Object> p, String key) {
        return LocalDate.parse(string(p, key));
    }

    private static <E extends Enum<E>> E enumValue(Map<String, Object> p, String key, Class<E> type) {
        return Enum.valueOf(type, string(p, key));
    }
}
