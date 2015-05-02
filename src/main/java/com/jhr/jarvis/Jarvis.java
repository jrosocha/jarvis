package com.jhr.jarvis;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
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
import com.jhr.jarvis.service.EddnService;

@Import(JarvisConfig.class)
public class Jarvis extends AbstractJavaFxApplicationSupport {

    private Stage primaryStage;
    
    @Autowired
    private SpringFxmlLoader loader;
    
    @Autowired
    private EddnService eddnService;
    
    RootLayoutController rootLayoutController;
    
    private BorderPane rootLayout;    
    private TabPane center;
    
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
        showShip();
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
    
    public void showShip() {

        ShipController controller = this.getApplicationContext().getBean(ShipController.class);
        rootLayoutController.getShipPane().getChildren().add(controller.getView());
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

        SettingsController controller = this.getApplicationContext().getBean(SettingsController.class);
        
        Tab settingsTab = new Tab();
        settingsTab.setText("Settings");
        settingsTab.setContent(controller.getView());
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
