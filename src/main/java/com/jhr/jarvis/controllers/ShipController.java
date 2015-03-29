package com.jhr.jarvis.controllers;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.jhr.jarvis.event.ShipModifiedEvent;
import com.jhr.jarvis.model.Ship;
import com.jhr.jarvis.service.ShipService;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class ShipController implements ApplicationListener<ShipModifiedEvent> {

    @Autowired
    private ShipService shipService;
    
    @FXML
    private Node view;
    
    @FXML
    private TextField shipCargo;
    
    @FXML
    private TextField shipRange;
    
    @FXML
    private TextField shipCredits;
    
    @Override
    public void onApplicationEvent(ShipModifiedEvent event) {
        
        if (event.getSource() != null) {
            Platform.runLater(new UpdateShip((Ship) event.getSource()));
        }
    }
    
    public Node getView() {
        return view;
    }
    
    private class UpdateShip implements Runnable {
        
        private Ship ship;
        
        public UpdateShip(Ship ship) {
            this.ship = ship;
        }
        
        @Override
        public void run() {
            shipCargo.setText(ship.getCargoSpace() + "");
            shipRange.setText(ship.getJumpDistance() + "");
            shipCredits.setText(ship.getCash() + "");
        }
    }
    
    @PostConstruct
    public void loadShip() {
        
        try {
            shipService.loadShip();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        shipCargo.setOnAction((event) -> {
            saveShip(shipCargo.getText(), shipRange.getText(), shipCredits.getText());
        });
        shipCargo.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                saveShip(shipCargo.getText(), shipRange.getText(), shipCredits.getText());
            }
        });
        
        shipRange.setOnAction((event) -> {
            saveShip(shipCargo.getText(), shipRange.getText(), shipCredits.getText());
        });
        shipRange.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                saveShip(shipCargo.getText(), shipRange.getText(), shipCredits.getText());
            }
        });
        
        shipCredits.setOnAction((event) -> {
            saveShip(shipCargo.getText(), shipRange.getText(), shipCredits.getText());
        });
        shipCredits.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                saveShip(shipCargo.getText(), shipRange.getText(), shipCredits.getText());
            }
        });

    }
    
    public void saveShip(String cargo, String jumpRange, String credits) {
        
        try {
            int iCargo = Integer.parseInt(cargo);
            float fJumpRange = Float.parseFloat(jumpRange);
            int iCredits = Integer.parseInt(credits);
            
            Ship s = new Ship(iCargo, fJumpRange, iCredits);
            shipService.saveShip(s);
        } catch (NumberFormatException | IOException e) {
            // ignore
        } 
    }

}
