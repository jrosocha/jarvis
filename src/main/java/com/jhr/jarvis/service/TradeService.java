package com.jhr.jarvis.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
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
    
    public List<BestExchange> tradeNOrientDb(String fromStationName, BestExchange parent, Ship ship, int maxJumps, int tradeStops, List<BestExchange> endList) {
        
        try {
            tradeStops--;
            
            List<BestExchange> exchangesForInputStation = tradeOrientDb(fromStationName, parent, ship, maxJumps);
            
            if (tradeStops > 0) {
                int tradeStopsLocal = tradeStops;
                exchangesForInputStation.parallelStream().forEach(exchangeForInputStation-> {                   
                    List<BestExchange> thisTrip = tradeNOrientDb(exchangeForInputStation.getSellStationName(), exchangeForInputStation, ship, maxJumps, tradeStopsLocal, endList);                    
                    exchangeForInputStation.setNextTrip(thisTrip);
                });
            } else {
                endList.addAll(exchangesForInputStation);
            }
            
            return exchangesForInputStation;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    public List<BestExchange> tradeOrientDb(String fromStationName, BestExchange parent, Ship ship, int maxJumps) {
        Station fromStation = new Station();
        OrientGraph graph = null;
        List<BestExchange> bestExchangeList = new CopyOnWriteArrayList<>();
        
        try {
            graph = orientDbService.getFactory().getTx();
            
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
                        int parentRouteProfit = parent != null ? parent.getRoutePerProfitUnit() : 0;
                        bestExchange.setRoutePerProfitUnit(parentRouteProfit + bestExchange.getPerUnitProfit());
                        // add the exchange to the master list.
                        bestExchangeList.add(bestExchange);
                    }
                }
            }
            
            graph.commit();
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        List<BestExchange> sortedBestExchangeList =  bestExchangeList.parallelStream().sorted((a,b)->{ return Integer.compare(a.getPerUnitProfit(), b.getPerUnitProfit()); }).collect(Collectors.toList());
        sortedBestExchangeList = Lists.reverse(sortedBestExchangeList);
        return sortedBestExchangeList;
    }
    
    public String sellOrientDb(Station fromStation, Ship ship, int maxJumps, String commodity) {
        Date start = new Date();
        List<Map<String, Object>> tableData = new ArrayList<>();
        OrientGraph graph = null;
        try {
            graph = orientDbService.getFactory().getTx();
            
            Vertex stationVertex = graph.getVertexByKey("Station.name", fromStation.getName());
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
                                Commodity sellCommodity = new Commodity(commodityVertex.getProperty("name"), buyPrice, supply, sellPrice, demand);
                                Map<String, Object> row = new HashMap<>();
                                row.put("COMMODITY", sellCommodity.getName());
                                row.put("TO SYSTEM", destinationSystem.getProperty("name"));
                                row.put("TO STATION", station.getProperty("name"));
                                row.put("UNIT PRICE", sellPrice);
                                row.put("DEMAND", demand);
                                row.put("DAYS OLD", (((new Date().getTime() - date)/1000/60/60/24) * 100)/100);
                                tableData.add(row);
                            }
                        }
                    }
                    
                }
            }           
            graph.commit();
        } catch(Exception e) {
            if (graph != null) {
                graph.rollback();
            }
        }
            
        if (tableData.size() == 0) {
            return "No sale available in provided range";
        }
        
        tableData = tableData.parallelStream().sorted((row1,row2)->{
            int p1 = (int) row1.get("UNIT PRICE");
            int p2 = (int) row2.get("UNIT PRICE");
            return Integer.compare(p1, p2);
        }).collect(Collectors.toList());
        
        Collections.reverse(tableData);
        String out = "";
        
        
//        String out = OsUtils.LINE_SEPARATOR;
//        out += "From System: " + fromStation.getSystem() + OsUtils.LINE_SEPARATOR;
//        out += "From Station: " + fromStation.getName() + OsUtils.LINE_SEPARATOR;
//        out += "Cargo Capacity: " + ship.getCargoSpace() + OsUtils.LINE_SEPARATOR;
//        out += tableData.size() + " Best stations to sell " + tableData.get(0).get("COMMODITY") + " within " + maxJumps + " jump(s) @ " + ship.getJumpDistance() + " ly or less." + OsUtils.LINE_SEPARATOR;
//        out += OsUtils.LINE_SEPARATOR + TableRenderer.renderMapDataAsTable(tableData, 
//                ImmutableList.of("TO SYSTEM", "TO STATION", "UNIT PRICE", "DEMAND",  "DAYS OLD"));
//        
//        out += OsUtils.LINE_SEPARATOR + "executed in " + (new Date().getTime() - start.getTime())/1000.0 + " seconds.";
        return out;
    }
    
    public String buyOrientDb(Station fromStation, Ship ship, int maxJumps, String commodity) {
        Date start = new Date();
        List<Map<String, Object>> tableData = new ArrayList<>();
        OrientGraph graph = null;
        try {
            graph = orientDbService.getFactory().getTx();
            
            Vertex stationVertex = graph.getVertexByKey("Station.name", fromStation.getName());
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
                        
                        if (supply > ship.getCargoSpace() && buyPrice > 0) {
                            Vertex commodityVertex = exchange.getVertex(Direction.IN);
                            if (commodityVertex.getProperty("name").equals(commodity)) {
                                Commodity buyCommodity = new Commodity(commodityVertex.getProperty("name"), buyPrice, supply, sellPrice, demand);
                                Map<String, Object> row = new HashMap<>();
                                row.put("COMMODITY", buyCommodity.getName());
                                row.put("TO SYSTEM", destinationSystem.getProperty("name"));
                                row.put("TO STATION", station.getProperty("name"));
                                row.put("UNIT PRICE", buyPrice);
                                row.put("SUPPLY", supply);
                                row.put("DAYS OLD", (((new Date().getTime() - date)/1000/60/60/24) * 100)/100);
                                tableData.add(row);
                            }
                        }
                    }
                    
                }
            }           
            graph.commit();
        } catch(Exception e) {
            if (graph != null) {
                graph.rollback();
            }
        }
            
        if (tableData.size() == 0) {
            return "No purchase available in provided range";
        }
        
        tableData = tableData.parallelStream().sorted((row1,row2)->{
            int p1 = (int) row1.get("UNIT PRICE");
            int p2 = (int) row2.get("UNIT PRICE");
            return Integer.compare(p1, p2);
        }).collect(Collectors.toList());
        
        String out = "";
        
//        String out = OsUtils.LINE_SEPARATOR;
//        out += "From System: " + fromStation.getSystem() + OsUtils.LINE_SEPARATOR;
//        out += "From Station: " + fromStation.getName() + OsUtils.LINE_SEPARATOR;
//        out += "Cargo Capacity: " + ship.getCargoSpace() + OsUtils.LINE_SEPARATOR;
//        out += tableData.size() + " Best stations to buy " + tableData.get(0).get("COMMODITY") + " within " + maxJumps + " jump(s) @ " + ship.getJumpDistance() + " ly or less." + OsUtils.LINE_SEPARATOR;
//        out += OsUtils.LINE_SEPARATOR + TableRenderer.renderMapDataAsTable(tableData, 
//                ImmutableList.of("TO SYSTEM", "TO STATION", "UNIT PRICE", "SUPPLY", "DAYS OLD"));
//        
//        out += OsUtils.LINE_SEPARATOR + "executed in " + (new Date().getTime() - start.getTime())/1000.0 + " seconds.";
        return out;
    }
    
    public List<BestExchange> bestBuyPriceOrientDb(String commodityName) {
        
        List<BestExchange> out = new ArrayList<>();
        OrientGraph graph = null;
        try {
            graph = orientDbService.getFactory().getTx();
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
                    exchange.setBuyStationName(stationVertex.getProperty("name"));
                    exchange.setBuySystemName(systemVertex.getProperty("name"));
                    exchange.setCommodity(commodityName);
                    exchange.setSupply(supply);
                    exchange.setBuyPrice(buyPrice);
                    
                    out.add(exchange);
                    
                   // row.put("DAYS OLD", (((new Date().getTime() - date)/1000/60/60/24) * 100)/100);
                }
            }
            graph.commit();
        } catch (Exception e) {
            if (graph != null) {
                graph.rollback();
            }
        }
        
        out = out.parallelStream().sorted((row1,row2)->{
            return Integer.compare(row1.getBuyPrice(), row2.getBuyPrice());
        }).collect(Collectors.toList());       

        return out;
    }

    
    public String bestSellPriceOrientDb(String commodityName) {
        Date start = new Date();
        
        List<Map<String, Object>> tableData = new ArrayList<>();
        
        OrientGraph graph = null;
        try {
            graph = orientDbService.getFactory().getTx();
            //starting commodity
            OrientVertex commodityVertex = (OrientVertex) graph.getVertexByKey("Commodity.name", commodityName);
            
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
                    tableData.add(row);
                }
            }
            graph.commit();
        } catch (Exception e) {
            if (graph != null) {
                graph.rollback();
            }
        }
        
        if (tableData.size() == 0) {
            return "No sale available in data";
        }
        
        tableData = tableData.parallelStream().sorted((row1,row2)->{
            int p1 = (int) row1.get("UNIT PRICE");
            int p2 = (int) row2.get("UNIT PRICE");
            return Integer.compare(p1, p2);
        }).collect(Collectors.toList());
        
        Collections.reverse(tableData);
        
        String out = "";
        
//        String out = OsUtils.LINE_SEPARATOR;
//        out += tableData.size() + " Best stations to sell " + commodityName + OsUtils.LINE_SEPARATOR;
//        out += OsUtils.LINE_SEPARATOR + TableRenderer.renderMapDataAsTable(tableData, 
//                ImmutableList.of("TO SYSTEM", "TO STATION", "UNIT PRICE", "DEMAND", "DAYS OLD"));
//        out += OsUtils.LINE_SEPARATOR + "executed in " + (new Date().getTime() - start.getTime())/1000.0 + " seconds.";
        return out;
    }
    
    public List<BestExchange> stationToStation(Station fromStation, Station toStation, Ship ship) {

        OrientGraph graph = null;
        Map<String, Commodity> buyCommodities = new HashMap<>();
        Map<String, Commodity> sellCommodities = new HashMap<>();
        
        try {
            graph = orientDbService.getFactory().getTx();
            
            //starting station
            OrientVertex stationFromVertex = (OrientVertex) graph.getVertexByKey("Station.name", fromStation.getName());
            
            OrientVertex stationToVertex = (OrientVertex) graph.getVertexByKey("Station.name", toStation.getName());
            
            // populate worthwile buy commodities
            buyCommodities = stationService.getStationBuyCommodities(stationFromVertex, ship);
            sellCommodities = stationService.getReleventStationSellCommodities(stationToVertex, buyCommodities, ship);
            graph.commit();
        } catch (Exception e) {
            e.printStackTrace();
            graph.rollback();
        }
        
        List<BestExchange> exchanges = new ArrayList<>();
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
            
            exchanges.add(e);
            
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
            OrientGraph graph = orientDbService.getFactory().getTx();
            exchangeCount = graph.countEdges("Exchange");
            graph.commit();
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
