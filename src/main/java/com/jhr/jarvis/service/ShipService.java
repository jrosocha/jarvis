package com.jhr.jarvis.service;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhr.jarvis.event.ShipModifiedEvent;
import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.model.Ship;

@Service
public class ShipService implements ApplicationEventPublisherAware {
    
    @Autowired
    private Settings settings;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ApplicationEventPublisher eventPublisher;
    
    private File shipFile = null;
    
    public boolean isShipEmpty(Ship ship) {
        if (ship == null || ship.getCargoSpace() == 0 || ship.getCash() == 0 || ship.getJumpDistance() == 0) {
            return true;
        }
        return false;
    }
    
    public Ship saveShip(Ship ship) throws JsonGenerationException, JsonMappingException, IOException {
        shipFile = new File(settings.getShipFile());
        
        if (shipFile.exists()) {
            shipFile.delete();
        }
        
        objectMapper.writeValue(shipFile, ship);
        
        eventPublisher.publishEvent(new ShipModifiedEvent(ship));
        
        return ship;
    }
    
    public Ship loadShip() throws JsonParseException, JsonMappingException, IOException {
        shipFile = new File(settings.getShipFile());
        
        if (shipFile.exists()) {
            return objectMapper.readValue(shipFile, Ship.class);
        }
        
        return saveShip(new Ship());
    }
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
    
    
}
