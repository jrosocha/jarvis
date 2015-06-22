package com.jhr.jarvis;

import java.io.File;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

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
import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.service.EddnService;
import com.jhr.jarvis.service.OrientDbService;
import com.jhr.jarvis.service.StarSystemService;

@Import(JarvisConfig.class)
public class Jarvis extends AbstractJavaFxApplicationSupport {

    private Stage primaryStage;
    
    @Autowired
    private SpringFxmlLoader loader;
    
    @Autowired
    private EddnService eddnService;
    
    @Autowired 
    private StarSystemService starSystemService;
    
    @Autowired
    private Settings settings;
    
    @Autowired
    private OrientDbService orientDbService;
    
    RootLayoutController rootLayoutController;
    
    private BorderPane rootLayout;    
    private TabPane center;

    @Override  
    public void stop() {       
        orientDbService.shutDownDb();
        Platform.exit();
        System.out.println("Shutting down...");
        System.exit(0);
    }  
    
	@Override
	public void start(Stage stage) throws Exception {

        this.primaryStage = stage;
        this.primaryStage.setTitle("Jarvis");
        initRootLayout();
	}

	public static void main(String[] args) {
		launchApp(Jarvis.class, args);
	}

	public void initRootLayout() {

	    rootLayoutController = this.getApplicationContext().getBean(RootLayoutController.class);
	    rootLayout = (BorderPane) rootLayoutController.getView();
	    center = rootLayoutController.getCenter();
	    
        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        showCurrentSystem();
        showStationOverview();
        //showShip();
        showCommands();
        showExchange();
        showConsole();
        showSettings();
        showMap();
        showRoute();
        
        eddnService.scanForEddnMessages();
    }
	
    public void showCurrentSystem() {

        CurrentSystemController controller = this.getApplicationContext().getBean(CurrentSystemController.class);
        rootLayoutController.getCurrentSystemPane().getChildren().add(controller.getView());
    }
    
    public void showStationOverview() {

        StationOverviewController controller = this.getApplicationContext().getBean(StationOverviewController.class);
        
        Tab stationOverviewTab = new Tab();
        stationOverviewTab.setText("Station Overview");
        stationOverviewTab.setContent(controller.getView());
        center.getTabs().add(stationOverviewTab);
    }
    
    public void showCommands() {

        CommandsController controller = this.getApplicationContext().getBean(CommandsController.class);
        rootLayoutController.getCommandsPane().getChildren().add(controller.getView());
    }
    
    public void showExchange() {

        ExchangeController controller = this.getApplicationContext().getBean(ExchangeController.class);
        Tab exchangesTab = new Tab();
        exchangesTab.setText("Exchange");
        exchangesTab.setContent(controller.getView());
        center.getTabs().add(exchangesTab);
        //controller.x();
        controller.populateSystems();
    }
    
    public void showConsole() {

        ConsoleController controller = this.getApplicationContext().getBean(ConsoleController.class);
        
        Tab consoleTab = new Tab();
        consoleTab.setText("Console");
        consoleTab.setContent(controller.getView());
        center.getTabs().add(consoleTab);
    }
    
    public void showSettings() {

        SettingsController settingsController = this.getApplicationContext().getBean(SettingsController.class);
        ShipController shipController = this.getApplicationContext().getBean(ShipController.class);
        
        Tab settingsTab = new Tab();
        settingsTab.setText("Settings");
        VBox settingsVbox = new VBox();
        settingsVbox.setMinSize(700, 700);
        settingsVbox.getChildren().add(settingsController.getView());
        settingsVbox.getChildren().add(shipController.getView());
        settingsTab.setContent(settingsVbox);
        center.getTabs().add(settingsTab);
    }

    public void showMap() {

        MapController controller = this.getApplicationContext().getBean(MapController.class);
        
        Tab mapTab = new Tab();
        mapTab.setText("Map");
        mapTab.setContent(controller.getView());
        center.getTabs().add(mapTab);
    }
    
    public void showRoute() {

        RouteController controller = this.getApplicationContext().getBean(RouteController.class);
        
        Tab routeTab = new Tab();
        routeTab.setText("Route");
        routeTab.setContent(controller.getView());
        center.getTabs().add(routeTab);
    }
    
    
}
