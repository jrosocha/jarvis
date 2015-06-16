package com.jhr.jarvis.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableList;
import com.jhr.jarvis.exceptions.StationNotFoundException;
import com.jhr.jarvis.model.Commodity;
import com.jhr.jarvis.model.Ship;
import com.jhr.jarvis.model.StarSystem;
import com.jhr.jarvis.model.Station;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

@Service
public class StationService {
    
    @Autowired
    private CommodityService commodityService;
    
    @Autowired
    private StarSystemService starSystemService;
    
    @Autowired
    private OrientDbService orientDbService;
       
    private Station userLastStoredStation = null;
    
    
    public Map<String, Commodity> getStationBuyCommodities(Vertex stationVertex, Ship ship) {
        Map<String, Commodity> stationBuyCommodities = new HashMap<>();
        for (Edge exchange: stationVertex.getEdges(Direction.OUT, "Exchange")) {            

            int sellPrice = exchange.getProperty("sellPrice");
            int buyPrice = exchange.getProperty("buyPrice");
            int supply = exchange.getProperty("supply");
            int demand = exchange.getProperty("demand");
                       
            if (buyPrice > 0 && supply >= ship.getCargoSpace() && (buyPrice * ship.getCargoSpace()) <= ship.getCash()) {
                Vertex commodityVertex = exchange.getVertex(Direction.IN);
                Commodity commodity = new Commodity(commodityVertex.getProperty("name"), buyPrice, supply, sellPrice, demand);
                commodity.setGroup(commodityService.getCommodityGroup(commodity.getName()));
                stationBuyCommodities.put(commodity.getName(), commodity);
            }
        }
        return stationBuyCommodities;
    }
    
    public Map<String, Commodity> getReleventStationSellCommodities(Vertex stationVertex, Map<String, Commodity> buyCommodities, Ship ship) {
        Map<String, Commodity> stationSellReleventCommodities = new HashMap<>();
        for (Edge exchange: stationVertex.getEdges(Direction.OUT, "Exchange")) {            

            int sellPrice = exchange.getProperty("sellPrice");
            int buyPrice = exchange.getProperty("buyPrice");
            int supply = exchange.getProperty("supply");
            int demand = exchange.getProperty("demand");
            
            if (demand > ship.getCargoSpace() && sellPrice > 0) {
                Vertex commodityVertex = exchange.getVertex(Direction.IN);
                Commodity sellCommodity = new Commodity(commodityVertex.getProperty("name"), buyPrice, supply, sellPrice, demand);
                sellCommodity.setGroup(commodityService.getCommodityGroup(sellCommodity.getName()));
                Commodity boughtCommodity = buyCommodities.get(sellCommodity.getName());
                
                if (boughtCommodity != null && boughtCommodity.getBuyPrice() < sellCommodity.getSellPrice()) {
                    stationSellReleventCommodities.put(sellCommodity.getName(), sellCommodity);
                }
            }
        }
        
        return stationSellReleventCommodities;
    }
    
    public List<Commodity> getStationCommodities(Vertex stationVertex) {
        
        List<Commodity> stationCommodities = new ArrayList<>();
        
        for (Edge exchange: stationVertex.getEdges(Direction.OUT, "Exchange")) {            

            int sellPrice = exchange.getProperty("sellPrice");
            int buyPrice = exchange.getProperty("buyPrice");
            int supply = exchange.getProperty("supply");
            int demand = exchange.getProperty("demand");
            long date = exchange.getProperty("date");
            
            if ( (demand > 0 && sellPrice > 0) || (supply > 0 && buyPrice > 0)) {
                Vertex commodityVertex = exchange.getVertex(Direction.IN);
                Commodity commodity = new Commodity(commodityVertex.getProperty("name"), buyPrice, supply, sellPrice, demand, date);
                commodity.setGroup(commodityService.getCommodityGroup(commodity.getName()));
                stationCommodities.add(commodity);
            }
        }
        
        return stationCommodities;
    }
    
    public Station findExactStationOrientDb(String stationName) throws StationNotFoundException {
        OrientGraphNoTx graph = null;
        graph = orientDbService.getFactory().getNoTx();
        Station out = new Station();
        try {    
            OrientVertex stationVertex = (OrientVertex) graph.getVertexByKey("Station.name", stationName);
            if (stationVertex == null) {
                throw new StationNotFoundException("No station matching '" + stationName + "' in graph.");
            }
            out = vertexToStation(stationVertex);
            List<Commodity> stationCommodities = getStationCommodities(stationVertex);
            out.getAvailableCommodityExchanges().addAll(stationCommodities);
        } catch (Exception e) {
            throw new StationNotFoundException("No station matching '" + stationName + "' in graph.");
        }
        return out;
   }
    
    public Station deleteStationOrientDb(String stationName) throws StationNotFoundException {
        OrientGraphNoTx graph = null;
        graph = orientDbService.getFactory().getNoTx();
        Station out = new Station();
        try {    
            OrientVertex stationVertex = (OrientVertex) graph.getVertexByKey("Station.name", stationName);
            if (stationVertex == null) {
                throw new StationNotFoundException("No station matching '" + stationName + "' in graph.");
            }
            stationVertex.getEdges(Direction.BOTH).forEach((edge)->{edge.remove();});
            stationVertex.remove();
        } catch (Exception e) {
            throw new StationNotFoundException("No station matching '" + stationName + "' in graph.");
        }
        return out;
   }
    
    /**
     * Runs an exact match and a partial match looking to identify a single station.
     * If a single station is found, it is loaded into memory
     * 
     * @param partial
     * @return
     * @throws Exception
     */
    public Station findUniqueStationOrientDb(String partial, boolean loadIntoMemory) throws StationNotFoundException {
        
        Station foundStation = null;
        boolean found = false;
        
        try {
            foundStation = findExactStationOrientDb(partial);
            found = true;
        } catch (Exception e) {
            // not an exact match. proceed
        }
     
        if (!found) {
            List<Station> stations = findStationsOrientDb(partial, loadIntoMemory);
            
            if (stations.size() == 0 || stations.size() > 1 ) {
                throw new StationNotFoundException("Unique station could not be identified for '" + partial + "'.");
            }
            foundStation = stations.get(0);
        }
        
        if (loadIntoMemory) {
            setUserLastStoredStation(foundStation);
        }
        return foundStation;
    }
    
    
    /**
     * Gives an exact match on the station passed in, the unique station found matching what was passed in, the in memory store of a station of nothing was passed in, or an exception.
     * 
     * @param station
     * @param usage
     * @return
     * @throws StationNotFoundException
     */
    public Station getBestMatchingStationOrStoredStation(String station) throws StationNotFoundException {
        
        if (station == null && getUserLastStoredStation() != null) {
            return getUserLastStoredStation();
        } else if (!StringUtils.isEmpty(station)) {
            return findUniqueStationOrientDb(station, true);            
        }
        
        throw new StationNotFoundException("No unique station could be found.");
    }

    public Vertex getSystemVertexForStationVertex(Vertex stationVertex) {
        Vertex originSystem= null;
        for (Edge hasEdge: stationVertex.getEdges(Direction.IN, "Has")) {
            originSystem = hasEdge.getVertex(Direction.OUT);
            return originSystem;
        }
        return null;
    }

    public List<Station> findStationsOrientDb(String partial, boolean loadToMemory) {
        
        List<Station> out = new ArrayList<>();
        
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            
            String whereClause = StringUtils.isEmpty(partial) ? "" : " where name like '" + partial.toUpperCase() + "%'";
            for (Vertex stationVertex : (Iterable<Vertex>) graph.command(new OCommandSQL("select from Station" + whereClause)).execute()) {
                
                Station station = vertexToStation(stationVertex);
                out.add(station);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (out.size() == 1 && loadToMemory) {
            setUserLastStoredStation(out.get(0));
        }
        
        return out;
    }
    
    /**
     * Creates a station and its HAS with its system is the station is not yet present in the graph.
     * 
     * @param system
     * @param station
     * @return
     */
    public void createStationOrientDb(StarSystem system, Station station) {
        
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            OrientVertex vertexStation = (OrientVertex) graph.getVertexByKey("Station.name", station.getName());
            if (vertexStation == null) {
                vertexStation = graph.addVertex("class:Station");
                vertexStation.setProperty("name", station.getName());
                
                OrientVertex vertexSystem = (OrientVertex) graph.getVertexByKey("System.name", system.getName());
                graph.addEdge(vertexSystem.getProperty("name") + "-" + station.getName(), vertexSystem, vertexStation, "Has");
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteCommodityExchangeRelationshipOrientDb(Station station, Commodity commodity) {
        
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            OrientVertex vertexStation = (OrientVertex) graph.getVertexByKey("Station.name", station.getName());
            OrientVertex vertexCommodity = (OrientVertex) graph.getVertexByKey("Commodity.name", commodity.getName());
            for (Edge exchange: vertexStation.getEdges(vertexCommodity, Direction.BOTH)) {
                exchange.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void createCommodityExchangeRelationshipOrientDb(Station station, Commodity commodity, int sellPrice, int buyPrice, int supply, int demand, long date) {
        
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            OrientVertex vertexStation = (OrientVertex) graph.getVertexByKey("Station.name", station.getName());
            OrientVertex vertexCommodity = (OrientVertex) graph.getVertexByKey("Commodity.name", commodity.getName());
            OrientEdge edgeExchange = graph.addEdge(station.getName() + "-" + commodity.getName(), vertexStation, vertexCommodity, "Exchange");
            edgeExchange.setProperty("sellPrice", sellPrice);
            edgeExchange.setProperty("buyPrice", buyPrice);
            edgeExchange.setProperty("supply", supply);
            edgeExchange.setProperty("demand", demand);
            edgeExchange.setProperty("date", date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createCommodityOrientDb(Commodity commodity) {
        
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            OrientVertex vertexCommodity = (OrientVertex) graph.getVertexByKey("Commodity.name", commodity.getName());
            if (vertexCommodity == null) {
                vertexCommodity = graph.addVertex("class:Commodity");
                vertexCommodity.setProperty("name", commodity.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public int clearStationOfExchangesOrientDb(Station station) {
        int edgesRemoved = 0;
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            OrientVertex vertexStation = (OrientVertex) graph.getVertexByKey("Station.name", station.getName());
            for (Edge exchange: vertexStation.getEdges(Direction.OUT, "Exchange")) {
                exchange.remove();
                edgesRemoved++;
            };
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return edgesRemoved;
    }
        
    public String joinStationsAsString(List<Station> stations) {
        return stations.stream().map(s->{ return s.getName() + " @ " + s.getSystem();}).collect(Collectors.joining(", "));
    }
    
    /**
     * @param system
     * @return stations or an empty list
     */
    public List<Station> getStationsForSystemOrientDb(String system) {
        
        List<Station> stations = new ArrayList<>();
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            Vertex systemVertex = graph.getVertexByKey("System.name", system);
            Set<Vertex> systemStations = starSystemService.findStationsInSystemOrientDb(systemVertex, null);
            
            for (Vertex stationVertex: systemStations) {
            	
                Station station = vertexToStation(stationVertex);
                stations.add(station);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return stations;
    }

    public long stationCountOrientDb() {
        
        long stationCount = 0;
        try {
            OrientGraphNoTx graph = orientDbService.getFactory().getNoTx();
            stationCount = graph.countVertices("Station");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stationCount;        
    }
    
    public void addPropertyToStationOrientDb(Station station, String property, Object value) {
        
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            OrientVertex vertexStation = (OrientVertex) graph.getVertexByKey("Station.name", station.getName());
            if (vertexStation == null) {            	
            	throw new StationNotFoundException(station.getName() + " not found.");
            }
            vertexStation.setProperty(property, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Station vertexToStation(Vertex stationVertex) {
    	Station out = new Station();
        out.setName(stationVertex.getProperty("name"));
        
        Boolean blackMarket = stationVertex.getProperty("blackMarket") != null ? stationVertex.getProperty("blackMarket") : Boolean.FALSE;
        out.setBlackMarket(blackMarket);
        
        Vertex systemVertex = getSystemVertexForStationVertex(stationVertex);
        out.setSystem(systemVertex.getProperty("name"));
        
        for (Edge exchange: stationVertex.getEdges(Direction.OUT, "Exchange")) {
            out.setDate(exchange.getProperty("date"));
            break;
        }
        
        return out;
    }
    
    /**
     * @return the userLastStoredStation
     */
    public Station getUserLastStoredStation() {
        return userLastStoredStation;
    }

    /**
     * @param userLastStoredStation the userLastStoredStation to set
     */
    public void setUserLastStoredStation(Station station) {
        this.userLastStoredStation = station;
    }
    
}
