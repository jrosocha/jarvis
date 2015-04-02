package com.jhr.jarvis;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.jhr.jarvis.controllers.CurrentSystemController;
import com.jhr.jarvis.controllers.ExchangeController;
import com.jhr.jarvis.controllers.RootLayoutController;
import com.jhr.jarvis.controllers.ShipController;
import com.jhr.jarvis.controllers.StationOverviewController;

@Import(JarvisConfig.class)
public class Jarvis extends AbstractJavaFxApplicationSupport {

    private Stage primaryStage;
    
    @Autowired
    private SpringFxmlLoader loader;
    
    RootLayoutController rootLayoutController;
    
    private BorderPane rootLayout;    
    //private VBox left;
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
	    //left = rootLayoutController.getLeft();
	    center = rootLayoutController.getCenter();
	    
        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        showCurrentSystem();
        showStationOverview();
        showShip();
        showExchange();
    }
	
    public void showCurrentSystem() {

        CurrentSystemController controller = this.getApplicationContext().getBean(CurrentSystemController.class);
        rootLayoutController.getCurrentSystemPane().getChildren().add(controller.getView());
        //left.getChildren().add(controller.getView());
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
        //left.getChildren().add(controller.getView());
    }
    
    public void showExchange() {

        ExchangeController controller = this.getApplicationContext().getBean(ExchangeController.class);
        Tab exchnagesTab = new Tab();
        exchnagesTab.setText("Exchange");
        exchnagesTab.setContent(controller.getView());
        center.getTabs().add(exchnagesTab);
        //controller.x();
        controller.populateSystems();
    }

}
