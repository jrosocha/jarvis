package com.jhr.jarvis.service;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.jhr.jarvis.model.BestExchange;
import com.jhr.jarvis.model.Commodity;
import com.jhr.jarvis.model.SavedExchange;
import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.model.Ship;
import com.jhr.jarvis.model.Station;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

@Service
public class TradeService {
    
    @Autowired
    private Settings settings;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private StationService stationService;
    
    @Autowired
    private StarSystemService starSystemService;
    
    @Autowired
    private OrientDbService orientDbService;
    
    private Map<Integer, SavedExchange> lastSearchedExchanges = new HashMap<>(); 

    
    public List<BestExchange> pathToExchange(List<BestExchange> data, int index) {

        List<BestExchange> pathToExchange = new ArrayList<>();
        BestExchange currentExchange = data.get(index);
        
        while (currentExchange != null) {
            pathToExchange.add(currentExchange);
            currentExchange = currentExchange.getParent();
        }
        
        Collections.reverse(pathToExchange);
        return pathToExchange;
          
    }
    
    public Set<BestExchange> tradeNOrientDb(String fromStationName, BestExchange parent, Ship ship, int maxJumps, int tradeStops, Set<BestExchange> endExchanges) {
        
        try {
            tradeStops--;
            
            Set<BestExchange> exchangesForInputStation = tradeOrientDb(fromStationName, parent, ship, maxJumps);
            
            if (tradeStops > 0) {
                int tradeStopsLocal = tradeStops;
                exchangesForInputStation.parallelStream().forEach(exchangeForInputStation-> {                   
                    Set<BestExchange> thisTrip = tradeNOrientDb(exchangeForInputStation.getSellStationName(), exchangeForInputStation, ship, maxJumps, tradeStopsLocal, endExchanges);                    
                    exchangeForInputStation.setNextTrip(thisTrip);
                });
            } else {
                endExchanges.addAll(exchangesForInputStation);
            }
            
            return exchangesForInputStation;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    /**
     * All the trades for neighboring systems (within ship ly of input station)
     * 
     * @param fromStationName
     * @param parent
     * @param ship
     * @param maxJumps
     * @return
     */
    public Set<BestExchange> tradeOrientDb(String fromStationName, BestExchange parent, Ship ship, int maxJumps) {
        Station fromStation = new Station();
        OrientGraphNoTx graph = null;
        SortedSet<BestExchange> bestExchangeSet = new ConcurrentSkipListSet<>();
        
        try {
            graph = orientDbService.getFactory().getNoTx();
            
            //starting station
            OrientVertex vertexStation = (OrientVertex) graph.getVertexByKey("Station.name", fromStationName);
            fromStation.setName(vertexStation.getProperty("name"));
            
            // populate worthwile buy commodities
            Map<String, Commodity> buyCommodities = stationService.getStationBuyCommodities(vertexStation, ship);
            
            // get a system for a station.
            Vertex originSystem = stationService.getSystemVertexForStationVertex(vertexStation);
            fromStation.setSystem(originSystem.getProperty("name"));
    
            Set<Vertex> systemsWithinNShipJumps = starSystemService.findSystemsWithinNFrameshiftJumpsOfDistance(graph, originSystem, ship.getJumpDistance(), maxJumps);
            
            for (Vertex destinationSystem: systemsWithinNShipJumps) {
                
                String destinationSystemName = destinationSystem.getProperty("name");
                Set<Vertex> systemStations = starSystemService.findStationsInSystemOrientDb(destinationSystem, null);
                
                for (Vertex station: systemStations) {
                    Station toStation = new Station(station.getProperty("name"), destinationSystemName);
                    Map<String, Commodity> sellCommodities = stationService.getReleventStationSellCommodities(station, buyCommodities, ship);
                    
                    for (String commodity: sellCommodities.keySet()) {
                        
                        Commodity buyCommodity = buyCommodities.get(commodity);
                        Commodity sellCommodity = sellCommodities.get(commodity);
                        BestExchange bestExchange = new BestExchange(fromStation, toStation, buyCommodity, sellCommodity, ship.getCargoSpace());
                        bestExchange.setRoutePerProfitUnit(bestExchange.getPerUnitProfit());
                        bestExchange.setParent(parent);
                        bestExchange.setDistanceFromOrigin(starSystemService.distanceCalc(originSystem, destinationSystem));
                        bestExchange.setSellStationDataAge(sellCommodity.getDate().toInstant(ZoneOffset.UTC).toEpochMilli());
                        bestExchange.setBuyStationDataAge(buyCommodity.getDate().toInstant(ZoneOffset.UTC).toEpochMilli());
                        int parentRouteProfit = parent != null ? parent.getRoutePerProfitUnit() : 0;
                        bestExchange.setRoutePerProfitUnit(parentRouteProfit + bestExchange.getPerUnitProfit());
                        // add the exchange to the master list.
                        bestExchangeSet.add(bestExchange);
                    }
                }
            }
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Comparator<BestExchange> byProfit =
                Comparator.comparingInt(BestExchange::getPerUnitProfit);

        Supplier<ConcurrentSkipListSet<BestExchange>> supplier =
                () -> new ConcurrentSkipListSet<BestExchange>(byProfit.reversed());

        Set<BestExchange> sortedBestExchanges = bestExchangeSet
                            .parallelStream()
                            .collect(Collectors.toCollection(supplier));
        
        return sortedBestExchanges;
    }
    
    public Set<BestExchange> sellOrientDb(String fromStationName, Ship ship, int maxJumps, String commodity) {
        Date start = new Date();
        Set<BestExchange> out = new ConcurrentSkipListSet<>();
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            
            Vertex stationVertex = graph.getVertexByKey("Station.name", fromStationName);
            Vertex systemVertex = stationService.getSystemVertexForStationVertex(stationVertex);
            
            Set<Vertex> systemsWithinNShipJumps = starSystemService.findSystemsWithinNFrameshiftJumpsOfDistance(graph, systemVertex, ship.getJumpDistance(), maxJumps);
            systemsWithinNShipJumps.add(systemVertex);
            
            for (Vertex destinationSystem: systemsWithinNShipJumps) {
                Set<Vertex> systemStations = starSystemService.findStationsInSystemOrientDb(destinationSystem, null);
                for (Vertex station: systemStations) {
                    for (Edge exchange: station.getEdges(Direction.OUT, "Exchange")) {            

                        int sellPrice = exchange.getProperty("sellPrice");
                        int buyPrice = exchange.getProperty("buyPrice");
                        int supply = exchange.getProperty("supply");
                        int demand = exchange.getProperty("demand");
                        long date = exchange.getProperty("date");
                        
                        if (demand > ship.getCargoSpace() && sellPrice > 0) {
                            Vertex commodityVertex = exchange.getVertex(Direction.IN);
                            if (commodityVertex.getProperty("name").equals(commodity)) {
                                BestExchange bestExchange = new BestExchange();
                                bestExchange.setBuyStationName(fromStationName);
                                bestExchange.setBuySystemName(systemVertex.getProperty("name"));
                                bestExchange.setSupply(0);
                                bestExchange.setSellStationName(station.getProperty("name"));
                                bestExchange.setSellSystemName(destinationSystem.getProperty("name"));
                                bestExchange.setCommodity(commodity);
                                bestExchange.setSellPrice(sellPrice);
                                bestExchange.setDemand(demand);
                                bestExchange.setSellStationDataAge(date);
                                bestExchange.setDistanceFromOrigin(starSystemService.distanceCalc(systemVertex, destinationSystem));
                                out.add(bestExchange);
                                
                            }
                        }
                    }
                }
            }           
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        Comparator<BestExchange> bySellPrice =
                Comparator.comparingInt(BestExchange::getSellPrice);

        Supplier<ConcurrentSkipListSet<BestExchange>> supplier =
                () -> new ConcurrentSkipListSet<BestExchange>(bySellPrice.reversed());

        Set<BestExchange> sortedBestExchanges = out
                            .parallelStream()
                            .collect(Collectors.toCollection(supplier));
     
        return sortedBestExchanges;
    }
    
    public Set<BestExchange> buyOrientDb(String fromStation, Ship ship, int maxJumps, String commodity) {

        Set<BestExchange> out = new ConcurrentSkipListSet<>();
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            
            Vertex stationVertex = graph.getVertexByKey("Station.name", fromStation);
            Vertex systemVertex = stationService.getSystemVertexForStationVertex(stationVertex);
            
            Set<Vertex> systemsWithinNShipJumps = starSystemService.findSystemsWithinNFrameshiftJumpsOfDistance(graph, systemVertex, ship.getJumpDistance(), maxJumps);
            systemsWithinNShipJumps.add(systemVertex);
            
            for (Vertex destinationSystem: systemsWithinNShipJumps) {
                Set<Vertex> systemStations = starSystemService.findStationsInSystemOrientDb(destinationSystem, null);
                for (Vertex station: systemStations) {
                    for (Edge exchange: station.getEdges(Direction.OUT, "Exchange")) {            

                        int sellPrice = exchange.getProperty("sellPrice");
                        int buyPrice = exchange.getProperty("buyPrice");
                        int supply = exchange.getProperty("supply");
                        int demand = exchange.getProperty("demand");
                        long date = exchange.getProperty("date");
                        
                        if (supply >= ship.getCargoSpace() && buyPrice > 0) {
                            Vertex commodityVertex = exchange.getVertex(Direction.IN);
                            if (commodityVertex.getProperty("name").equals(commodity)) {
                                BestExchange bestExchange = new BestExchange();
                                bestExchange.setCommodity(commodity);
                                bestExchange.setBuyStationName(station.getProperty("name"));
                                bestExchange.setBuySystemName(destinationSystem.getProperty("name"));
                                bestExchange.setBuyStationDataAge(date);
                                bestExchange.setBuyPrice(buyPrice);
                                bestExchange.setSupply(supply);
                                bestExchange.setSellStationName(stationVertex.getProperty("name"));
                                bestExchange.setSellSystemName(systemVertex.getProperty("name"));
                                bestExchange.setSellPrice(sellPrice);
                                bestExchange.setDemand(demand);
                                bestExchange.setDistanceFromOrigin(starSystemService.distanceCalc(systemVertex, destinationSystem));
                                out.add(bestExchange);
                            }
                        }
                    }
                    
                }
            }           
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        Comparator<BestExchange> byBuyPrice =
                Comparator.comparingInt(BestExchange::getBuyPrice);

        Supplier<ConcurrentSkipListSet<BestExchange>> supplier =
                () -> new ConcurrentSkipListSet<BestExchange>(byBuyPrice);

        Set<BestExchange> sortedBestExchanges = out
                            .parallelStream()
                            .collect(Collectors.toCollection(supplier));
            
        return sortedBestExchanges;
    }
    
    public Set<BestExchange> bestBuyPriceOrientDb(String originSystem, String commodityName) {
        
        Set<BestExchange> out = new ConcurrentSkipListSet<>();
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            
            OrientVertex startSystemVertex = (OrientVertex) graph.getVertexByKey("System.name", originSystem);
            
            //starting commodity
            OrientVertex commodityVertex = (OrientVertex) graph.getVertexByKey("Commodity.name", commodityName);
            
            for (Edge hasExchange: commodityVertex.getEdges(Direction.IN, "Exchange")) {
                
                int supply = hasExchange.getProperty("supply");
                int buyPrice = hasExchange.getProperty("buyPrice");
                long date = hasExchange.getProperty("date");
                if (supply > 0 && buyPrice > 0) {
                    Vertex stationVertex = hasExchange.getVertex(Direction.OUT);
                    Vertex systemVertex = stationService.getSystemVertexForStationVertex(stationVertex);
                    
                    BestExchange exchange = new BestExchange();
                    exchange.setSellStationName("?");
                    exchange.setSellSystemName(startSystemVertex.getProperty("name"));
                    exchange.setBuyStationName(stationVertex.getProperty("name"));
                    exchange.setBuySystemName(systemVertex.getProperty("name"));
                    exchange.setCommodity(commodityName);
                    exchange.setSupply(supply);
                    exchange.setBuyPrice(buyPrice);
                    exchange.setBuyStationDataAge(date);
                    exchange.setDistanceFromOrigin(starSystemService.distanceCalc(startSystemVertex, systemVertex));
                    out.add(exchange);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Comparator<BestExchange> byBuyPrice =
                Comparator.comparingInt(BestExchange::getBuyPrice);

        Supplier<ConcurrentSkipListSet<BestExchange>> supplier =
                () -> new ConcurrentSkipListSet<BestExchange>(byBuyPrice);

        Set<BestExchange> sortedBestExchanges = out
                            .parallelStream()
                            .collect(Collectors.toCollection(supplier));
        
        return sortedBestExchanges;

    }

    
    public Set<BestExchange> bestSellPriceOrientDb(String originSystem, String commodityName) {
        
        List<BestExchange> out = new ArrayList<>();
        
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            //starting commodity
            OrientVertex commodityVertex = (OrientVertex) graph.getVertexByKey("Commodity.name", commodityName);
            
            OrientVertex startSystemVertex = (OrientVertex) graph.getVertexByKey("System.name", originSystem);
            
            for (Edge hasExchange: commodityVertex.getEdges(Direction.IN, "Exchange")) {
                
                int demand = hasExchange.getProperty("demand");
                int sellPrice = hasExchange.getProperty("sellPrice");
                long date = hasExchange.getProperty("date");
                if (demand > 0 && sellPrice > 0) {
                    Vertex stationVertex = hasExchange.getVertex(Direction.OUT);
                    Vertex systemVertex = stationService.getSystemVertexForStationVertex(stationVertex);
                    Map<String, Object> row = new HashMap<>();
                    row.put("TO SYSTEM", systemVertex.getProperty("name"));
                    row.put("TO STATION", stationVertex.getProperty("name"));
                    row.put("UNIT PRICE", sellPrice);
                    row.put("DEMAND", demand);
                    row.put("DAYS OLD", (((new Date().getTime() - date)/1000/60/60/24) * 100)/100);
                    
                    BestExchange exchange = new BestExchange();
                    exchange.setBuyStationName("?");
                    exchange.setBuySystemName(startSystemVertex.getProperty("name"));
                    exchange.setSellStationName(stationVertex.getProperty("name"));
                    exchange.setSellSystemName(systemVertex.getProperty("name"));
                    exchange.setCommodity(commodityName);
                    exchange.setDemand(demand);
                    exchange.setSellPrice(sellPrice);
                    exchange.setSellStationDataAge(date);
                    exchange.setDistanceFromOrigin(starSystemService.distanceCalc(startSystemVertex, systemVertex));
                    out.add(exchange);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        Comparator<BestExchange> bySellPrice =
                Comparator.comparingInt(BestExchange::getSellPrice);

        Supplier<ConcurrentSkipListSet<BestExchange>> supplier =
                () -> new ConcurrentSkipListSet<BestExchange>(bySellPrice);

        Set<BestExchange> sortedBestExchanges = out
                            .parallelStream()
                            .collect(Collectors.toCollection(supplier));
        
        return sortedBestExchanges;

    }
    
    public Set<BestExchange> stationToStation(Station fromStation, Station toStation, Ship ship) {

        OrientGraphNoTx graph = null;
        Map<String, Commodity> buyCommodities = new HashMap<>();
        Map<String, Commodity> sellCommodities = new HashMap<>();
        Set<BestExchange> exchanges = new ConcurrentSkipListSet<>();
        
        try {
            graph = orientDbService.getFactory().getNoTx();
            
            //starting station
            OrientVertex stationFromVertex = (OrientVertex) graph.getVertexByKey("Station.name", fromStation.getName());
            Vertex originSystem = stationService.getSystemVertexForStationVertex(stationFromVertex);
            OrientVertex stationToVertex = (OrientVertex) graph.getVertexByKey("Station.name", toStation.getName());
            Vertex destinationSystem = stationService.getSystemVertexForStationVertex(stationToVertex);
            
            // populate worthwile buy commodities
            buyCommodities = stationService.getStationBuyCommodities(stationFromVertex, ship);
            sellCommodities = stationService.getReleventStationSellCommodities(stationToVertex, buyCommodities, ship);
            
            for (String key: sellCommodities.keySet()){
                
                BestExchange e = new BestExchange();
                e.setBuyPrice(buyCommodities.get(key).getBuyPrice());
                e.setBuyStationName(fromStation.getName());
                e.setBuySystemName(fromStation.getSystem());
                e.setSupply(buyCommodities.get(key).getSupply());
                e.setCommodity(key);
                
                int unitProfit = sellCommodities.get(key).getSellPrice() - buyCommodities.get(key).getBuyPrice(); 
                e.setPerUnitProfit(unitProfit);
                e.setQuantity(ship.getCargoSpace());
                e.setRoutePerProfitUnit(unitProfit);
                e.setSellPrice(sellCommodities.get(key).getSellPrice());
                e.setSellStationName(toStation.getName());
                e.setSellSystemName(toStation.getSystem());
                e.setDemand(sellCommodities.get(key).getDemand());
                
                e.setDistanceFromOrigin(starSystemService.distanceCalc(originSystem, destinationSystem));
                exchanges.add(e);
                
            }        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return exchanges;
    }
    
    /**
     * resets lastSearchedExchanges
     * with the results of the passed in query so long as the query has 
     * FROM STATION, FROM SYSTEM, TO STATION, TO SYSTEM
     * as returned values.
     * 
     * @param queryResults
     */
    protected List<Map<String,Object>> includeSavedExchangeIndexWithQueryResults(List<Map<String,Object>> queryResults) {
        int i = 0;
        lastSearchedExchanges.clear();
        List<Map<String, Object>> modifiedResultList = new ArrayList<>();
        for (Map<String, Object> result: queryResults) {
            i++;
            Map<String, Object> modifiedResult = new HashMap<>();
            modifiedResult.putAll(result);
            modifiedResult.put("#", i);
            SavedExchange e = new SavedExchange(new Station((String)result.get("FROM STATION"), (String) result.get("FROM SYSTEM")), 
                    new Station((String) result.get("TO STATION"), (String) result.get("TO SYSTEM")));
            lastSearchedExchanges.put(i, e);
            modifiedResultList.add(modifiedResult);
        }
        return modifiedResultList;
    }

    public long exchangeCountOrientDb() {
        
        long exchangeCount = 0;
        try {
            OrientGraphNoTx graph = orientDbService.getFactory().getNoTx();
            exchangeCount = graph.countEdges("Exchange");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exchangeCount;
    }
    
    /**
     * @return the lastSearchedExchanges
     */
    public Map<Integer, SavedExchange> getLastSearchedExchanges() {
        return lastSearchedExchanges;
    }

    /**
     * @param lastSearchedExchanges the lastSearchedExchanges to set
     */
    public void setLastSearchedExchanges(Map<Integer, SavedExchange> lastSearchedExchanges) {
        this.lastSearchedExchanges = lastSearchedExchanges;
    }
    
}
