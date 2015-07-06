package com.jhr.jarvis.event;

import org.springframework.context.ApplicationEvent;

public class ExchangeStationChangedEvent extends ApplicationEvent {

    private final String stationName;
    private final FROM_OR_TO type;
    
    public ExchangeStationChangedEvent(String stationName, FROM_OR_TO type) {
        super(stationName);
        this.stationName = stationName;
        this.type = type;
        System.out.println(String.format("Exchange station changed for %s @ %s.", type, stationName));
    }
    
    public enum FROM_OR_TO {
        FROM, TO;
    }

    public String getStationName() {
        return stationName;
    }

    public FROM_OR_TO getType() {
        return type;
    }
}
