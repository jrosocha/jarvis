package com.jhr.jarvis.controllers;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;

import com.jhr.jarvis.event.ConsoleEvent;
import com.jhr.jarvis.event.EddnMessageQueueModifiedEvent;
import com.jhr.jarvis.event.ExchangeCompletedEvent;
import com.jhr.jarvis.event.OcrCompletedEvent;
import com.jhr.jarvis.event.ShipModifiedEvent;
import com.jhr.jarvis.model.BestExchange;
import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.model.Ship;
import com.jhr.jarvis.service.EddnService;
import com.jhr.jarvis.service.EliteOcrService;
import com.jhr.jarvis.service.ShipService;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class CommandsController implements ApplicationListener<ApplicationEvent> {
    
    @FXML
    private Node view;
    
    @FXML
    private Button ocrButton;
    
    @FXML
    private ProgressIndicator ocrProgress;
    
    @FXML
    private Button eddnButton;
    
    @FXML
    private Label eddnLastModified;
    
    @FXML
    private ProgressIndicator eddnProgress;
    
    @FXML
    private CheckBox eddnAuto;
    
    @Autowired
    private EliteOcrService eliteOcrService;

    @Autowired
    private EddnService eddnService;
    
    @Autowired 
    private Settings settings;
    
    public Node getView() {
        return view;
    }
    
    @PostConstruct
    public void initController() {
        ocrProgress.setVisible(false);
        eddnProgress.setVisible(false);
        
        ocrButton.setOnAction((event) -> {
            ocrProgress.setVisible(true);
            runOcrImport();
        });
        
        eddnButton.setOnAction((event) -> {
            eddnProgress.setVisible(true);
            runEddnImport();
        });
        
        eddnAuto.setSelected(eddnService.isAutoProcess());
        eddnAuto.setOnAction((event)->{
            event.getSource();
            eddnService.setAutoProcess(!eddnService.isAutoProcess());
        });
    }
    
    public void runOcrImport() {
    
        Runnable task = () -> {             
            try {
                eliteOcrService.scanDirectoryForOrientDb(settings.isEliteOcrScanArchiveEnabed());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(task).start();
        
    }

    public void runEddnImport() {
        
        Runnable task = () -> {             
            eddnService.processMessageQueue();
        };
        new Thread(task).start();
        
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        
        if (event instanceof OcrCompletedEvent) {
            Platform.runLater(()->ocrProgress.setVisible(false));
            Platform.runLater(()->eddnProgress.setVisible(false));
        }
        
        if (event instanceof EddnMessageQueueModifiedEvent) {
            Platform.runLater(()-> { 
                int size = eddnService.getMessageQueueSize();
                eddnButton.setText(String.format("EDDN Import (%d)", size));
                if (size > 0) {
                    eddnButton.setStyle("-fx-background-color: lightgreen;");
                } else {
                    eddnButton.setStyle(null);
                }
                eddnProgress.setVisible(false);
                eddnLastModified.setText(eddnService.getLastMessageReceived().format(DateTimeFormatter.ISO_LOCAL_TIME));
            });
            
        }
        
        
    }
    

}
