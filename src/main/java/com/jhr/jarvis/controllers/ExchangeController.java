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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.jhr.jarvis.model.BestExchange;
import com.jhr.jarvis.model.Commodity;
import com.jhr.jarvis.model.StarSystem;
import com.jhr.jarvis.model.Station;
import com.jhr.jarvis.service.CommodityService;
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
    
//    @FXML
//    private ComboBox<String> commodityComboBox;
   
    @FXML
    private ComboBox<Integer> numberOfTradesComboBox;
    
    @FXML
    private ComboBox<Integer> numberOfJumpsBetweenStationsComboBox;
    
    @FXML
    private Button searchButton;
    
    @Autowired
    private TradeService tradeService;
    
    @Autowired
    private ShipService shipService;
    
    @Autowired
    private StarSystemService starSystemService;
    
    @Autowired
    private StationService stationService;
    
    @Autowired
    private CommodityService commodityService;
    
    private ObservableList<String> allSystems = FXCollections.observableArrayList();
    
//    private ObservableList<String> allCommodities = FXCollections.observableArrayList();
    
    private ObservableList<String> fromStation = FXCollections.observableArrayList();
    
    private ObservableList<String> toStation = FXCollections.observableArrayList();
    
    private ObservableList<Integer> numberOfTradesOptions = FXCollections.observableArrayList(0, 1, 2, 3);
    
    private ObservableList<Integer> numberOfJumpsBetweenStationsOptions = FXCollections.observableArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ,10);
    
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
                fromStation.clear();
                fromStation.addAll(stations);
            }
        });
        
        fromStationComboBox.setItems(fromStation);
        FxUtil.autoCompleteComboBox(fromStationComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        
        toSystemComboBox.setItems(allSystems);
        FxUtil.autoCompleteComboBox(toSystemComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        toSystemComboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                String selectedSystem = FxUtil.getComboBoxValue(toSystemComboBox);
                List<String> stations = getStationsBasedOnSystemSelection(selectedSystem);
                toStation.clear();
                toStation.addAll(stations);
            }
        });
        
        toStationComboBox.setItems(toStation);
        FxUtil.autoCompleteComboBox(toStationComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        
//        commodityComboBox.setItems(allCommodities);
//        FxUtil.autoCompleteComboBox(commodityComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        
        numberOfTradesComboBox.setItems(numberOfTradesOptions);
        FxUtil.autoCompleteComboBox(numberOfTradesComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        
        numberOfJumpsBetweenStationsComboBox.setItems(numberOfJumpsBetweenStationsOptions);
        FxUtil.autoCompleteComboBox(numberOfJumpsBetweenStationsComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        
        searchButton.setOnAction((event) -> {
            search();
        });
        
        populateSystems();
        //populateCommodities();
        List<String> allStations = getStationsBasedOnSystemSelection(null);
        fromStation.clear();
        fromStation.addAll(allStations);
        toStation.clear();
        toStation.addAll(allStations);
        
    }
    
    public void search() {
        
        exchangesVbox.getChildren().clear();
        
        String fromSystem = FxUtil.getComboBoxValue(fromSystemComboBox);
        String fromStation = FxUtil.getComboBoxValue(fromStationComboBox);
        String toSystem = FxUtil.getComboBoxValue(toSystemComboBox);
        String toStation = FxUtil.getComboBoxValue(toStationComboBox);
        Integer numberOfTrades = FxUtil.getComboBoxValue(numberOfTradesComboBox);
        Integer numberOfJumpsBetweenStations = FxUtil.getComboBoxValue(numberOfJumpsBetweenStationsComboBox);
        
        // crazy state machine to determine type of search. 
        
        if (StringUtils.isNotBlank(fromStation)
                && numberOfTrades != null && numberOfTrades > 1
                && numberOfJumpsBetweenStations != null && numberOfJumpsBetweenStations > 0
                && StringUtils.isBlank(toSystem)
                && StringUtils.isBlank(toStation)) {
            
            List<BestExchange> endOfRunTrades = new CopyOnWriteArrayList<>();
            List<BestExchange> sortedBestExchangeList = tradeService.tradeNOrientDb(fromStation, null, shipService.getActiveShip(), numberOfJumpsBetweenStations, numberOfTrades, endOfRunTrades);
            multistopTrade(endOfRunTrades);
        } else if (StringUtils.isNotBlank(fromStation)
                && numberOfTrades != null && numberOfTrades == 1
                && numberOfJumpsBetweenStations != null && numberOfJumpsBetweenStations > 0
                && StringUtils.isBlank(toSystem)
                && StringUtils.isBlank(toStation)) {
            
            List<BestExchange> endOfRunTrades = new CopyOnWriteArrayList<>();
            List<BestExchange> sortedBestExchangeList = tradeService.tradeNOrientDb(fromStation, null, shipService.getActiveShip(), numberOfJumpsBetweenStations, numberOfTrades, endOfRunTrades);
            singleStopTrade(sortedBestExchangeList);
            
        }
        
    }
    
    public void singleStopTrade(List<BestExchange> sortedBestExchangeList) {
 
            ObservableList<BestExchange> exchanges = FXCollections.observableArrayList();            
            exchanges.addAll(sortedBestExchangeList);
            
            TableView<BestExchange> exchangeTable = new TableView<>();
            exchangeTable.setMaxWidth(800);
            exchangeTable.setPrefWidth(800);
            
            TableColumn<BestExchange,Integer> stopNumber = new TableColumn<>("#");
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
    
    public void multistopTrade(List<BestExchange> endOfRunTrades) {
        
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
    
//    public void populateCommodities() {
//        List<Commodity> commodities = commodityService.findCommoditiesOrientDb(null);
//        List<String> commoditiesAsStrings = commodities.parallelStream().map(Commodity::getName).sorted().collect(Collectors.toList());
//        allCommodities.clear();
//        allCommodities.addAll(commoditiesAsStrings);
//    }

    
}
