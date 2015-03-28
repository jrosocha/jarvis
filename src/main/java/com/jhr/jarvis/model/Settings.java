package com.jhr.jarvis.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class Settings {

    @JsonIgnore
    private boolean loaded = false;
    
    private String orientGraphDb = null;
    
    private String systemsFile = null;
    
    private String commodityFile = null;
    
    private String shipFile = null;
    
    private String avoidStationsFile = null;
    
    private String eliteOcrScanDirectory = null;
    
    private boolean eliteOcrScanArchiveEnabed = false;
    
    private int longestDistanceEdge = 25;
    
    private int closeSystemDistance = 10;
    
    private String eliteDangerousAppDirectory = null;
     
    
    @JsonIgnore
    @Autowired
    private ObjectMapper objectMapper;
    
    @PostConstruct
    public void loadSettings() throws FileNotFoundException, IOException {
        
        File settings = new File("../data/jarvis-config.json");
        Settings fileSettings = null;
        
        try {
            fileSettings = objectMapper.readValue(settings, Settings.class);
        } catch (Exception e) {
            e.printStackTrace();
            // try developer file for load dev
            fileSettings = objectMapper.readValue(this.getClass().getResourceAsStream("/jarvis-dev-config.json"), Settings.class);
        }
        
        load(fileSettings);
        
        loaded = true;
    }
    
    private void load(Settings s) {
        BeanUtils.copyProperties(s, this);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * @return the loaded
     */
    public boolean isLoaded() {
        return loaded;
    }
    
    /**
     * @return the eliteOcrScanDirectory
     */
    public String getEliteOcrScanDirectory() {
        return eliteOcrScanDirectory;
    }

    /**
     * @param eliteOcrScanDirectory the eliteOcrScanDirectory to set
     */
    public void setEliteOcrScanDirectory(String eliteOcrScanDirectory) {
        this.eliteOcrScanDirectory = eliteOcrScanDirectory;
    }

    /**
     * @param systemsFile the systemsFile to set
     */
    public void setSystemsFile(String systemsFile) {
        this.systemsFile = systemsFile;
    }
    
    /**
     * @return the systemsFile
     */
    public String getSystemsFile() {
        return systemsFile;
    }
    
    /**
     * @return the eliteOcrScanArchiveEnabed
     */
    public boolean isEliteOcrScanArchiveEnabed() {
        return eliteOcrScanArchiveEnabed;
    }
    
    /**
     * @return the longestDistanceEdge
     */
    public int getLongestDistanceEdge() {
        return longestDistanceEdge;
    }

    /**
     * @param longestDistanceEdge the longestDistanceEdge to set
     */
    public void setLongestDistanceEdge(int longestDistanceEdge) {
        this.longestDistanceEdge = longestDistanceEdge;
    }

    /**
     * @return the closeSystemDistance
     */
    public int getCloseSystemDistance() {
        return closeSystemDistance;
    }

    /**
     * @param closeSystemDistance the closeSystemDistance to set
     */
    public void setCloseSystemDistance(int closeSystemDistance) {
        this.closeSystemDistance = closeSystemDistance;
    }

    /**
     * @param eliteOcrScanArchiveEnabed the eliteOcrScanArchiveEnabed to set
     */
    public void setEliteOcrScanArchiveEnabed(boolean eliteOcrScanArchiveEnabed) {
        this.eliteOcrScanArchiveEnabed = eliteOcrScanArchiveEnabed;
    }

    /**
     * @return the commodityFile
     */
    public String getCommodityFile() {
        return commodityFile;
    }

    /**
     * @param commodityFile the commodityFile to set
     */
    public void setCommodityFile(String commodityFile) {
        this.commodityFile = commodityFile;
    }

    /**
     * @return the shipFile
     */
    public String getShipFile() {
        return shipFile;
    }

    /**
     * @param shipFile the shipFile to set
     */
    public void setShipFile(String shipFile) {
        this.shipFile = shipFile;
    }

    /**
     * @return the orientGraphDb
     */
    public String getOrientGraphDb() {
        return orientGraphDb;
    }

    /**
     * @param orientGraphDb the orientGraphDb to set
     */
    public void setOrientGraphDb(String orientGraphDb) {
        this.orientGraphDb = orientGraphDb;
    }

    /**
     * @return the avoidStationsFile
     */
    public String getAvoidStationsFile() {
        return avoidStationsFile;
    }

    /**
     * @param avoidStationsFile the avoidStationsFile to set
     */
    public void setAvoidStationsFile(String avoidStationsFile) {
        this.avoidStationsFile = avoidStationsFile;
    }

    /**
     * @return the eliteDangerousAppDirectory
     */
    public String getEliteDangerousAppDirectory() {
        return eliteDangerousAppDirectory;
    }

    /**
     * @param eliteDangerousAppDirectory the eliteDangerousAppDirectory to set
     */
    public void setEliteDangerousAppDirectory(String eliteDangerousAppDirectory) {
        this.eliteDangerousAppDirectory = eliteDangerousAppDirectory;
    }

}
