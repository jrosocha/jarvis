package com.jhr.jarvis.controllers;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;

import com.jhr.jarvis.event.ConsoleEvent;
import com.jhr.jarvis.event.ExchangeCompletedEvent;
import com.jhr.jarvis.event.OcrCompletedEvent;
import com.jhr.jarvis.event.ShipModifiedEvent;
import com.jhr.jarvis.model.BestExchange;
import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.model.Ship;
import com.jhr.jarvis.service.EliteOcrService;
import com.jhr.jarvis.service.ShipService;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class CommandsController implements ApplicationListener<OcrCompletedEvent> {
    
    @FXML
    private Node view;
    
    @FXML
    private Button ocrButton;
    
    @FXML
    private ProgressIndicator ocrProgress;
    
    
    @Autowired
    private EliteOcrService eliteOcrService;
    
    @Autowired 
    private Settings settings;
    
    public Node getView() {
        return view;
    }
    
    @PostConstruct
    public void initController() {
        ocrProgress.setVisible(false);
        
        ocrButton.setOnAction((event) -> {
            ocrProgress.setVisible(true);
            runOcrImport();
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

    @Override
    public void onApplicationEvent(OcrCompletedEvent event) {
        Platform.runLater(()->ocrProgress.setVisible(false));
    }
    

}
