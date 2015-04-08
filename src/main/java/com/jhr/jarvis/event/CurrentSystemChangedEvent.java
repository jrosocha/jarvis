package com.jhr.jarvis.event;

import org.springframework.context.ApplicationEvent;

import com.jhr.jarvis.model.StarSystem;

public class CurrentSystemChangedEvent extends ApplicationEvent {

    private final StarSystem starSystem;
    
    public CurrentSystemChangedEvent(StarSystem system) {
        super(system);
        this.starSystem = system;
        System.out.println(String.format("Local system changed to %s.", system));
    }

    public StarSystem getStarSystem() {
        return starSystem;
    }
}
