package com.jhr.jarvis.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.jhr.jarvis.model.MapData;
import com.jhr.jarvis.model.StarSystem;

public class DrawRouteMapEvent extends DrawMapEvent {

    private final List<String> systemsInRoute;
    
    public DrawRouteMapEvent(List<String> systemsInRoute) {
        super(systemsInRoute);
        this.systemsInRoute = systemsInRoute;
    }

    public List<String> getSystemsInRoute() {
        return systemsInRoute;
    }
    
}
