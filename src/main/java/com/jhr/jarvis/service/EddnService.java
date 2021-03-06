package com.jhr.jarvis.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.google.common.collect.EvictingQueue;
import com.jhr.jarvis.JarvisConfig;
import com.jhr.jarvis.event.ConsoleEvent;
import com.jhr.jarvis.event.EddnMessageQueueModifiedEvent;
import com.jhr.jarvis.event.OcrCompletedEvent;
import com.jhr.jarvis.model.Commodity;
import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.model.StarSystem;
import com.jhr.jarvis.model.Station;
import com.jhr.jarvis.model.eddn.EddnMessage;

@Service
@DependsOn({ "settings" })
public class EddnService implements ApplicationEventPublisherAware {

    @Autowired
    private Settings settings;

    @Autowired
    private StarSystemService starSystemService;

    @Autowired
    private StationService stationService;

    private LocalDateTime lastMessageReceived = null;
    
    private ApplicationEventPublisher eventPublisher;

    private Thread eddnScanningThread = null;
    private EvictingQueue<EddnMessage> eddnMessageQueue = EvictingQueue.create(100000);
    private boolean autoProcess= false;

    public int getMessageQueueSize() {
        return this.eddnMessageQueue.size();
    }
    
    public void cancelScanForEddnMessages() {
        if (eddnScanningThread != null) {
            eddnScanningThread.interrupt();
        }
    }
    
    public void scanForEddnMessages() {

        if (eddnScanningThread != null) {
            eddnScanningThread.interrupt();
        }
        
        Runnable eddnScanTask = () -> {

            try (Context context = ZMQ.context(1); Socket subscriber = context.socket(ZMQ.SUB);) {

                subscriber.connect("tcp://eddn-relay.elite-markets.net:9500");
                subscriber.subscribe("".getBytes());

                while (!Thread.interrupted()) {
                    byte[] raw = subscriber.recv();
                    Inflater inflater = new Inflater();
                    inflater.setInput(raw);
                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(raw.length)) {
                        byte[] buffer = new byte[1024];
                        while (!inflater.finished()) {
                            int count = inflater.inflate(buffer);
                            outputStream.write(buffer, 0, count);
                        }
                        byte[] output = outputStream.toByteArray();
                        String outputString = new String(output, 0, output.length, "UTF-8");
                        
                        System.out.println("received->" + outputString);
                        
                        EddnMessage message = JarvisConfig.MAPPER.readValue(outputString, EddnMessage.class);
                        
                        if ( message.get$schemaRef().equals("http://schemas.elite-markets.net/eddn/commodity/2") &&
                                message.getHeader().getSoftwareName().equalsIgnoreCase("EliteOCR") ||
                                message.getHeader().getSoftwareName().startsWith("E:D Market Connector")) {
                            eddnMessageQueue.add(message);
                            lastMessageReceived = LocalDateTime.now();
                            eventPublisher.publishEvent(new ConsoleEvent("new EDDN record: " + message));
                            eventPublisher.publishEvent(new EddnMessageQueueModifiedEvent(eddnMessageQueue.size()));
                            if (autoProcess) {
                                processMessageQueue();
                            }
                        } else {
                            System.out.println("Not accepting data from applicaion: " + message.getHeader().getSoftwareName());
                        }
                        
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        };

        eddnScanningThread = new Thread(eddnScanTask, "eddnScan");
        eddnScanningThread.setDaemon(true);
        eddnScanningThread.start();

    }
    
    public synchronized void processMessageQueue() {
        
        while (!this.eddnMessageQueue.isEmpty() ) {            
            EddnMessage message = this.eddnMessageQueue.poll();
            eventPublisher.publishEvent(new EddnMessageQueueModifiedEvent(eddnMessageQueue.size()));
            try {
                processEddnMessageOrientDb(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        eventPublisher.publishEvent(new OcrCompletedEvent(Boolean.TRUE));
    }

    private void processEddnMessageOrientDb(EddnMessage msg) throws IOException {

        StarSystem currentSystem = null;
        Station currentStation = null;

        Date start = new Date();

        /*
         * add system and other close systems if needed
         */
        List<StarSystem> matchingSystems = starSystemService.searchSystemFileForStarSystemsByName(msg.getMessage().getSystemName().toUpperCase(), true);
        if (matchingSystems.size() == 0) {
            eventPublisher.publishEvent(new ConsoleEvent("System not found in systems file " + msg.getMessage().getSystemName().toUpperCase()));
            return;
        } else if (matchingSystems.size() > 1) {
            eventPublisher.publishEvent(new ConsoleEvent("Multiple matching systems found in systems file " + msg.getMessage().getSystemName().toUpperCase()));
        }
        currentSystem = matchingSystems.get(0);
        Set<StarSystem> closeSystems = starSystemService.closeStarSystems(currentSystem, settings.getCloseSystemDistance());
        closeSystems.add(currentSystem);

        for (StarSystem system : closeSystems) {
            starSystemService.saveOrUpdateSystemToOrient(system, false, false);
        }

        /*
         * add station to graph if needed
         */
        if (StringUtils.isNotBlank(msg.getMessage().getStationName())) {
            currentStation = new Station(msg.getMessage().getStationName().toUpperCase(), currentSystem.getName().toUpperCase());
            stationService.createStationOrientDb(currentSystem, currentStation);
        }

        if (msg.getMessage().getCommodities() != null && msg.getMessage().getCommodities().size() > 0){
            
            for (com.jhr.jarvis.model.eddn.Commodity commodity: msg.getMessage().getCommodities()) {
                
                try {
                    /*
                     * add commodity to graph if needed
                     */
                    Commodity currentCommodity = new Commodity(commodity.getName().toUpperCase());
                    stationService.createCommodityOrientDb(currentCommodity);
                    /*
                    * delete existing exchange if it exists
                    */
                    stationService.deleteCommodityExchangeRelationshipOrientDb(currentStation, currentCommodity);
                    
                    int buyPrice = commodity.getBuyPrice() == null ? 0 : commodity.getBuyPrice().intValue();
                    int sellPrice = commodity.getSellPrice() == null ? 0 : commodity.getSellPrice().intValue();
                    int supply = commodity.getSupply() == null ? 0 : commodity.getSupply().intValue();
                    int demand =commodity.getDemand() == null ? 0 : commodity.getDemand().intValue();
                    stationService.createCommodityExchangeRelationshipOrientDb(currentStation, currentCommodity, sellPrice, buyPrice, supply, demand, new Date().toInstant().toEpochMilli());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        }

        eventPublisher.publishEvent(new ConsoleEvent("added exchange " + msg.toString()));
        eventPublisher.publishEvent(new ConsoleEvent("activity completed in " + ((new Date().getTime() - start.getTime()) / 1000.0) + " seconds"));

    };

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public boolean isAutoProcess() {
        return autoProcess;
    }

    public void setAutoProcess(boolean autoProcess) {
        this.autoProcess = autoProcess;
    }

    public LocalDateTime getLastMessageReceived() {
        return lastMessageReceived;
    }

    public void setLastMessageReceived(LocalDateTime lastMessageReceived) {
        this.lastMessageReceived = lastMessageReceived;
    }

}
