package com.jhr.jarvis.controllers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationListener;

import com.jhr.jarvis.event.StationOverviewChangedEvent;
import com.jhr.jarvis.model.Commodity;
import com.jhr.jarvis.model.Station;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class StationOverviewController implements ApplicationListener<StationOverviewChangedEvent> {

    @FXML
    private Node view;
    
    @FXML
    private TableView<Commodity> commodityTable;
    @FXML
    private TableColumn<Commodity, String> commodityTypeColumn;
    @FXML
    private TableColumn<Commodity, String> commodityNameColumn;
    @FXML
    private TableColumn<Commodity, Integer> commodityBuyColumn;
    @FXML
    private TableColumn<Commodity, Integer> commoditySupplyColumn;
    @FXML
    private TableColumn<Commodity, Integer> commoditySellColumn;
    @FXML
    private TableColumn<Commodity, Integer> commodityDemandColumn;
    
    
    @FXML
    private Label systemNameLabel;
    @FXML
    private Label stationNameLabel;
    @FXML
    private Label blackMarketLabel;
    @FXML
    private Label ageOfDataLabel;
    
    private ObservableList<Commodity> commodities = FXCollections.observableArrayList();
    
    
    @Override
    public void onApplicationEvent(StationOverviewChangedEvent event) {
        
        if (event.getSource() != null) {
            Platform.runLater(new UpdateStationOverview((Station) event.getSource()));
        }
    }
    
    public Node getView() {
        return view;
    }
    
    private class UpdateStationOverview implements Runnable {
        
        private Station station;
        
        public UpdateStationOverview(Station station) {
            this.station = station;
        }
        
        @Override
        public void run() {
            commodities.clear();
            commodities.addAll(station.getAvailableCommodityExchanges());
            
            
            systemNameLabel.setText(station.getSystem());
            stationNameLabel.setText(station.getName());
            blackMarketLabel.setText(station.getBlackMarket().toString());
            ageOfDataLabel.setText(ChronoUnit.DAYS.between(station.getDate(), LocalDateTime.now()) + "");
        }
    }
    
    @PostConstruct
    private void initTableCells() {
        
        commodityTypeColumn.setCellValueFactory(column ->column.getValue().getGroupProperty());
        commodityNameColumn.setCellValueFactory(column -> column.getValue().getNameProperty());
        commodityBuyColumn.setCellValueFactory(column -> column.getValue().getBuyPriceProperty().asObject());
        commoditySupplyColumn.setCellValueFactory(column -> column.getValue().getSupplyProperty().asObject());
        commoditySellColumn.setCellValueFactory(column -> column.getValue().getSellPriceProperty().asObject());
        commodityDemandColumn.setCellValueFactory(column -> column.getValue().getDemandProperty().asObject());
        commodityTable.setItems(commodities);   
    }

}
