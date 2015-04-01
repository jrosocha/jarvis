package com.jhr.jarvis.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.jhr.jarvis.model.BestExchange;

public class ExchangeCompletedEvent extends ApplicationEvent {

    private final List<BestExchange> exchanges;
    private final Boolean singleStop;
    
    public ExchangeCompletedEvent(List<BestExchange> exchanges, Boolean singleStop) {
        super(exchanges);
        this.exchanges = exchanges;
        this.singleStop = singleStop;
    }

    public List<BestExchange> getExchanges() {
        return exchanges;
    }

    public Boolean getSingleStop() {
        return singleStop;
    }
}
