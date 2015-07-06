package com.jhr.jarvis.event;

import org.springframework.context.ApplicationEvent;

public class EddnMessageQueueModifiedEvent extends ApplicationEvent {

    private final int message;
    
    public EddnMessageQueueModifiedEvent(int message) {
        super(message);
        this.message = message;
    }

    public int getMessage() {
        return message;
    }
}
