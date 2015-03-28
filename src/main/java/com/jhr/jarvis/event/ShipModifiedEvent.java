package com.jhr.jarvis.event;

import org.springframework.context.ApplicationEvent;

import com.jhr.jarvis.model.Ship;

public class ShipModifiedEvent extends ApplicationEvent {

    private final Ship ship;
    
    public ShipModifiedEvent(Ship ship) {
        super(ship);
        this.ship = ship;
        System.out.println(String.format("Ship changed to %s.", ship));
    }
}
