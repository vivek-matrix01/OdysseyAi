package com.OdysseyAi.demo.travel.interfaces;

import java.math.BigDecimal;

public interface CurrencyClient {

    BigDecimal getExchangeRate(
            String baseCurrency,
            String targetCurrency
    );
}