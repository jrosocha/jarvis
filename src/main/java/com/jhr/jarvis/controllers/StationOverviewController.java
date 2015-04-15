package com.jhr.jarvis.controllers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;

import com.jhr.jarvis.controllers.CurrentSystemController.CurrentSystemComboBoxAutocompleteTask;
import com.jhr.jarvis.event.CurrentSystemChangedEvent;
import com.jhr.jarvis.event.OcrCompletedEvent;
import com.jhr.jarvis.event.StationOverviewChangedEvent;
import com.jhr.jarvis.exceptions.StationNotFoundException;
import com.jhr.jarvis.model.Commodity;
import com.jhr.jarvis.model.StarSystem;
import com.jhr.jarvis.model.Station;
import com.jhr.jarvis.service.StationService;
import com.jhr.jarvis.util.FxUtil;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class StationOverviewController implements ApplicationListener<ApplicationEvent>, ApplicationEventPublisherAware {

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
    private ComboBox<String> stationComboBox;
    private Timer stationComboBoxTimer = new Timer();
    
    @FXML
    private CheckBox blackMarketCheckBox;
    @FXML
    private Label ageOfDataLabel;
    @FXML
    private Button deleteStationButton;
    
    private ObservableList<String> stations = FXCollections.observableArrayList();
    
    private ObservableList<Commodity> commodities = FXCollections.observableArrayList();
    
    @Autowired
    private StationService stationService;
    
    private ApplicationEventPublisher eventPublisher;
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        
        if (event instanceof StationOverviewChangedEvent) {        
            if (event.getSource() != null) {
                Platform.runLater(new UpdateStationOverview((Station) event.getSource()));
            }
        }
        
        if (event instanceof OcrCompletedEvent) {        
            if (event.getSource() != null) {
                Platform.runLater(()->{
                    String selectedStation = FxUtil.getComboBoxValue(stationComboBox);
                    populateStations();
                    if (StringUtils.isNotBlank(selectedStation)) {
                        try {
                            Station station = stationService.findExactStationOrientDb(selectedStation);                    
                            eventPublisher.publishEvent(new StationOverviewChangedEvent(station));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
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
            stationComboBox.getSelectionModel().select(station.getName());
            blackMarketCheckBox.setSelected(station.getBlackMarket() != null && station.getBlackMarket() ? true : false);
            ageOfDataLabel.setText(ChronoUnit.DAYS.between(station.getDate(), LocalDateTime.now()) + "");
        }
    }
    
    @PostConstruct
    private void initController() {
        
        commodityTypeColumn.setCellValueFactory(column ->column.getValue().getGroupProperty());
        commodityNameColumn.setCellValueFactory(column -> column.getValue().getNameProperty());
        commodityBuyColumn.setCellValueFactory(column -> column.getValue().getBuyPriceProperty().asObject());
        commoditySupplyColumn.setCellValueFactory(column -> column.getValue().getSupplyProperty().asObject());
        commoditySellColumn.setCellValueFactory(column -> column.getValue().getSellPriceProperty().asObject());
        commodityDemandColumn.setCellValueFactory(column -> column.getValue().getDemandProperty().asObject());
        commodityTable.setItems(commodities);   
        
        blackMarketCheckBox.setOnAction((event) -> {
            updateBlackMarketStatus(blackMarketCheckBox.isSelected());
        });
        
        stationComboBox.setItems(stations);
        FxUtil.autoCompleteComboBox(stationComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        stationComboBox.setOnAction((event)->{
            stationComboBoxTimer.cancel();
            stationComboBoxTimer = new Timer();
            stationComboBoxTimer.schedule(new StationComboBoxAutocompleteTask(), 200);   
        });
 
        populateStations();
    }
    
    public void updateBlackMarketStatus(Boolean blackMarketAvailable) {
        Station station = null;
        try {
            station = stationService.findExactStationOrientDb(FxUtil.getComboBoxValue(stationComboBox));
            stationService.addPropertyToStationOrientDb(station, "blackMarket", blackMarketAvailable);
            station = stationService.findExactStationOrientDb(FxUtil.getComboBoxValue(stationComboBox));
            Platform.runLater(new UpdateStationOverview(station));
        } catch (StationNotFoundException e) {
            e.printStackTrace();
        }
        
    }
    
    public void populateStations() {
        List<Station> stationsDb = stationService.findStationsOrientDb(null, false);
        List<String> stationsAsStrings = stationsDb.parallelStream().map(Station::getName).sorted().collect(Collectors.toList());
        stations.clear();
        stations.addAll(stationsAsStrings);
    }
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
    
    class StationComboBoxAutocompleteTask extends TimerTask {
        public void run() {            
            String selectedStation = FxUtil.getComboBoxValue(stationComboBox);
            if (stationComboBox.getItems().contains(selectedStation)) {
                try {
                     Station station = stationService.findExactStationOrientDb(selectedStation);                    
                     eventPublisher.publishEvent(new StationOverviewChangedEvent(station));
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
            }
        }
    }

}
