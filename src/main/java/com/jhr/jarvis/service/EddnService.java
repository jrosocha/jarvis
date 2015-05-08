package com.jhr.jarvis.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

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
                        EddnMessage message = JarvisConfig.MAPPER.readValue(outputString, EddnMessage.class);
                        eddnMessageQueue.add(message);
                        lastMessageReceived = LocalDateTime.now();
                        eventPublisher.publishEvent(new ConsoleEvent("new EDDN record: " + message));
                        eventPublisher.publishEvent(new EddnMessageQueueModifiedEvent(eddnMessageQueue.size()));
                        if (autoProcess) {
                            processMessageQueue();
                        }
                        
                        
                    } catch (IOException | DataFormatException e) {
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
            starSystemService.saveSystemToOrient(system);
        }

        /*
         * add station to graph if needed
         */
        currentStation = new Station(msg.getMessage().getStationName().toUpperCase(), currentSystem.getName().toUpperCase());
        stationService.createStationOrientDb(currentSystem, currentStation);

        /*
         * add commodity to graph if needed
         */
        Commodity currentCommodity = new Commodity(msg.getMessage().getItemName().toUpperCase());
        stationService.createCommodityOrientDb(currentCommodity);

        /*
         * delete existing exchange if it exists
         */
        stationService.deleteCommodityExchangeRelationshipOrientDb(currentStation, currentCommodity);

        int buyPrice = msg.getMessage().getBuyPrice() == null ? 0 : msg.getMessage().getBuyPrice().intValue();
        int sellPrice = msg.getMessage().getSellPrice() == null ? 0 : msg.getMessage().getSellPrice().intValue();
        int supply = msg.getMessage().getStationStock() == null ? 0 : msg.getMessage().getStationStock().intValue();
        int demand = msg.getMessage().getDemand() == null ? 0 : msg.getMessage().getDemand().intValue();
        long date = msg.getMessage().getTimestamp() == null ? 0 : msg.getMessage().getTimestamp().getMillis();
        stationService.createCommodityExchangeRelationshipOrientDb(currentStation, currentCommodity, sellPrice, buyPrice, supply, demand, date);

        eventPublisher.publishEvent(new ConsoleEvent("added exchange " + msg.getMessage().toString()));
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
