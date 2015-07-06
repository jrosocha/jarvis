package com.jhr.jarvis.event;

import org.springframework.context.ApplicationEvent;

public class DrawMapEvent extends ApplicationEvent {

    public DrawMapEvent(Object source) {
        super(source);
    }
    
}
