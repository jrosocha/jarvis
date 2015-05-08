package com.jhr.jarvis.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.web.WebView;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.google.common.io.CharStreams;
import com.jhr.jarvis.JarvisConfig;
import com.jhr.jarvis.event.CurrentSystemChangedEvent;
import com.jhr.jarvis.event.DrawMapEvent;
import com.jhr.jarvis.event.DrawRouteMapEvent;
import com.jhr.jarvis.event.UpdateMapEvent;
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
    
    @FXML
    private Label mapInformation;
    
    @FXML
    private ProgressIndicator mapLoading;
    
    @Autowired
    private Settings settings;

    @Autowired
    private StarSystemService starSystemService;
    
    @Autowired
    private ShipService shipService;
    
    String mapHtml = null;
    
    @PostConstruct
    public void initMap() {
        Platform.runLater(()->{mapLoading.setVisible(false);});
        try ( InputStream in = getClass().getResourceAsStream("/mapTemplate.html");
                final InputStreamReader inr = new InputStreamReader(in)) {
            mapHtml = CharStreams.toString(inr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof CurrentSystemChangedEvent) {

            Platform.runLater(()->{mapLoading.setVisible(true);});          
            CurrentSystemChangedEvent currentSystemChangedEvent = (CurrentSystemChangedEvent) event;
            StarSystem starSystem = currentSystemChangedEvent.getStarSystem();
            Platform.runLater(()->{mapInformation.setText(String.format("Routes from system %s with a %f ly jump range", starSystem.getName(), shipService.getActiveShip().getJumpDistance()));});            
            MapData mapData = starSystemService.getMapDataForSystem(starSystem, shipService.getActiveShip().getJumpDistance());
            
            try {
                mapData.getOptimalWindowSizeAndAdjustEverythingPositive();
                String mapDataAsString = JarvisConfig.MAPPER.writeValueAsString(mapData);
                String newMapHtml = mapHtml.replace("__DATA__", mapDataAsString);
                newMapHtml = newMapHtml.replace("__X__", "700");
                newMapHtml = newMapHtml.replace("__Y__", "700");
                final String newMapHtmlFinal = newMapHtml;
                //Files.write(new File("/Users/jrosocha/trade/map.html").toPath(), newMapHtml.getBytes("UTF-8"));
                
                Platform.runLater(()->{            
                    try {                     
                        map.getEngine().loadContent(newMapHtmlFinal);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } 
                });
                
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(()->{mapLoading.setVisible(false);});
            }
        }
    }
    
    public Node getView() {
        return view;
    }

}
