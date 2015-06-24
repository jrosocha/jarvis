package com.jhr.jarvis.controllers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;

import com.jhr.jarvis.event.CurrentSystemChangedEvent;
import com.jhr.jarvis.event.ExchangeStationChangedEvent;
import com.jhr.jarvis.event.OcrCompletedEvent;
import com.jhr.jarvis.event.StationOverviewChangedEvent;
import com.jhr.jarvis.exceptions.StationNotFoundException;
import com.jhr.jarvis.exceptions.SystemNotFoundException;
import com.jhr.jarvis.model.BestExchange;
import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.model.StarSystem;
import com.jhr.jarvis.model.Station;
import com.jhr.jarvis.service.StarSystemService;
import com.jhr.jarvis.service.StationService;
import com.jhr.jarvis.util.FxUtil;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class CurrentSystemController implements ApplicationListener<ApplicationEvent>, ApplicationEventPublisherAware {

    @Autowired
    private StationService stationService;
    
    @Autowired
    private StarSystemService starSystemService;
    
    @Autowired
    private Settings settings;
    
    @FXML
    private Node view;
    
    @FXML
    private TableView<Station> stationTable;
    
    @FXML
    private TableColumn<Station, String> stationNameColumn;
    
    @FXML
    private TableColumn<Station, Boolean> stationBlackMarketFlagColumn;
    
    @FXML
    private TableColumn<Station, LocalDateTime> stationDataAgeColumn;
    
    @FXML
    private ComboBox<String> currentSystemComboBox;
    private Timer currentSystemComboBoxTimer = new Timer();
    
    private ObservableList<String> allSystems = FXCollections.observableArrayList();
    
    private ObservableList<Station> stations = FXCollections.observableArrayList();
    
    @FXML
    private ComboBox<String> allegianceComboBox;
    private ObservableList<String> allegiance = FXCollections.observableArrayList();
    
    @FXML
    private ComboBox<String> governmentComboBox;
    private ObservableList<String> government = FXCollections.observableArrayList();
    
    @FXML
    private ComboBox<String> primaryEconomyComboBox;
    private ObservableList<String> primaryEconomy = FXCollections.observableArrayList();
    
    @FXML
    private ComboBox<String> secondaryEconomyComboBox;
    private ObservableList<String> secondaryEconomy = FXCollections.observableArrayList();
    
    private ApplicationEventPublisher eventPublisher;
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        
        if (event instanceof CurrentSystemChangedEvent) {
            System.out.println("CurrentSystemChangedEvent received by CurrentSystemController.");
            CurrentSystemChangedEvent currentSystemChangedEvent = (CurrentSystemChangedEvent) event;
            if (event.getSource() != null) {
                StarSystem starSystem = currentSystemChangedEvent.getStarSystem();
                //starSystemService.setCurrentStarSystem(starSystem);
                System.out.println("Updating to: " + starSystem);
                Platform.runLater(()->{
                    stations.clear();
                    stations.addAll(starSystem.getStations());
                    stationTable.getItems().clear();
                    stationTable.getItems().addAll(stations);
                    currentSystemComboBox.getSelectionModel().select(starSystem.getName());
                    allegianceComboBox.getSelectionModel().select(starSystem.getAllegiance());
                    governmentComboBox.getSelectionModel().select(starSystem.getGovernment());
                    primaryEconomyComboBox.getSelectionModel().select(starSystem.getPrimaryEconomy());
                    secondaryEconomyComboBox.getSelectionModel().select(starSystem.getSecondaryEconomy());
                    
                });
            }
        }
        
        if (event instanceof OcrCompletedEvent) {
            Platform.runLater(()->{
                System.out.println("OcrCompletedEvent received by CurrentSystemController.");
                String starSystemName = currentSystemComboBox.getEditor().getText();
                System.out.println("Updating current system " + starSystemName);
                populateSystems();
                if (starSystemName != null) {
                    StarSystem reloadedStarSystem;
                    try {
                        reloadedStarSystem = starSystemService.findExactSystemAndStationsOrientDb(starSystemName, false);
                        System.out.println("Updating current system to " + reloadedStarSystem);
                        this.onApplicationEvent(new CurrentSystemChangedEvent(reloadedStarSystem));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    
    public Node getView() {
        return view;
    }
    
    @PostConstruct
    private void initController() {
        
        try {
            starSystemService.loadSystemsV2(new File(settings.getSystemsFile()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        currentSystemComboBox.setItems(allSystems);
        FxUtil.autoCompleteComboBox(currentSystemComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        
        this.allegianceComboBox.setItems(this.allegiance);
        allegianceComboBox.setOnAction((event)->{
            if (allegianceComboBox.getSelectionModel().getSelectedItem() != null) {
                try {
                    starSystemService.addPropertyToSystem(currentSystemComboBox.getEditor().getText().toUpperCase(), "allegiance", allegianceComboBox.getSelectionModel().getSelectedItem());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        this.governmentComboBox.setItems(this.government);
        governmentComboBox.setOnAction((event)->{
            if (governmentComboBox.getSelectionModel().getSelectedItem() != null) {
                try {
                    starSystemService.addPropertyToSystem(currentSystemComboBox.getEditor().getText().toUpperCase(), "government", governmentComboBox.getSelectionModel().getSelectedItem());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        this.primaryEconomyComboBox.setItems(this.primaryEconomy);
        primaryEconomyComboBox.setOnAction((event)->{
            if (primaryEconomyComboBox.getSelectionModel().getSelectedItem() != null) {
                try {
                    starSystemService.addPropertyToSystem(currentSystemComboBox.getEditor().getText().toUpperCase(), "primaryEconomy", primaryEconomyComboBox.getSelectionModel().getSelectedItem());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        this.secondaryEconomyComboBox.setItems(this.secondaryEconomy);
        secondaryEconomyComboBox.setOnAction((event)->{
            if (secondaryEconomyComboBox.getSelectionModel().getSelectedItem() != null) {
                try {
                    starSystemService.addPropertyToSystem(currentSystemComboBox.getEditor().getText().toUpperCase(), "secondaryEconomy", secondaryEconomyComboBox.getSelectionModel().getSelectedItem());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            try {
                System.out.println("Updated to->" + starSystemService.findExactSystemAndStationsOrientDb(currentSystemComboBox.getEditor().getText().toUpperCase(), false));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        
        currentSystemComboBox.setOnAction((event) -> {
            if (currentSystemComboBox.getSelectionModel().getSelectedItem() != null) {
                currentSystemComboBoxTimer.cancel();
                currentSystemComboBoxTimer = new Timer();
                currentSystemComboBoxTimer.schedule(new CurrentSystemComboBoxAutocompleteTask(), 200); 
            }
        });
        
        stationBlackMarketFlagColumn.setCellFactory( tableCell -> new CheckBoxTableCell<>());
        stationDataAgeColumn.setCellFactory( tableCell -> { return new TableCell<Station, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    if (item != null) {
                        setText(ChronoUnit.DAYS.between(item, LocalDateTime.now()) + "");
                    } else {
                        setText("");
                    }
                    
                }
            };
        });
        
        stationNameColumn.setCellValueFactory(column ->column.getValue().getNameProperty());
        stationBlackMarketFlagColumn.setCellValueFactory(column -> column.getValue().getBlackMarketProperty());
        stationDataAgeColumn.setCellValueFactory(column -> column.getValue().getDateProperty());
        
        stationTable.setRowFactory(new Callback<TableView<Station>, TableRow<Station>>() {  
          @Override  
          public TableRow<Station> call(TableView<Station> tableView) {  
              final TableRow<Station> row = new TableRow<>();  
              final ContextMenu contextMenu = new ContextMenu();  
              final MenuItem stationDetailsMenuItem = new MenuItem("Station Details");  
              stationDetailsMenuItem.setOnAction(new EventHandler<ActionEvent>() {  
                  @Override  
                  public void handle(ActionEvent event) {  
                      Station selectedStation = (Station) row.getItem();
                      loadNewStation(selectedStation.getName());
                  }  
              });  
              contextMenu.getItems().add(stationDetailsMenuItem);
              
              final MenuItem exchangeFromMenuItem = new MenuItem("Set As Exchange From");  
              exchangeFromMenuItem.setOnAction(new EventHandler<ActionEvent>() {  
                  @Override  
                  public void handle(ActionEvent event) {  
                      Station selectedStation = (Station) row.getItem();
                      eventPublisher.publishEvent(new ExchangeStationChangedEvent(selectedStation.getName(), ExchangeStationChangedEvent.FROM_OR_TO.FROM));
                  }  
              });  
              contextMenu.getItems().add(exchangeFromMenuItem);
              
              final MenuItem exchangeToMenuItem = new MenuItem("Set As Exchange To");  
              exchangeToMenuItem.setOnAction(new EventHandler<ActionEvent>() {  
                  @Override  
                  public void handle(ActionEvent event) {  
                      Station selectedStation = (Station) row.getItem();
                      eventPublisher.publishEvent(new ExchangeStationChangedEvent(selectedStation.getName(), ExchangeStationChangedEvent.FROM_OR_TO.TO));
                  }  
              });  
              contextMenu.getItems().add(exchangeToMenuItem);
              
             // Set context menu on row, but use a binding to make it only show for non-empty rows:  
              row.contextMenuProperty().bind(  
                      Bindings.when(row.emptyProperty())  
                      .then((ContextMenu)null)  
                      .otherwise(contextMenu)  
              );  
              return row ;  
          }  
      });
        
      populateSystems();
      populateSystemMetadata();
    }
    
    private void loadNewStation(String name) {
        
        try {
            Station station = stationService.findExactStationOrientDb(name);
            eventPublisher.publishEvent(new StationOverviewChangedEvent(station));
            
        } catch (StationNotFoundException e) {
            e.printStackTrace();
        }
        
    }
    
    public void populateSystems() {
        
        List<StarSystem> currentSystems = starSystemService.findSystemsOrientDb(null);
        List<String> systems = currentSystems.parallelStream().map(StarSystem::getName).sorted().collect(Collectors.toList());
        allSystems.clear();
        allSystems.addAll(systems);      
    }
    
    public void populateSystemMetadata() {
        
        this.allegiance.clear();
        this.allegiance.addAll(starSystemService.getAllegiances());
        this.government.clear();
        this.government.addAll(starSystemService.getGovernments());
        this.primaryEconomy.clear();
        this.primaryEconomy.addAll(starSystemService.getEconomies());
        this.secondaryEconomy.clear();
        this.secondaryEconomy.addAll(starSystemService.getEconomies());
           
    }
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    class CurrentSystemComboBoxAutocompleteTask extends TimerTask {
        public void run() {
            
            String selectedSystem = FxUtil.getComboBoxValue(currentSystemComboBox);
            if (currentSystemComboBox.getItems().contains(selectedSystem)) {
                try {
                    StarSystem starSystem = starSystemService.findExactSystemAndStationsOrientDb(selectedSystem, true);
                    eventPublisher.publishEvent(new CurrentSystemChangedEvent(starSystem));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        }
    }
    
}
