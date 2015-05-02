package com.jhr.jarvis.event;

import org.springframework.context.ApplicationEvent;

import com.jhr.jarvis.model.MapData;
import com.jhr.jarvis.model.StarSystem;

public class DrawMapEvent extends ApplicationEvent {

    public DrawMapEvent(Object source) {
        super(source);
    }
    
}
