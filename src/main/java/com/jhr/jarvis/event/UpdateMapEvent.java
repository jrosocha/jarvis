package com.jhr.jarvis.event;

import org.springframework.context.ApplicationEvent;

import com.jhr.jarvis.model.MapData;
import com.jhr.jarvis.model.StarSystem;

public class UpdateMapEvent extends DrawMapEvent {

    private final MapData message;
    
    public UpdateMapEvent(MapData mapData) {
        super(mapData);
        this.message = mapData;
    }

    public MapData getMessage() {
        return message;
    }

}
