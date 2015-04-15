package com.jhr.jarvis.controllers;

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
    
    private ApplicationEventPublisher eventPublisher;
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        
        if (event instanceof CurrentSystemChangedEvent) {
            CurrentSystemChangedEvent currentSystemChangedEvent = (CurrentSystemChangedEvent) event;
            if (event.getSource() != null) {
                starSystemService.setCurrentStarSystem(currentSystemChangedEvent.getStarSystem());
                Platform.runLater(new UpdateCurrentSystem(currentSystemChangedEvent.getStarSystem()));
            }
        }
        
        if (event instanceof OcrCompletedEvent) {
            Platform.runLater(()->{
                String starSystemName = starSystemService.getCurrentStarSystem() != null ? starSystemService.getCurrentStarSystem().getName() : null;
                populateSystems();
                if (starSystemName != null) {
                    StarSystem reloadedStarSystem;
                    try {
                        reloadedStarSystem = starSystemService.findExactSystemAndStationsOrientDb(starSystemName);
                        eventPublisher.publishEvent(new CurrentSystemChangedEvent(reloadedStarSystem));
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
    
    private class UpdateCurrentSystem implements Runnable {
        
        private StarSystem starSystem;
        
        public UpdateCurrentSystem(StarSystem starSystem) {
            this.starSystem = starSystem;
        }
        
        @Override
        public void run() {
            stations.clear();
            stations.addAll(starSystem.getStations());
            stationTable.setItems(stations);
            currentSystemComboBox.getSelectionModel().select(starSystem.getName());
        }
    }
    
    @PostConstruct
    private void initController() {
        
        currentSystemComboBox.setItems(allSystems);
        FxUtil.autoCompleteComboBox(currentSystemComboBox, FxUtil.AutoCompleteMode.STARTS_WITH);
        
        currentSystemComboBox.setOnAction((event) -> {
            currentSystemComboBoxTimer.cancel();
            currentSystemComboBoxTimer = new Timer();
            currentSystemComboBoxTimer.schedule(new CurrentSystemComboBoxAutocompleteTask(), 200);            
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
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    class CurrentSystemComboBoxAutocompleteTask extends TimerTask {
        public void run() {
            
            String selectedSystem = FxUtil.getComboBoxValue(currentSystemComboBox);
            if (currentSystemComboBox.getItems().contains(selectedSystem)) {
                try {
                    StarSystem starSystem = starSystemService.findExactSystemAndStationsOrientDb(selectedSystem);
                    eventPublisher.publishEvent(new CurrentSystemChangedEvent(starSystem));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        }
    }
    
}
