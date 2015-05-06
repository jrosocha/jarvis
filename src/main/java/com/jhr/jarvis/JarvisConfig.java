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
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.jhr.jarvis.controllers.CommandsController;
import com.jhr.jarvis.controllers.ConsoleController;
import com.jhr.jarvis.controllers.CurrentSystemController;
import com.jhr.jarvis.controllers.ExchangeController;
import com.jhr.jarvis.controllers.MapController;
import com.jhr.jarvis.controllers.RootLayoutController;
import com.jhr.jarvis.controllers.RouteController;
import com.jhr.jarvis.controllers.SettingsController;
import com.jhr.jarvis.controllers.ShipController;
import com.jhr.jarvis.controllers.StationOverviewController;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableScheduling
public class JarvisConfig {
    
    @Autowired 
    private SpringFxmlLoader springFxmlLoader;
    
    public static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.registerModule(new JodaModule());
        MAPPER.registerModule(new JSR310Module());
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
            return (RootLayoutController) springFxmlLoader.loadController("/com/jhr/jarvis/controllers/RootLayout.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }
    
    @Bean
    public CurrentSystemController getCurrentSystemController() {
        
        try {
            return (CurrentSystemController) springFxmlLoader.loadController("/com/jhr/jarvis/controllers/CurrentSystem.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }

    }
    
    @Bean
    public StationOverviewController getStationOverviewController() {
        
        try {
            return (StationOverviewController) springFxmlLoader.loadController("/com/jhr/jarvis/controllers/StationOverview.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }

    }
    
    @Bean
    public ShipController getShipController() {

        try {
            return (ShipController) springFxmlLoader.loadController("/com/jhr/jarvis/controllers/Ship.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }
    
    @Bean
    public ExchangeController getExchangeController() {

        try {
            return (ExchangeController) springFxmlLoader.loadController("/com/jhr/jarvis/controllers/Exchange.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }
    
    @Bean
    public CommandsController getCommandsController() {

        try {
            return (CommandsController) springFxmlLoader.loadController("/com/jhr/jarvis/controllers/Commands.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }
    
    @Bean
    public ConsoleController getConsoleController() {

        try {
            return (ConsoleController) springFxmlLoader.loadController("/com/jhr/jarvis/controllers/Console.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }
    
    @Bean
    public SettingsController getSettingsController() {

        try {
            return (SettingsController) springFxmlLoader.loadController("/com/jhr/jarvis/controllers/Settings.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }
    
    @Bean
    public MapController getMapController() {

        try {
            return (MapController) springFxmlLoader.loadController("/com/jhr/jarvis/controllers/Map.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }
    
    @Bean
    public RouteController getRouteController() {

        try {
            return (RouteController) springFxmlLoader.loadController("/com/jhr/jarvis/controllers/Route.fxml");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }

}
