package com.jhr.jarvis.event;

import org.springframework.context.ApplicationEvent;

import com.jhr.jarvis.model.Station;

public class StationOverviewChangedEvent extends ApplicationEvent {

    private final Station station;
    
    public StationOverviewChangedEvent(Station station) {
        super(station);
        this.station = station;
        System.out.println(String.format("Station overview for %s.", station));
    }
}
