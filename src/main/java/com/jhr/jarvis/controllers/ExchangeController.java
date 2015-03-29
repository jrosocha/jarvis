package com.jhr.jarvis.controllers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.jhr.jarvis.model.BestExchange;
import com.jhr.jarvis.model.StarSystem;
import com.jhr.jarvis.model.Station;
import com.jhr.jarvis.service.ShipService;
import com.jhr.jarvis.service.StarSystemService;
import com.jhr.jarvis.service.StationService;
import com.jhr.jarvis.service.TradeService;
import com.jhr.jarvis.util.FxUtil;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class ExchangeController {

    @FXML
    private Node view;
    
    @FXML 
    private VBox exchangesVbox;

    @FXML
    private ComboBox<String> fromSystemComboBox;
    
    @FXML
    private ComboBox<String> fromStationComboBox;
    
    @FXML
    private ComboBox<String> toSystemComboBox;
    
    @FXML
    private ComboBox<String> toStationComboBox;
    
    @Autowired
    private TradeService tradeService;
    
    @Autowired
    private ShipService shipService;
    
    @Autowired
    private StarSystemService starSystemService;
    
    @Autowired
    private StationService stationService;
    
    /**
     * Houses the systems list
     */
    private ObservableList<String> allSystems = FXCollections.observableArrayList();
    
    private ObservableList<String> toStation = FXCollections.observableArrayList();
    
    /**
     * Houses the results of an exchange search
     */
    private ObservableList<BestExchange> commodities = FXCollections.observableArrayList();       
    
    public Node getView() {
        return view;
    }
    
    @PostConstruct
    public void initilizeExchangeForm() {
        
        fromSystemComboBox.setItems(allSystems);
        FxUtil.autoCompleteComboBox(fromSystemComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        fromSystemComboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                String selectedSystem = FxUtil.getComboBoxValue(fromSystemComboBox);
                List<String> stations = getStationsBasedOnSystemSelection(selectedSystem);
                toStation.clear();
                toStation.addAll(stations);
            }
        });
        
        fromStationComboBox.setItems(toStation);
        FxUtil.autoCompleteComboBox(fromStationComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        
        toSystemComboBox.setItems(allSystems);
        FxUtil.autoCompleteComboBox(toSystemComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        
        populateSystems();
        toStation.clear();
        toStation.addAll(getStationsBasedOnSystemSelection(null));
    }
    
    public void x() {
        
        List<BestExchange> endOfRunTrades = new CopyOnWriteArrayList<>();
        List<BestExchange> sortedBestExchangeList = tradeService.tradeNOrientDb("CAREY STATION", null, shipService.getActiveShip(), 10, 2, endOfRunTrades);
        
        List<BestExchange> endOfRunTradesSorted = endOfRunTrades.parallelStream().sorted((a,b)->{ return Integer.compare(a.getRoutePerProfitUnit(), b.getRoutePerProfitUnit()); }).collect(Collectors.toList());
        endOfRunTradesSorted = Lists.reverse(endOfRunTradesSorted);
        int stopIdx = endOfRunTradesSorted.size() > 5 ? 5 : endOfRunTradesSorted.size();
        
        for (int tradeIndex = 0; tradeIndex < stopIdx; tradeIndex++) {
            
            List<BestExchange> path = tradeService.pathToExchange(endOfRunTradesSorted, tradeIndex);
            ObservableList<BestExchange> exchanges = FXCollections.observableArrayList();
            exchanges.addAll(path);
            
            TableView<BestExchange> exchangeTable = new TableView<>();
            exchangeTable.setMaxWidth(800);
            exchangeTable.setPrefWidth(800);
            
            TableColumn<BestExchange,Integer> stopNumber = new TableColumn<>("Leg");
            stopNumber.setPrefWidth(25);
            exchangeTable.getColumns().add(stopNumber);
            stopNumber.setCellValueFactory(column -> new SimpleIntegerProperty(column.getTableView().getItems().indexOf(column.getValue()) + 1).asObject());
            
            TableColumn<BestExchange,String> from = new TableColumn<>("From");
            from.setPrefWidth(200);
            exchangeTable.getColumns().add(from);
            from.setCellValueFactory(column -> new SimpleStringProperty( column.getValue().getBuyStationName() + "@" + column.getValue().getBuySystemName()) );
            
            
            TableColumn<BestExchange,String> to = new TableColumn<>("To");
            from.setPrefWidth(200);
            exchangeTable.getColumns().add(to);
            to.setCellValueFactory(column -> new SimpleStringProperty( column.getValue().getSellStationName() + "@" + column.getValue().getSellSystemName()) );
            
            TableColumn<BestExchange,String> commodity = new TableColumn<>("Commodity");
            commodity.setPrefWidth(125);
            exchangeTable.getColumns().add(commodity);
            commodity.setCellValueFactory(column ->column.getValue().getCommodityProperty());
            
            TableColumn<BestExchange,Integer> unitPrice = new TableColumn<>("Unit +");
            unitPrice.setPrefWidth(50);
            exchangeTable.getColumns().add(unitPrice);
            unitPrice.setCellValueFactory(column ->column.getValue().getPerUnitProfitProperty().asObject());
            
            TableColumn<BestExchange,Integer> routeProfit = new TableColumn<>("Route +");
            routeProfit.setPrefWidth(50);
            exchangeTable.getColumns().add(routeProfit);
            routeProfit.setCellValueFactory(column -> new SimpleIntegerProperty(column.getValue().getRoutePerProfitUnit() * column.getValue().getQuantity()).asObject());
            
            exchangeTable.setItems(exchanges);
            
            exchangesVbox.getChildren().add(exchangeTable);
        }
    }    
    
    public void populateSystems() {
        
        List<StarSystem> currentSystems = starSystemService.findSystemsOrientDb(null);
        List<String> systems = currentSystems.parallelStream().map(StarSystem::getName).sorted().collect(Collectors.toList());
        allSystems.clear();
        allSystems.addAll(systems);      
    }
    
    public List<String> getStationsBasedOnSystemSelection(String system) {
        List<Station> stations;
        if (StringUtils.isBlank(system)) {
             stations = stationService.findStationsOrientDb(null, false);
        } else {
            stations = stationService.getStationsForSystemOrientDb(system);
        }
        List<String> stationsAsStrings = stations.parallelStream().map(Station::getName).sorted().collect(Collectors.toList());
        
        return stationsAsStrings;
    }

    
}
