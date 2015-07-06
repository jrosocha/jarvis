package com.jhr.jarvis.controllers;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

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
    private Button csvImportDirectoryBrowseButton;
    
    @FXML
    private TextField csvImportDirectory;
    
    @FXML
    private CheckBox csvImportArchiveEnabed;
    
    @FXML
    private TextField longestDistanceEdge;
    
    @FXML
    private TextField closeSystemDistance;

    @FXML
    private Button eliteDangerousAppDirectoryButton;
    
    @FXML
    private TextField eliteDangerousAppDirectory;
    
    @FXML
    private Button saveConfigButton;
    
    @FXML
    private Button getSystemsEddnButton;
    
    @FXML
    private ProgressIndicator getSystemsEddnLoading;
    
    
    public Node getView() {
        return view;
    }
    
    
    @PostConstruct
    public void initController() {
        
        loadSettings();
        
        csvImportDirectoryBrowseButton.setOnAction((event) -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(settings.getEliteOcrScanDirectory()));
            
            File selectedDirectory = 
                    directoryChooser.showDialog(view.getScene().getWindow());
            
            if(selectedDirectory == null){
                csvImportDirectory.setText(settings.getEliteOcrScanDirectory());
            }else{
                csvImportDirectory.setText(selectedDirectory.getAbsolutePath());
            }
            
        });
        
        eliteDangerousAppDirectoryButton.setOnAction((event) -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(settings.getEliteDangerousAppDirectory()));
            
            File selectedDirectory = 
                    directoryChooser.showDialog(view.getScene().getWindow());
            
            if(selectedDirectory == null){
                eliteDangerousAppDirectory.setText(settings.getEliteDangerousAppDirectory());
            }else{
                eliteDangerousAppDirectory.setText(selectedDirectory.getAbsolutePath());
            }
            
        });
        
        saveConfigButton.setOnAction((event) -> {
            saveSettings();
            loadSettings();
        });
        
        getSystemsEddnButton.setOnAction((event) -> {            
                getSystemsEddnLoading.setVisible(true);               
                Runnable task = () -> {             
                    try {
                        File systemsFile = new File(settings.getSystemsFile());
                        starSystemService.getLatestSystemsFileFromEddb(systemsFile);
                        starSystemService.loadSystemsV2(systemsFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        getSystemsEddnLoading.setVisible(false);
                    }

                };
                new Thread(task).start();                
        });
        
        getSystemsEddnLoading.setVisible(false);
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
        csvImportDirectory.setText(settings.getEliteOcrScanDirectory());
        csvImportArchiveEnabed.setSelected(settings.isEliteOcrScanArchiveEnabed());
        
    }
    
    public void saveSettings() {

        settings.setOrientGraphDb(orientGraphDb.getText());
        settings.setSystemsFile(systemsFile.getText());
        settings.setCommodityFile(commodityFile.getText());
        settings.setShipFile(shipFile.getText());
        settings.setAvoidStationsFile(avoidStationsFile.getText());
        settings.setEliteOcrScanDirectory(csvImportDirectory.getText());
        settings.setLongestDistanceEdge(Integer.parseInt(longestDistanceEdge.getText()));
        settings.setCloseSystemDistance(Integer.parseInt(closeSystemDistance.getText()));
        settings.setEliteDangerousAppDirectory(eliteDangerousAppDirectory.getText());
        settings.setEliteOcrScanArchiveEnabed(csvImportArchiveEnabed.isSelected());
        try {
            settings.saveSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
