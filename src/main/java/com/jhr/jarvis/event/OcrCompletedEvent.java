package com.jhr.jarvis.event;

import org.springframework.context.ApplicationEvent;

import com.jhr.jarvis.model.StarSystem;

public class OcrCompletedEvent extends ApplicationEvent {
    
    public OcrCompletedEvent(Object o) {
        super(o);
    }

}
