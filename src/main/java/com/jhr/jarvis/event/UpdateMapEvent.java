package com.jhr.jarvis.event;

import com.jhr.jarvis.model.MapData;

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
