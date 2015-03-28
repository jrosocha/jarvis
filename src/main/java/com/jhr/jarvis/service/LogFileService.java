package com.jhr.jarvis.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.collect.EvictingQueue;
import com.jhr.jarvis.event.CurrentSystemChangedEvent;
import com.jhr.jarvis.exceptions.SettingNotFoundException;
import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.model.StarSystem;
import com.jhr.jarvis.model.Station;

@Service
public class LogFileService implements ApplicationEventPublisherAware {

    private File activeNetLogFile = null;
    
    private Thread netLogTailerThread = null;
    
    private String lastFoundSystemInNetLog = null;
    
    private EvictingQueue<String> last10LogLinesRead = EvictingQueue.create(50);
    
    @Autowired 
    private Settings settings;
    
    @Autowired 
    private StarSystemService starSystemService;
    
    @Autowired
    private StationService stationService;
    
    private ApplicationEventPublisher eventPublisher;
    
    @Scheduled(initialDelay = 5000, fixedRate = 15000)
    private void scheduledCheckForNewNetlogFile() {
        
        try {
            File latestNetlogFile = getLatestNetLogFile();
            if (activeNetLogFile == null || !(activeNetLogFile.getName().equals(latestNetlogFile.getName()))) {
                activeNetLogFile = latestNetlogFile;
                initNetlogTailer();
            }
        } catch (FileNotFoundException | SettingNotFoundException e) {
            e.printStackTrace();
        }
        
    }
    
    private void initNetlogTailer() {
        
        if (netLogTailerThread != null) {
            netLogTailerThread.interrupt();
            netLogTailerThread = null;
        }
        
        if (activeNetLogFile != null) {
            TailerListener listener = new NetLogTailerListener();
            Tailer tailer = new Tailer(activeNetLogFile, listener, 2000);
            this.netLogTailerThread = new Thread(tailer);
            netLogTailerThread.setDaemon(true);
            netLogTailerThread.start();
        }
    }
    
    private File getLatestNetLogFile() throws SettingNotFoundException, FileNotFoundException{
        
        File eliteDangerousAppDirectory = new File(settings.getEliteDangerousAppDirectory());
        
        if (!(eliteDangerousAppDirectory.exists() && eliteDangerousAppDirectory.isDirectory())) {
            throw new SettingNotFoundException("eliteDangerousAppDirectory is not correctly set in jarvis-config.json");
        }
        
        File logDir = new File(eliteDangerousAppDirectory, "Logs");
        if (!(logDir.exists() && logDir.isDirectory())) {
            throw new FileNotFoundException("logDir could not be found relative to " + eliteDangerousAppDirectory.getAbsolutePath());
        }
        
        List<String> logFiles = new ArrayList<>(Arrays.asList(logDir.list((File dirToFilter, String filename) -> filename.startsWith("netLog"))));
      
        if (logFiles.size() == 0) {
            throw new FileNotFoundException("No logs found in " + logDir.getAbsolutePath());
        }
        
        long lastMod = Long.MIN_VALUE;
        File newestFile = null;
        for (String fileName: logFiles) {
            File currentFile = new File(logDir, fileName);
            if (currentFile.lastModified() > lastMod) {
                newestFile = currentFile;
                lastMod = currentFile.lastModified();
            }
        }

        return newestFile;
    }
    
    public class NetLogTailerListener extends TailerListenerAdapter {
        public void handle(String line) {
            last10LogLinesRead.add(line);
            // look for a line containing: System:26(Hyroks)    
            Pattern pattern = Pattern.compile("System:\\d*\\(([^)]*)\\)");
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String foundSystem =  matcher.group(1);
                if (lastFoundSystemInNetLog == null || !(lastFoundSystemInNetLog.equalsIgnoreCase(foundSystem))){
                    lastFoundSystemInNetLog = foundSystem;
                    
                    StarSystem starSystem = null;
                    try {
                        List<StarSystem> found = starSystemService.searchSystemFileForStarSystemsByName(lastFoundSystemInNetLog.toUpperCase(), true);
                        if (found.size() > 0) {
                            starSystem = found.get(0);
                            List<Station> stations = stationService.getStationsForSystemOrientDb(starSystem.getName());
                            starSystem.setStations(stations);
                            eventPublisher.publishEvent(new CurrentSystemChangedEvent(starSystem));
                        } else {
                            eventPublisher.publishEvent(null);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                }
            }
        }
    }

    /**
     * @return the activeNetLogFile
     */
    public File getActiveNetLogFile() {
        return activeNetLogFile;
    }

    /**
     * @param activeNetLogFile the activeNetLogFile to set
     */
    public void setActiveNetLogFile(File activeNetLogFile) {
        this.activeNetLogFile = activeNetLogFile;
    }

    /**
     * @return the lastFoundSystemInNetLog
     */
    public String getLastFoundSystemInNetLog() {
        return lastFoundSystemInNetLog;
    }

    /**
     * @param lastFoundSystemInNetLog the lastFoundSystemInNetLog to set
     */
    public void setLastFoundSystemInNetLog(String lastFoundSystemInNetLog) {
        this.lastFoundSystemInNetLog = lastFoundSystemInNetLog;
    }

    /**
     * @return the last10LogLinesRead
     */
    public EvictingQueue<String> getLast10LogLinesRead() {
        return last10LogLinesRead;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
}
