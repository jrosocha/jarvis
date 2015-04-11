package com.jhr.jarvis.controllers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;

import com.jhr.jarvis.event.CurrentSystemChangedEvent;
import com.jhr.jarvis.event.OcrCompletedEvent;
import com.jhr.jarvis.event.StationOverviewChangedEvent;
import com.jhr.jarvis.exceptions.StationNotFoundException;
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
                populateSystems();
                StarSystem starSystem = starSystemService.getCurrentStarSystem();
                if (starSystem != null) {
                    eventPublisher.publishEvent(new CurrentSystemChangedEvent(starSystem));
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
        currentSystemComboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                String selectedSystem = FxUtil.getComboBoxValue(currentSystemComboBox);
                try {
                    StarSystem starSystem = starSystemService.findExactSystemAndStationsOrientDb(selectedSystem);
                    eventPublisher.publishEvent(new CurrentSystemChangedEvent(starSystem));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        stationTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadNewStation(newValue.getName());
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

}
