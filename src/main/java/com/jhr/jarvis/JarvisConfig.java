package com.jhr.jarvis;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableScheduling
class JarvisConfig {
    
    @Autowired 
    private SpringFxmlLoader springFxmlLoader;
    
    public static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    }
    
    @Bean
    public ObjectMapper getObjectMapper() {
        return MAPPER;
    }
    
    @Bean
    public RootLayoutController getRootLayoutController() {

        try {
            return (RootLayoutController) springFxmlLoader.loadController("RootLayout.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }
    
    @Bean
    public CurrentSystemController getCurrentSystemController() {
        
        try {
            return (CurrentSystemController) springFxmlLoader.loadController("CurrentSystem.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }

    }
    
    @Bean
    public StationOverviewController getStationOverviewController() {
        
        try {
            return (StationOverviewController) springFxmlLoader.loadController("StationOverview.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }

    }
    
    @Bean
    public ShipController getShipController() {

        try {
            return (ShipController) springFxmlLoader.loadController("Ship.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }

}
