package com.jhr.jarvis.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;

import com.google.common.collect.Lists;
import com.jhr.jarvis.event.CurrentSystemChangedEvent;
import com.jhr.jarvis.event.ExchangeCompletedEvent;
import com.jhr.jarvis.event.StationOverviewChangedEvent;
import com.jhr.jarvis.exceptions.StationNotFoundException;
import com.jhr.jarvis.exceptions.SystemNotFoundException;
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
public class ExchangeController implements ApplicationListener<ApplicationEvent>, ApplicationEventPublisherAware {

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
    
    //@FXML
    //private ComboBox<String> commodityComboBox;
    
    //@FXML
    //private ComboBox<String> buyOrSellComboBox;
   
    @FXML
    private ComboBox<Integer> numberOfTradesComboBox;
    
    @FXML
    private ComboBox<Integer> numberOfJumpsBetweenStationsComboBox;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private ProgressIndicator searchProgress;
    
    @Autowired
    private TradeService tradeService;
    
    @Autowired
    private ShipService shipService;
    
    @Autowired
    private StarSystemService starSystemService;
    
    @Autowired
    private StationService stationService;
    
    //@Autowired
    //private CommodityService commodityService;
    
   
    
    private ObservableList<String> allSystems = FXCollections.observableArrayList();
    
    //private ObservableList<String> allCommodities = FXCollections.observableArrayList();
    
    private ObservableList<String> fromStation = FXCollections.observableArrayList();
    
    private ObservableList<String> toStation = FXCollections.observableArrayList();
    
    private ObservableList<Integer> numberOfTradesOptions = FXCollections.observableArrayList(0, 1, 2, 3);
    
    private ObservableList<Integer> numberOfJumpsBetweenStationsOptions = FXCollections.observableArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ,10);

    //private ObservableList<BestExchange> commodities = FXCollections.observableArrayList();       
    
    private ApplicationEventPublisher eventPublisher;
    
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
                System.out.println("getting stations for " + selectedSystem);
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
        
        //commodityComboBox.setItems(allCommodities);
        //FxUtil.autoCompleteComboBox(commodityComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        
        numberOfTradesComboBox.setItems(numberOfTradesOptions);
        FxUtil.autoCompleteComboBox(numberOfTradesComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        numberOfTradesComboBox.getSelectionModel().select(1);
        
        numberOfJumpsBetweenStationsComboBox.setItems(numberOfJumpsBetweenStationsOptions);
        FxUtil.autoCompleteComboBox(numberOfJumpsBetweenStationsComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        numberOfJumpsBetweenStationsComboBox.getSelectionModel().select(1);
        
        searchProgress.setVisible(false);
        searchButton.setOnAction((event) -> {
            searchProgress.setVisible(true);
            search();
        });
        
        cancelButton.setOnAction((event) -> {
            initilizeExchangeForm();
        });
        
        //ObservableList<String> options = FXCollections.observableArrayList();
        //options.add("buy");
        //options.add("sell");
        //buyOrSellComboBox.setItems(options);
        //FxUtil.autoCompleteComboBox(buyOrSellComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        
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
        //String buyOrSell = FxUtil.getComboBoxValue(buyOrSellComboBox);
        //String commodity = FxUtil.getComboBoxValue(commodityComboBox);
        
        // crazy state machine to determine type of search. 
        
        if (StringUtils.isNotBlank(fromStation)
                && numberOfTrades != null && numberOfTrades > 1
                && numberOfJumpsBetweenStations != null && numberOfJumpsBetweenStations > 0
                && StringUtils.isBlank(toSystem)
                && StringUtils.isBlank(toStation)) {
            /*
             * Multiple stop trade
             */
            Runnable task = () -> { 
                List<BestExchange> endOfRunTrades = new CopyOnWriteArrayList<>();
                tradeService.tradeNOrientDb(fromStation, null, shipService.getActiveShip(), numberOfJumpsBetweenStations, numberOfTrades, endOfRunTrades);
                eventPublisher.publishEvent(new ExchangeCompletedEvent(endOfRunTrades, false));
            };
            new Thread(task).start();
            
        } else if (StringUtils.isNotBlank(fromStation)
                && numberOfTrades != null && numberOfTrades == 1
                && numberOfJumpsBetweenStations != null && numberOfJumpsBetweenStations > 0
                && StringUtils.isBlank(toSystem)
                && StringUtils.isBlank(toStation)) {
            /*
             * Single stop trade
             */
            Runnable task = () -> { 
                List<BestExchange> endOfRunTrades = new CopyOnWriteArrayList<>();
                List<BestExchange> sortedBestExchangeList = tradeService.tradeNOrientDb(fromStation, null, shipService.getActiveShip(), numberOfJumpsBetweenStations, numberOfTrades, endOfRunTrades);
                eventPublisher.publishEvent(new ExchangeCompletedEvent(sortedBestExchangeList, true));
            };
            new Thread(task).start();
            
        } else if (StringUtils.isNotBlank(toStation) 
                && StringUtils.isNotBlank(fromStation)) {
            /*
             * Station to Station Trade 
             */
            Runnable task = () -> { 
                List<BestExchange> bestExchangeList = new ArrayList<>();
                try {
                    bestExchangeList = tradeService.stationToStation(stationService.findExactStationOrientDb(fromStation), stationService.findExactStationOrientDb(toStation), shipService.getActiveShip());
                } catch (StationNotFoundException e) {
                    e.printStackTrace();
                }
                List<BestExchange> sortedBestExchangeList =  bestExchangeList.parallelStream().sorted((a,b)->{ return Integer.compare(a.getPerUnitProfit(), b.getPerUnitProfit()); }).collect(Collectors.toList());
                sortedBestExchangeList = Lists.reverse(sortedBestExchangeList);
                eventPublisher.publishEvent(new ExchangeCompletedEvent(sortedBestExchangeList, true));
            };
            new Thread(task).start();
        }

    }
    
    public void singleStopTrade(List<BestExchange> sortedBestExchangeList) {
 
            ObservableList<BestExchange> exchanges = FXCollections.observableArrayList();            
            exchanges.addAll(sortedBestExchangeList);
            
            Pane paddingPane = new Pane();           
            TableView<BestExchange> exchangeTable = new TableView<>();           
            exchangeTable.setLayoutX(5);
            exchangeTable.setLayoutY(5);
            exchangeTable.setMaxWidth(790);
            exchangeTable.setPrefWidth(790);
            exchangeTable.setPrefHeight(715);
            exchangeTable.setMaxHeight(Integer.MAX_VALUE);
            paddingPane.getChildren().add(exchangeTable);
            
//            exchangeTable.setRowFactory(new Callback<TableView<BestExchange>, TableRow<BestExchange>>() {  
//                @Override  
//                public TableRow<BestExchange> call(TableView<BestExchange> tableView) {  
//                    final TableRow<BestExchange> row = new TableRow<>();  
//                    final ContextMenu contextMenu = new ContextMenu();  
//                    final MenuItem menuItem = new MenuItem("Test");  
//                    menuItem.setOnAction(new EventHandler<ActionEvent>() {  
//                        @Override  
//                        public void handle(ActionEvent event) {  
//                            System.out.println("row something something " + row.getItem());
//                        }  
//                    });  
//                    contextMenu.getItems().add(menuItem);  
//                   // Set context menu on row, but use a binding to make it only show for non-empty rows:  
//                    row.contextMenuProperty().bind(  
//                            Bindings.when(row.emptyProperty())  
//                            .then((ContextMenu)null)  
//                            .otherwise(contextMenu)  
//                    );  
//                    return row ;  
//                }  
//            });  
            
            TableColumn<BestExchange,Integer> stopNumber = new TableColumn<>("#");
            stopNumber.setPrefWidth(25);
            exchangeTable.getColumns().add(stopNumber);
            stopNumber.setCellValueFactory(column -> new SimpleIntegerProperty(column.getTableView().getItems().indexOf(column.getValue()) + 1).asObject());
            
            TableColumn<BestExchange,String> from = new TableColumn<>("From");
            from.setPrefWidth(200);
            exchangeTable.getColumns().add(from);
            from.setCellValueFactory(column -> new SimpleStringProperty( column.getValue().getBuyStationName() + "@" + column.getValue().getBuySystemName()) );
            from.setCellFactory(new Callback<TableColumn<BestExchange, String>, TableCell<BestExchange, String>>() {
                @Override
                public TableCell<BestExchange, String> call(TableColumn<BestExchange, String> col) {
                    final TableCell<BestExchange, String> cell = new TableCell<>();
                    cell.textProperty().bind(cell.itemProperty());
                    cell.itemProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> obs, String oldValue, String newValue) {
                            if (newValue != null) {
                                final ContextMenu cellMenu = new ContextMenu();
                                final MenuItem currentSystemMenuItem = new MenuItem("Make Current System");
                                final MenuItem stationDetailsMenuItem = new MenuItem("Station Details");
                                currentSystemMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        try {
                                            BestExchange bestExchange = (BestExchange) cell.getTableRow().getItem();
                                            StarSystem starSystem = starSystemService.findExactSystemOrientDb(bestExchange.getBuySystemName());
                                            List<Station> stations = stationService.getStationsForSystemOrientDb(starSystem.getName());
                                            starSystem.setStations(stations);
                                            eventPublisher.publishEvent(new CurrentSystemChangedEvent(starSystem));
                                        } catch (SystemNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                stationDetailsMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        try {
                                            BestExchange bestExchange = (BestExchange) cell.getTableRow().getItem();
                                            Station station = stationService.findExactStationOrientDb(bestExchange.getBuyStationName());
                                            eventPublisher.publishEvent(new StationOverviewChangedEvent(station));     
                                        } catch (StationNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                cellMenu.getItems().add(currentSystemMenuItem);
                                cellMenu.getItems().add(stationDetailsMenuItem);
                                cell.setContextMenu(cellMenu);
                            } else {
                                cell.setContextMenu(null);
                            }
                        }
                    });
                    return cell;
                }
            });
            
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
            
            TableColumn<BestExchange,Integer> routeProfit = new TableColumn<>("Gross +");
            routeProfit.setPrefWidth(50);
            exchangeTable.getColumns().add(routeProfit);
            routeProfit.setCellValueFactory(column -> new SimpleIntegerProperty(column.getValue().getRoutePerProfitUnit() * column.getValue().getQuantity()).asObject());
            
            TableColumn<BestExchange,Integer> haulCost = new TableColumn<>("Risk $");
            haulCost.setPrefWidth(50);
            exchangeTable.getColumns().add(haulCost);
            haulCost.setCellValueFactory(column -> new SimpleIntegerProperty(column.getValue().getBuyPrice() * column.getValue().getQuantity()).asObject());
            
            exchangeTable.setItems(exchanges);
            
            exchangesVbox.getChildren().add(paddingPane);
            searchProgress.setVisible(false);

    }  
    
    public void multistopTrade(List<BestExchange> endOfRunTrades) {
        
        List<BestExchange> endOfRunTradesSorted = endOfRunTrades.parallelStream().sorted((a,b)->{ return Integer.compare(a.getRoutePerProfitUnit(), b.getRoutePerProfitUnit()); }).collect(Collectors.toList());
        endOfRunTradesSorted = Lists.reverse(endOfRunTradesSorted);
        int stopIdx = endOfRunTradesSorted.size() > 5 ? 5 : endOfRunTradesSorted.size();
        
        for (int tradeIndex = 0; tradeIndex < stopIdx; tradeIndex++) {
            
            List<BestExchange> path = tradeService.pathToExchange(endOfRunTradesSorted, tradeIndex);
            ObservableList<BestExchange> exchanges = FXCollections.observableArrayList();
            exchanges.addAll(path);
            
            Pane paddingPane = new Pane();           
            TableView<BestExchange> exchangeTable = new TableView<>();           
            exchangeTable.setLayoutX(5);
            exchangeTable.setLayoutY(5);
            exchangeTable.setMaxWidth(790);
            exchangeTable.setPrefWidth(790);
            exchangeTable.setPrefHeight(715);
            exchangeTable.setMaxHeight(Integer.MAX_VALUE);
            paddingPane.getChildren().add(exchangeTable);
            
            TableColumn<BestExchange,Integer> stopNumber = new TableColumn<>("Leg");
            stopNumber.setPrefWidth(25);
            exchangeTable.getColumns().add(stopNumber);
            stopNumber.setCellValueFactory(column -> new SimpleIntegerProperty(column.getTableView().getItems().indexOf(column.getValue()) + 1).asObject());
            
            TableColumn<BestExchange,String> from = new TableColumn<>("From");
            from.setPrefWidth(200);
            exchangeTable.getColumns().add(from);
            from.setCellValueFactory(column -> new SimpleStringProperty( column.getValue().getBuyStationName() + "@" + column.getValue().getBuySystemName()) );
            from.setCellFactory(new Callback<TableColumn<BestExchange, String>, TableCell<BestExchange, String>>() {
                @Override
                public TableCell<BestExchange, String> call(TableColumn<BestExchange, String> col) {
                    final TableCell<BestExchange, String> cell = new TableCell<>();
                    cell.textProperty().bind(cell.itemProperty());
                    cell.itemProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> obs, String oldValue, String newValue) {
                            if (newValue != null) {
                                final ContextMenu cellMenu = new ContextMenu();
                                final MenuItem emailMenuItem = new MenuItem("Email");
                                emailMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        String emailAdd = cell.getItem();
                                        BestExchange person = (BestExchange) cell.getTableRow().getItem();
                                        System.out.println("Email " + person + " at " + emailAdd);
                                    }
                                });
                                cellMenu.getItems().add(emailMenuItem);
                                cell.setContextMenu(cellMenu);
                            } else {
                                cell.setContextMenu(null);
                            }
                        }
                    });
                    return cell;
                }
            });
            
            
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
            
            TableColumn<BestExchange,Integer> routeProfit = new TableColumn<>("Gross +");
            routeProfit.setPrefWidth(50);
            exchangeTable.getColumns().add(routeProfit);
            routeProfit.setCellValueFactory(column -> new SimpleIntegerProperty(column.getValue().getRoutePerProfitUnit() * column.getValue().getQuantity()).asObject());
            
            TableColumn<BestExchange,Integer> haulCost = new TableColumn<>("Risk $");
            haulCost.setPrefWidth(50);
            exchangeTable.getColumns().add(haulCost);
            haulCost.setCellValueFactory(column -> new SimpleIntegerProperty(column.getValue().getBuyPrice() * column.getValue().getQuantity()).asObject());
            
            exchangeTable.setItems(exchanges);
            
            exchangesVbox.getChildren().add(paddingPane);
            searchProgress.setVisible(false);
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
    
//  public void populateCommodities() {
//      List<Commodity> commodities = commodityService.findCommoditiesOrientDb(null);
//      List<String> commoditiesAsStrings = commodities.parallelStream().map(Commodity::getName).sorted().collect(Collectors.toList());
//      allCommodities.clear();
//      allCommodities.addAll(commoditiesAsStrings);
//  }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // TODO Auto-generated method stub
        if (event instanceof ExchangeCompletedEvent) {
            ExchangeCompletedEvent exchangeCompletedEvent = (ExchangeCompletedEvent) event;
            if (exchangeCompletedEvent.getSingleStop()) {
               Platform.runLater(()->singleStopTrade(exchangeCompletedEvent.getExchanges()));
            } else {
               Platform.runLater(()->multistopTrade(exchangeCompletedEvent.getExchanges()));
            }
        }
        
        if (event instanceof ExchangeCompletedEvent) {
            Platform.runLater(()->initilizeExchangeForm());
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
    
}
