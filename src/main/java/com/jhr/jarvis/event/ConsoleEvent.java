package com.jhr.jarvis.event;

import org.springframework.context.ApplicationEvent;

import com.jhr.jarvis.model.StarSystem;

public class ConsoleEvent extends ApplicationEvent {

    private final String message;
    
    public ConsoleEvent(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
