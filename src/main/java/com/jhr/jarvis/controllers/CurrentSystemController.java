package com.jhr.jarvis.controllers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;

import com.jhr.jarvis.event.CurrentSystemChangedEvent;
import com.jhr.jarvis.event.StationOverviewChangedEvent;
import com.jhr.jarvis.exceptions.StationNotFoundException;
import com.jhr.jarvis.model.StarSystem;
import com.jhr.jarvis.model.Station;
import com.jhr.jarvis.service.StationService;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class CurrentSystemController implements ApplicationListener<CurrentSystemChangedEvent>, ApplicationEventPublisherAware {

    @Autowired
    private StationService stationService;
    
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
    private Label systemNameLabel;
    
    private ObservableList<Station> stations = FXCollections.observableArrayList();
    
    private ApplicationEventPublisher eventPublisher;
    
    @Override
    public void onApplicationEvent(CurrentSystemChangedEvent event) {
        
        if (event.getSource() != null) {
            Platform.runLater(new UpdateCurrentSystem((StarSystem) event.getSource()));
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
            systemNameLabel.setText(starSystem.getName());
        }
    }
    
    @PostConstruct
    private void initTableCells() {
        
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
                (observable, oldValue, newValue) -> loadNewStation(newValue.getName()));        
    }
    
    private void loadNewStation(String name) {
        
        try {
            Station station = stationService.findExactStationOrientDb(name);
            eventPublisher.publishEvent(new StationOverviewChangedEvent(station));
            
        } catch (StationNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

}
