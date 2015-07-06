package com.jhr.jarvis.event;

import org.springframework.context.ApplicationEvent;

public class OcrCompletedEvent extends ApplicationEvent {
    
    public OcrCompletedEvent(Object o) {
        super(o);
    }

}
