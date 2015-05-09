package com.jhr.jarvis.event;

import java.util.Set;

import org.springframework.context.ApplicationEvent;

import com.jhr.jarvis.model.BestExchange;

public class ExchangeCompletedEvent extends ApplicationEvent {

    private final Set<BestExchange> exchanges;
    private final ExchangeType type;
    public enum ExchangeType {
        SINGLE_TRADE, MULTI_TRADE, SELL_COMMODITY_WITHIN_SHIP_JUMPS, BUY_COMMODITY_WITHIN_SHIP_JUMPS, SELL_COMMODITY_ANYWHERE, BUY_COMMODITY_ANYWHERE;
    }
    
    public ExchangeCompletedEvent(Set<BestExchange> exchanges, ExchangeType type) {
        super(exchanges);
        this.exchanges = exchanges;
        this.type = type;
    }

    public Set<BestExchange> getExchanges() {
        return exchanges;
    }

    public ExchangeType getType() {
        return type;
    }
    
}
