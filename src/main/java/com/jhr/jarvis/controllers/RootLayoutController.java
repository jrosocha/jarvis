package com.jhr.jarvis.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class RootLayoutController {

    @FXML
    private Node view;
    
    @FXML
    private VBox left;
    
    @FXML
    private Pane currentSystemPane;
    
    @FXML
    private Pane shipPane;
    
    @FXML
    private TabPane center;
    
    public Node getView() {
        return view;
    }

    public VBox getLeft() {
        return left;
    }

    public TabPane getCenter() {
        return center;
    }

    public Pane getCurrentSystemPane() {
        return currentSystemPane;
    }

    public void setCurrentSystemPane(Pane currentSystemPane) {
        this.currentSystemPane = currentSystemPane;
    }

    public Pane getShipPane() {
        return shipPane;
    }

    public void setShipPane(Pane shipPane) {
        this.shipPane = shipPane;
    }

}
