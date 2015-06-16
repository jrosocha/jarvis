package com.jhr.jarvis.controllers;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.service.StarSystemService;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class SettingsController {

    @Autowired
    private Settings settings;

    @Autowired
    private StarSystemService starSystemService;
    
    @FXML
    private Node view;
    
    @FXML
    private TextField orientGraphDb;

    @FXML
    private TextField systemsFile;
    
    @FXML
    private TextField commodityFile;
    
    @FXML
    private TextField shipFile;
    
    @FXML
    private TextField avoidStationsFile;
    
    @FXML
    private TextField eliteOcrScanDirectory;
    
    @FXML
    private CheckBox eliteOcrScanArchiveEnabed;
    
    @FXML
    private TextField longestDistanceEdge;
    
    @FXML
    private TextField closeSystemDistance;
    
    @FXML
    private TextField eliteDangerousAppDirectory;
    
    @FXML
    private Button saveConfigButton;
    
    @FXML
    private Button getSystemsEddnButton;
    
    public Node getView() {
        return view;
    }
    
    
    @PostConstruct
    public void initController() {
        
        loadSettings();
        saveConfigButton.setOnAction((event) -> {
            saveSettings();
            loadSettings();
        });
        
        getSystemsEddnButton.setOnAction((event) -> {
            try {
                File systemsFile = new File(settings.getSystemsFile());
                starSystemService.getLatestSystemsFileFromEddb(systemsFile);
                starSystemService.loadSystemsV2(systemsFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public void loadSettings() {
        
        orientGraphDb.setText(settings.getOrientGraphDb());
        systemsFile.setText(settings.getSystemsFile());
        commodityFile.setText(settings.getCommodityFile());
        shipFile.setText(settings.getShipFile());
        avoidStationsFile.setText(settings.getAvoidStationsFile());
        longestDistanceEdge.setText(settings.getLongestDistanceEdge() + "");
        closeSystemDistance.setText(settings.getCloseSystemDistance() + "");
        eliteDangerousAppDirectory.setText(settings.getEliteDangerousAppDirectory());
        eliteOcrScanDirectory.setText(settings.getEliteOcrScanDirectory());
        eliteOcrScanArchiveEnabed.setSelected(settings.isEliteOcrScanArchiveEnabed());
        
    }
    
    public void saveSettings() {

        settings.setOrientGraphDb(orientGraphDb.getText());
        settings.setSystemsFile(systemsFile.getText());
        settings.setCommodityFile(commodityFile.getText());
        settings.setShipFile(shipFile.getText());
        settings.setAvoidStationsFile(avoidStationsFile.getText());
        settings.setEliteOcrScanDirectory(eliteOcrScanDirectory.getText());
        settings.setLongestDistanceEdge(Integer.parseInt(longestDistanceEdge.getText()));
        settings.setCloseSystemDistance(Integer.parseInt(closeSystemDistance.getText()));
        settings.setEliteDangerousAppDirectory(eliteDangerousAppDirectory.getText());
        settings.setEliteOcrScanArchiveEnabed(eliteOcrScanArchiveEnabed.isSelected());
        try {
            settings.saveSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
