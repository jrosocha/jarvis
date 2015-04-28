package com.jhr.jarvis.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.annotation.PostConstruct;















import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;















import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;















import com.fasterxml.jackson.core.JsonProcessingException;
import com.jhr.jarvis.Jarvis;
import com.jhr.jarvis.JarvisConfig;
import com.jhr.jarvis.event.ConsoleEvent;
import com.jhr.jarvis.event.CurrentSystemChangedEvent;
import com.jhr.jarvis.model.MapData;
import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.model.StarSystem;
import com.jhr.jarvis.service.ShipService;
import com.jhr.jarvis.service.StarSystemService;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class MapController implements ApplicationListener<ApplicationEvent> {
    
    @FXML
    private Node view;
    
    @FXML
    private WebView map;
    
    @Autowired
    private Settings settings;

    @Autowired
    private StarSystemService starSystemService;
    
    @Autowired
    private ShipService shipService;
    
    String mapHtml = null;
    
    @PostConstruct
    public void initMap() {

        try {
            mapHtml = new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/mapTemplate.html").toURI())));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof CurrentSystemChangedEvent) {
            CurrentSystemChangedEvent currentSystemChangedEvent = (CurrentSystemChangedEvent) event;
            
            StarSystem starSystem = currentSystemChangedEvent.getStarSystem();
            MapData mapData = starSystemService.getMapDataForSystem(starSystem, shipService.getActiveShip().getJumpDistance());
            
            try {
                String mapDataAsString = JarvisConfig.MAPPER.writeValueAsString(mapData);
                final String newMapHtml = mapHtml.replace("__DATA__", mapDataAsString);
                
                Platform.runLater(()->{            
                    try {
                        map.getEngine().loadContent(newMapHtml);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } 
                });
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    
    public Node getView() {
        return view;
    }

}
