package com.jhr.jarvis.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.jhr.jarvis.JarvisConfig;
import com.jhr.jarvis.exceptions.SystemNotFoundException;
import com.jhr.jarvis.model.MapData;
import com.jhr.jarvis.model.Node;
import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.model.Ship;
import com.jhr.jarvis.model.StarSystem;
import com.jhr.jarvis.model.Station;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.filter.OSQLPredicate;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientDynaElementIterable;
import com.tinkerpop.blueprints.impls.orient.OrientElement;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

@Service
public class StarSystemService {

    /**
     * list of systems loaded from the csv file, used to add new systems being
     * added from new stations.
     */
    private Set<StarSystem> starSystemData = null;

    @Autowired
    private OrientDbService orientDbService;

    @Autowired
    private StationService stationService;

    @Autowired
    private Settings settings;
    
    //private StarSystem currentStarSystem = null;

    public void getLatestSystemsFileFromEddb(File eddbSystemsJson) throws MalformedURLException, IOException {        
        FileUtils.copyURLToFile(new URL("http://eddb.io/archive/v3/systems.json"), eddbSystemsJson);
    }
    
    /**
     * produces the d3 map data for the system you are in and the systems you can reach with 1 jump.
     * 
     * @param starSystem
     * @param jumpDistance
     * @return
     */
    public MapData getMapDataForSystem(StarSystem starSystem, float jumpDistance) {

        double lyDistanceMultiplier = 20;
        
        MapData out = new MapData();

        List<StarSystem> closeSystems = new ArrayList<>(this.closeStarSystems(starSystem, jumpDistance));
        //closeSystems.remove(starSystem);
        //closeSystems.add(0, starSystem);
        
        double xOffset = 350 - (starSystem.getX() * lyDistanceMultiplier);
        double yOffset = 350 - (starSystem.getY() * lyDistanceMultiplier);
        
        /* 
         * add all the neighbor systems first
         */
        
        for (StarSystem system: closeSystems) {
            Node systemNode = new Node(system.getName(), xOffset + (system.getX() * lyDistanceMultiplier), yOffset + (system.getY() * lyDistanceMultiplier), true);
            systemNode.getAdditionalProperties().put("starSystem", system);
            //systemNode.getAdditionalProperties().put("idx", nodeIndex);
            out.getNodes().add(systemNode);
            //nodeIndex++;
        }
        
        /*
         * sort systems by z
         */
        Collections.sort(out.getNodes(), (sys1, sys2)->{
            return ((StarSystem)sys1.getAdditionalProperties().get("starSystem")).getZ().compareTo(((StarSystem)sys2.getAdditionalProperties().get("starSystem")).getZ());
        });
        
//        int nodeIndex = 0;
//        
//        if (nodeIndex > 0) {
//            // connect every new node to node 0
//            double distance = this.distanceCalc(system.getX(), starSystem.getX(), system.getY(), starSystem.getY(), system.getZ(), starSystem.getZ());
//            com.jhr.jarvis.model.Edge edge = new com.jhr.jarvis.model.Edge(0, nodeIndex, Math.round(distance*100.0)/100.0);
//            out.getEdges().add(edge);
//        }
        
        
        /*
         * Add stations
         */
        int nodeIndex = 0;
        for (Node systemNode: out.getNodes()) {
            systemNode.getAdditionalProperties().put("idx", nodeIndex);
            nodeIndex++;
            try {
                StarSystem starSystemWithStations = this.findExactSystemAndStationsOrientDb(systemNode.getName(), false);
                for (Station station: starSystemWithStations.getStations()) {
                    Node stationNode = new Node(station.getName(), 0, 0, false);
                    stationNode.getAdditionalProperties().put("station", station);
                    stationNode.getAdditionalProperties().put("idx", out.getNodes().size());
                    out.getNodes().add(stationNode);
                    com.jhr.jarvis.model.Edge edge = new com.jhr.jarvis.model.Edge((Integer) systemNode.getAdditionalProperties().get("idx"), (Integer) stationNode.getAdditionalProperties().get("idx"), 1.0);
                    edge.getAdditionalProperties().put("station", station);
                    out.getEdges().add(edge);                   
                }                
            } catch (SystemNotFoundException | IOException e) {
                System.out.println("System not yet in graph (go visit it!): " + systemNode.getName());
                continue;
            }
        }
        
        
        return out;
        
    }
    
    /**
     * 
     * @param system
     * @param avoidStations
     * @return
     */
    public Set<Vertex> findStationsInSystemOrientDb(Vertex system, Set<String> avoidStations) {

        if (avoidStations == null) {
            avoidStations = new HashSet<>();
        }

        Set<Vertex> stationsInSystem = new HashSet<>();
        if (system != null && system.getEdges(com.tinkerpop.blueprints.Direction.OUT, "Has") != null) {
            for (Edge hasEdge : system.getEdges(com.tinkerpop.blueprints.Direction.OUT, "Has")) {
                Vertex station = hasEdge.getVertex(com.tinkerpop.blueprints.Direction.IN);
                String stationName = station.getProperty("name");
                if (!avoidStations.contains(stationName)) {
                    stationsInSystem.add(station);
                }
            }
        }
        return stationsInSystem;
    }

    public Set<Vertex> findSystemsWithinOneFrameshiftJumpOfDistance(Vertex system, float jumpDistance) {

        Set<Vertex> systemsWithinOneJumpOfDistance = new HashSet<>();

        for (Edge edgeFrameshift : system.getEdges(com.tinkerpop.blueprints.Direction.BOTH, "Frameshift")) {
            double ly = edgeFrameshift.getProperty("ly");
            if (ly > jumpDistance) {
                // ignore any shift that is out of range
                continue;
            }

            // because we cant tell what direction the edge is from
            // system--shift--system, just try both.
            Vertex destinationSystem = null;
            destinationSystem = edgeFrameshift.getVertex(com.tinkerpop.blueprints.Direction.IN);
            if (destinationSystem == null || destinationSystem.getProperty("name").equals(system.getProperty("name"))) {
                destinationSystem = edgeFrameshift.getVertex(com.tinkerpop.blueprints.Direction.OUT);
            }

            if (destinationSystem == null) {
                continue;
            }

            systemsWithinOneJumpOfDistance.add(destinationSystem);
        }

        return systemsWithinOneJumpOfDistance;
    }

    public Set<Vertex> findSystemsWithinNFrameshiftJumpsOfDistance(OrientGraphNoTx graph, Vertex system, float jumpDistance, int jumps) {

        // "traverse in_Frameshift, out_Frameshift, Frameshift.in, Frameshift.out from #11:4 while $depth <= 4"
        // select from (traverse in_Frameshift, out_Frameshift, Frameshift.in,
        // Frameshift.out from #11:4 while $depth <= 4 and (@class = 'System' or
        // (@class = 'Frameshift' and ly < 10.0))) where @class = 'System'
        Set<Vertex> systemsWithinNJumpOfDistance = new HashSet<>();
        OrientVertex o = (OrientVertex) system;

        for (OIdentifiable id : o.traverse().fields("in_Frameshift", "out_Frameshift", "Frameshift.in", "Frameshift.out")
                .predicate(new OSQLPredicate("$depth <= " + jumps * 2 + " and (@class = 'System' or (@class = 'Frameshift' and ly < " + jumpDistance + "))"))) {

            OrientElement element = graph.getElement(id);
            if (element.getRecord().getClassName().equals("System")) {
                systemsWithinNJumpOfDistance.add((Vertex) element);
            }
        }

        return systemsWithinNJumpOfDistance;
    }

    public Set<StarSystem> closeStarSystems(final StarSystem s, final float withinDistance) {

        if (starSystemData == null) {
            return new HashSet<StarSystem>();
        }

        Set<StarSystem> closeSystems = starSystemData.parallelStream().filter(s2 -> {
            return distanceCalc(s.getX(), s2.getX(), s.getY(), s2.getY(), s.getZ(), s2.getZ()) <= withinDistance;
        }).collect(Collectors.toSet());

        return closeSystems;
    }

    /**
     * Loads the Systems.csv file to memory for use in identifying x,y,z coords
     * for use when adding systems to the graph.
     * 
     * @param systemsCsvFile
     * @throws IOException
     */
//    public synchronized void loadSystems(File systemsCsvFile) throws IOException {
//        Set<StarSystem> c = Files.lines(systemsCsvFile.toPath()).parallel().map(parseCSVLineToSystem).collect(Collectors.toSet());
//        starSystemData = c;
//    }

    public synchronized void loadSystemsV2(File eddbSystemsJson) throws JsonParseException, IOException {
        
        StopWatch sw = new StopWatch();
        
        JsonFactory jsonFactory = new JsonFactory();
        Set<StarSystem> systems = new ConcurrentSkipListSet<>();
        
        sw.start("Parse EDDB File @ " + eddbSystemsJson.getAbsolutePath());
        try (JsonParser parser = jsonFactory.createParser(eddbSystemsJson)) { 
            
            if(parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected an array");
              }
            
              while(parser.nextToken() == JsonToken.START_OBJECT) {
                // read everything from this START_OBJECT to the matching END_OBJECT
                StarSystem node = JarvisConfig.MAPPER.readValue(parser, StarSystem.class);
                systems.add(node);
              }
              System.out.println("setting " + systems.size() + " systems");
              starSystemData = systems;
        }
        sw.stop();
        System.out.println(sw.prettyPrint());

    }

    public List<String> getGovernments() {
        
        Set<String> governments = starSystemData.stream().filter(system->{
            if (StringUtils.isNotBlank(system.getGovernment())) {
                return true;
            }
            return false;
        }).map(StarSystem::getGovernment).collect(Collectors.toSet());
        
        List<String> out = new ArrayList<String>(governments);
        Collections.sort(out);
        return out;
    }
    
    public List<String> getAllegiances() {
        
        Set<String> allegiances = starSystemData.stream().filter(system->{
            if (StringUtils.isNotBlank(system.getAllegiance())) {
                return true;
            }
            return false;
        }).map(StarSystem::getAllegiance).collect(Collectors.toSet());
        
        List<String> out = new ArrayList<String>(allegiances);
        Collections.sort(out);
        return out;
    }
    
    public List<String> getFactions() {
        
        Set<String> factions = starSystemData.stream().filter(system->{
            if (StringUtils.isNotBlank(system.getFaction())) {
                return true;
            }
            return false;
        }).map(StarSystem::getFaction).collect(Collectors.toSet());
        
        List<String> out = new ArrayList<String>(factions);
        Collections.sort(out);
        return out;
    }
    
    public List<String> getEconomies() {
        
        Set<String> economies = starSystemData.stream().filter(system->{
            if (StringUtils.isNotBlank(system.getPrimaryEconomy())) {
                return true;
            }
            return false;
        }).map(StarSystem::getPrimaryEconomy).collect(Collectors.toSet());
        
        List<String> out = new ArrayList<String>(economies);
        Collections.sort(out);
        return out;
    }
    
    /**
     * When adding a system found via a EliteOCR import, we grab the system's
     * coordinates from the data/System.csv file and add systems that are close
     * to that one, so that systems without stations are more likely to appear
     * in the graph.
     * 
     * @param systemName
     * @param exactMatch
     *            if false, use String.match(regex). If true use String.equals
     * @return
     * @throws IOException
     */
    public List<StarSystem> searchSystemFileForStarSystemsByName(String systemName, boolean exactMatch) throws IOException {

        if (starSystemData == null) {
            return new ArrayList<>();
        }

        List<StarSystem> systems;
        if (exactMatch) {
            systems = starSystemData.parallelStream().filter(ss -> {
                return ss.getName().toUpperCase().equals(systemName);
            }).collect(Collectors.toList());
        } else {
            systems = starSystemData.parallelStream().filter(ss -> {
                return ss.getName().matches(systemName);
            }).collect(Collectors.toList());
        }
        return systems;
    }

    public void addPropertyToSystem(String systemName, String propertyName, Object value) throws SystemNotFoundException {
        OrientGraphNoTx graph = null;

        if (StringUtils.isBlank(systemName) || StringUtils.isBlank(propertyName) || value == null) {
            return;
        }
        
        
        for (int retry = 0; retry < JarvisConfig.MAX_SAVE_RETRIES; retry++) {
        
            OrientVertex vertexSystem;
            try {
                graph = orientDbService.getFactory().getNoTx();
                vertexSystem = (OrientVertex) graph.getVertexByKey("System.name", systemName);
                if (vertexSystem == null) {
                    throw new SystemNotFoundException("Unique station could not be identified for '" + systemName + "'.");
                }
                if (vertexSystem.getProperty(propertyName) == null ||
                        (vertexSystem.getProperty(propertyName) != null && !vertexSystem.getProperty(propertyName).equals(value))) {
                    vertexSystem.setProperty(propertyName, value);
                    System.out.println("Set " + propertyName + "-->" + value + " for " + systemName);
                }
                break;
    
            } catch (OConcurrentModificationException e1)  {
                System.out.println("save retry #" + retry + "; cause="  + e1);
                retry++;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            } finally {
                graph.shutdown();
            }
        }
        
        
        
    }

    /**
     * Adds or updates a system to the graph.
     * when adding a new system, this also adds edges from this system to all nearby systems (defined in settings.getLongestDistanceEdge())
     * 
     * @param system
     */
    public synchronized void saveOrUpdateSystemToOrient(StarSystem system, boolean updateEdges, boolean replaceAllSystemData) {

        OrientGraphNoTx graph = null;
        boolean existingSystem = false;

        try {
            graph = orientDbService.getFactory().getNoTx();

            OrientVertex vertexSystem = (OrientVertex) graph.getVertexByKey("System.name", system.getName().toUpperCase());
            if (vertexSystem != null) {
                existingSystem = true;                
            }
            
            if (!existingSystem) {
                vertexSystem = graph.addVertex("class:System");
            }
            
            /*
             * perhaps if the system already exists and has data we should not change it?
             */
            if (StringUtils.isBlank((String)vertexSystem.getProperty("name")) || replaceAllSystemData) {
                vertexSystem.setProperty("name", system.getName().toUpperCase());
            }
            
            if (vertexSystem.getProperty("x") == null || replaceAllSystemData) {
                vertexSystem.setProperty("x", system.getX());
            }
            
            if (vertexSystem.getProperty("y") == null || replaceAllSystemData) {
                vertexSystem.setProperty("y", system.getY());
            }
            
            if (vertexSystem.getProperty("z") == null || replaceAllSystemData) {
                vertexSystem.setProperty("z", system.getZ());
            }
            if (StringUtils.isBlank((String)vertexSystem.getProperty("allegiance")) || replaceAllSystemData) {
                vertexSystem.setProperty("allegiance", system.getAllegiance() != null ? system.getAllegiance().toUpperCase() : "");
            }
            
            if (StringUtils.isBlank((String)vertexSystem.getProperty("faction")) || replaceAllSystemData) {
                vertexSystem.setProperty("faction", system.getFaction() != null ? system.getFaction().toUpperCase() : "");
            }
            
            if (StringUtils.isBlank((String)vertexSystem.getProperty("government")) || replaceAllSystemData) {
                vertexSystem.setProperty("government", system.getGovernment() != null ? system.getGovernment().toUpperCase() : "");
            }
            
            if (vertexSystem.getProperty("needsPermit") == null || replaceAllSystemData) {
                vertexSystem.setProperty("needsPermit", system.getNeedsPermit() != null ? system.getNeedsPermit() : false);
            }
            
            if (vertexSystem.getProperty("population") == null || replaceAllSystemData) {
                vertexSystem.setProperty("population", system.getPopulation() != null ? system.getPopulation() : 0);
            }
            
            if (StringUtils.isBlank((String)vertexSystem.getProperty("primaryEconomy")) || replaceAllSystemData) {
                vertexSystem.setProperty("primaryEconomy", system.getPrimaryEconomy() != null ? system.getPrimaryEconomy().toUpperCase() : "");
            }
            
            if (StringUtils.isBlank((String)vertexSystem.getProperty("secondaryEconomy")) || replaceAllSystemData) {
                vertexSystem.setProperty("secondaryEconomy", system.getSecondaryEconomy() != null ? system.getSecondaryEconomy().toUpperCase() : "");
            }
            
            if (StringUtils.isBlank((String)vertexSystem.getProperty("security")) || replaceAllSystemData) {
                vertexSystem.setProperty("security", system.getSecurity() != null ? system.getSecurity().toUpperCase() : "");
            }
            
            if (StringUtils.isBlank((String)vertexSystem.getProperty("state")) || replaceAllSystemData) {
                vertexSystem.setProperty("state", system.getState() != null ? system.getState().toUpperCase() : "");      
            }

            if (existingSystem && updateEdges) {
                // delete edges for system
                for (Edge edge: vertexSystem.getEdges(Direction.BOTH, "Frameshift")) {
                    edge.remove();
                }
            }
            int edgeCount = 0;
            for (Edge edge: vertexSystem.getEdges(Direction.BOTH, "Frameshift")) {
                edgeCount++;
            }

            if (updateEdges || edgeCount == 0) {
                // for each system within the defined distance, add an edge
                for (Vertex vertexSystem2 : graph.getVerticesOfClass("System")) {
                    // the these 2 are not the save vertex
                    if (!vertexSystem2.getProperty("name").equals(vertexSystem.getProperty("name"))) {
                        double distance = distanceCalc(vertexSystem.getProperty("x"), vertexSystem2.getProperty("x"), vertexSystem.getProperty("y"), vertexSystem2.getProperty("y"),
                                vertexSystem.getProperty("z"), vertexSystem2.getProperty("z"));
                        // verify edge is inside the max edge range
                        if (distance > settings.getLongestDistanceEdge()) {
                            // and edge too far
                            continue;
                        }
                        // verify edge does not exist
                        Edge frameshiftEdge = vertexSystem.addEdge("Frameshift", vertexSystem2);
                        frameshiftEdge.setProperty("ly", distance);
                    }
                }
            }

            System.out.println("Added: " + system);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs an exact match and a partial match looking to identify a single
     * system. If a single system is found, it is loaded into memory
     * 
     * @param partial
     * @return
     * @throws Exception
     */
    public StarSystem findUniqueSystemOrientDb(String partial) throws SystemNotFoundException {

        StarSystem foundSystem = null;
        boolean found = false;

        try {
            foundSystem = findExactSystemAndStationsOrientDb(partial, false);
            found = true;
        } catch (Exception e) {
            // not an exact match. proceed
        }

        if (!found) {

            List<StarSystem> systems = findSystemsOrientDb(partial);

            if (systems.size() == 0 || systems.size() > 1) {
                throw new SystemNotFoundException("Unique station could not be identified for '" + partial + "'.");
            }

            foundSystem = systems.get(0);
        }

        return foundSystem;
    }
    
    
    public StarSystem convertVertexToSystem(OrientVertex systemVertex) {

        if (systemVertex != null) {
            systemVertex.getPropertyKeys().forEach((key)->{System.out.println("to convert " + systemVertex.getProperty("name") + ": " + key + "-->" + systemVertex.getProperty(key));});
        }
        
        StarSystem starSystem = null;
        if (systemVertex != null) {
            starSystem = new StarSystem(systemVertex.getProperty("name"));
            starSystem.setX(systemVertex.getProperty("x"));
            starSystem.setY(systemVertex.getProperty("y"));
            starSystem.setZ(systemVertex.getProperty("z"));
           
            starSystem.setAllegiance(systemVertex.getProperty("allegiance"));
            starSystem.setFaction(systemVertex.getProperty("faction"));
            starSystem.setGovernment(systemVertex.getProperty("government"));
            starSystem.setNeedsPermit(systemVertex.getProperty("needsPermit"));
            starSystem.setPopulation(systemVertex.getProperty("population"));
            starSystem.setPrimaryEconomy(systemVertex.getProperty("primaryEconomy"));
            starSystem.setSecondaryEconomy(systemVertex.getProperty("secondaryEconomy"));
            starSystem.setSecurity(systemVertex.getProperty("security"));
            starSystem.setState(systemVertex.getProperty("state"));
        }
        
        System.out.println("converted=" + starSystem);
        return starSystem;
    }
    
    //i left here. i want to compare the current system with eddb and update it if eddb data is less null.
    public StarSystem updateSystemWithLatestFromEddbData(StarSystem starSystem) throws SystemNotFoundException, IOException {
        
            List<StarSystem> latestInEddb = this.searchSystemFileForStarSystemsByName(starSystem.getName(), true);
            if (latestInEddb.size() > 0) {
                StarSystem fileStarSystem = latestInEddb.get(0);
                
                if (StringUtils.isBlank(starSystem.getAllegiance())) {
                    starSystem.setAllegiance(fileStarSystem.getAllegiance());
                }
                
                if (StringUtils.isBlank(starSystem.getFaction())) {
                    starSystem.setFaction(fileStarSystem.getFaction());
                }
                
                if (StringUtils.isBlank(starSystem.getGovernment())) {
                    starSystem.setGovernment(fileStarSystem.getGovernment());
                }
                
                if (StringUtils.isBlank(starSystem.getPrimaryEconomy())) {
                    starSystem.setPrimaryEconomy(fileStarSystem.getPrimaryEconomy());
                }
                
                if (StringUtils.isBlank(starSystem.getSecurity())) {
                    starSystem.setState(fileStarSystem.getSecurity());
                }
                
                if (starSystem.getNeedsPermit() == null) {
                    starSystem.setNeedsPermit(fileStarSystem.getNeedsPermit());
                }
                
                if (starSystem.getPopulation() == null || starSystem.getPopulation() == 0) {
                    starSystem.setPopulation(fileStarSystem.getPopulation());
                }
                
                return starSystem;
                
            } else {
                throw new SystemNotFoundException(starSystem.getName());
            }
    }

    /**
     * Matches if the system exists as typed
     * also populates stations
     * 
     * @param systemName
     * @return
     * @throws IOException 
     * @throws Exception
     */
    public StarSystem findExactSystemAndStationsOrientDb(String systemName, boolean updateAgainstEddb) throws SystemNotFoundException, IOException {

        System.out.println("findExactSystemAndStationsOrientDb called with systemName=" + systemName + ", updateAgainstEddb=" + updateAgainstEddb);
        
        if (systemName == null) {
            throw new SystemNotFoundException("Exact system '" + systemName + "' could not be identified");
        }

        StarSystem foundSystem = null;

        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            OrientVertex vertexSystem = (OrientVertex) graph.getVertexByKey("System.name", systemName);
            if (vertexSystem != null) {
                vertexSystem.getPropertyKeys().forEach((key)->{System.out.println(vertexSystem.getProperty("name") + ": " + key + "-->" + vertexSystem.getProperty(key));});
            }
            
            if (vertexSystem != null) {
                foundSystem = convertVertexToSystem(vertexSystem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        if (foundSystem == null) {
            throw new SystemNotFoundException("Exact system '" + systemName + "' could not be identified");
        }
        
        if (updateAgainstEddb) {
            updateSystemWithLatestFromEddbData(foundSystem);
            saveOrUpdateSystemToOrient(foundSystem, false, false);
        }
        
        foundSystem.setStations(stationService.getStationsForSystemOrientDb(foundSystem.getName()));

        return foundSystem;
    }

    /**
     * Matches a system starting with @param partial
     * 
     * @param partial
     * @return
     */
    public List<StarSystem> findSystemsOrientDb(String partial) {
        OrientGraphNoTx graph = null;
        List<StarSystem> out = new ArrayList<>();
        try {
            graph = orientDbService.getFactory().getNoTx();
            
            String queryWhere = partial == null ? "" : " where name like '" + partial.toUpperCase() + "%'";
            
            for (Vertex systemVertex : (Iterable<Vertex>) graph.command(new OCommandSQL("select from System" + queryWhere)).execute()) {
                StarSystem foundSystem = new StarSystem(systemVertex.getProperty("name"));
                foundSystem.setX((float) systemVertex.getProperty("x"));
                foundSystem.setY((float) systemVertex.getProperty("y"));
                foundSystem.setZ((float) systemVertex.getProperty("z"));
                out.add(foundSystem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public long systemCountOrientDb() {
        
        long systemCount = 0;
        try {
            OrientGraphNoTx graph = orientDbService.getFactory().getNoTx();
            systemCount = graph.countVertices("System");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return systemCount;        
    }
    
    public long shiftCountOrientDb() {
        
        long shiftCount = 0;
        try {
            OrientGraphNoTx graph = orientDbService.getFactory().getNoTx();
            shiftCount = graph.countEdges("Frameshift");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shiftCount;
    }

    private double distanceCalc(float x1, float x2, float y1, float y2, float z1, float z2) {
        return Math.sqrt((Math.pow((x1 - x2), 2.0) + Math.pow((y1 - y2), 2.0) + Math.pow((z1 - z2), 2.0)));
    }
    
    public double distanceCalc(Vertex systemA, Vertex systemB) {
        return distanceCalc(systemA.getProperty("x"), systemB.getProperty("x"), systemA.getProperty("y"), systemB.getProperty("y"), systemA.getProperty("z"), systemB.getProperty("z"));    
    }

    public MapData calculateShortestPathBetweenSystems(Ship ship, List<String> systemsInRoute) {
        OrientGraphNoTx graph = null;
        
        MapData out = new MapData();
        double lyDistanceMultiplier = 20;

        LinkedList<OrientVertex> path = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            
            // handle a 1 system solution 
            if (systemsInRoute.size() == 1) {
                OrientVertex nextSystemVertex = (OrientVertex) graph.getVertexByKey("System.name", systemsInRoute.get(0));
                Node systemNode = new Node(nextSystemVertex.getProperty("name"), 350.0, 350.0, true);
                out.getNodes().add(systemNode);
                return out;
            }
            
            OrientVertex lastSystemVertex = null;
            OrientVertex lastSystemInPathBetweenVertex = null;
            int systemIdx = 0;
            double xOffset = 0.0;
            double yOffset = 0.0;
            
            for (String nextSystem: systemsInRoute) {
                OrientVertex nextSystemVertex = (OrientVertex) graph.getVertexByKey("System.name", nextSystem);
                if (lastSystemVertex != null) {
                    
                    double lyLimit = ship.getJumpDistance();
                    Map<String,Object> params = new HashMap<String,Object>();
                    params.put("sourceVertex", lastSystemVertex);
                    params.put("destinationVertex", nextSystemVertex);
                    params.put("weightEdgeFieldName", "ly");
                    params.put("weightLimit", lyLimit);
                    
                    String sql = String.format("select dijkstra2(%s, %s, '%s', %f, 'BOTH')", lastSystemVertex.getId().toString(), nextSystemVertex.getId().toString(), "ly", ship.getJumpDistance());
                    OrientDynaElementIterable result = graph.command(new OCommandSQL(sql)).execute();
                    for (Object obj : result) {
                        OrientVertex thing = (OrientVertex) obj; 
                        path = thing.getRecord().field("dijkstra2");
                        break;
                    }

                    if (path == null) {
                        System.out.println(String.format("No path could be found between %s and %s with a %.2f jump distance", lastSystemVertex.getProperty("name"), nextSystem, ship.getJumpDistance()));
                        return out;
                    }
                    
                    for (OrientVertex pathBetweenVertex: path) {

                        Node systemInPath = new Node(pathBetweenVertex.getProperty("name"), 
                                xOffset + ((float)pathBetweenVertex.getProperty("x") * lyDistanceMultiplier), 
                                yOffset + ((float)pathBetweenVertex.getProperty("y") * lyDistanceMultiplier), true);
                        
                        if (out.getNodes().size() == 0 || !out.getNodes().contains(systemInPath)) {
                            if (lastSystemInPathBetweenVertex != null) {
                                Edge frameshift = lastSystemInPathBetweenVertex.getEdges(pathBetweenVertex, Direction.BOTH, "Frameshift").iterator().next();                    
                                com.jhr.jarvis.model.Edge mapEdge = new com.jhr.jarvis.model.Edge(systemIdx - 1, systemIdx,  Math.round(((double) frameshift.getProperty("ly"))*100.0)/100.0);
                                out.getEdges().add(mapEdge);
                            }
                            
                            systemInPath.getAdditionalProperties().put("idx", systemIdx);
                            out.getNodes().add(systemInPath);
                            systemIdx ++;
                        }
                        
                        lastSystemInPathBetweenVertex = pathBetweenVertex;

                    }
                }
                
                lastSystemVertex = nextSystemVertex;
                xOffset = 350 - ((float)lastSystemVertex.getProperty("x") * lyDistanceMultiplier);
                yOffset = 350 - ((float)lastSystemVertex.getProperty("y") * lyDistanceMultiplier);
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    
    public MapData calculateShortestPathBetweenSystems(Ship ship, String startSystemName, String finishSystemName) {
        OrientGraphNoTx graph = null;
        
        MapData out = new MapData();
        
        LinkedList<OrientVertex> path = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            OrientVertex startSystemVertex = (OrientVertex) graph.getVertexByKey("System.name", startSystemName);
            OrientVertex destinationSystemVertex = (OrientVertex) graph.getVertexByKey("System.name", finishSystemName);

            double lyLimit = ship.getJumpDistance();
            Map<String,Object> params = new HashMap<String,Object>();
            params.put("sourceVertex", startSystemVertex);
            params.put("destinationVertex", destinationSystemVertex);
            params.put("weightEdgeFieldName", "ly");
            params.put("weightLimit", lyLimit);
            
            String sql = String.format("select dijkstra2(%s, %s, '%s', %f, 'BOTH')", startSystemVertex.getId().toString(), destinationSystemVertex.getId().toString(), "ly", ship.getJumpDistance());
            OrientDynaElementIterable result = graph.command(new OCommandSQL(sql)).execute();
            
            for (Object obj : result) {
                OrientVertex thing = (OrientVertex) obj; 
                path = thing.getRecord().field("dijkstra2");
                break;
            }

            if (path == null) {
                System.out.println(String.format("No path could be found between %s and %s with a %.2f jump distance", startSystemName, finishSystemName, ship.getJumpDistance()));
                return out;
            }
            
            OrientVertex lastSystem = null;
            int nodeIndex = 0;
            for (OrientVertex vertex: path) {
                if (lastSystem != null) {
                    Edge frameshift = lastSystem.getEdges(vertex, Direction.BOTH, "Frameshift").iterator().next();                    
                    com.jhr.jarvis.model.Edge mapEdge = new com.jhr.jarvis.model.Edge(nodeIndex - 1, nodeIndex,  Math.round(((double) frameshift.getProperty("ly"))*100.0)/100.0);
                    out.getEdges().add(mapEdge);
                }
                Node systemNode = new Node(vertex.getProperty("name"), vertex.getProperty("x"), vertex.getProperty("y"), true);
                systemNode.getAdditionalProperties().put("idx", nodeIndex);
                out.getNodes().add(systemNode);
                lastSystem = vertex;
                nodeIndex ++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

//    public StarSystem getCurrentStarSystem() {
//        return currentStarSystem;
//    }
//
//    public void setCurrentStarSystem(StarSystem currentStarSystem) {
//        this.currentStarSystem = currentStarSystem;
//    }
//    
    public MapData shortestPath(String startSystemName, String destinationSystemName, double maxJumpRange) {
        
        OrientGraphNoTx graph = null;
        MapData out = new MapData();

        try {
            graph = orientDbService.getFactory().getNoTx();
        
            Map<String, VertexWrapper> systems = new HashMap<>();
            
            OrientVertex startSystemVertex = (OrientVertex) graph.getVertexByKey("System.name", startSystemName);
            OrientVertex destinationSystemVertex = (OrientVertex) graph.getVertexByKey("System.name", destinationSystemName);
            
            double distanceBetweenStartAndFinish = distanceCalc(startSystemVertex, destinationSystemVertex);
            
            for (Vertex system: graph.getVerticesOfClass("System")) {
                if (distanceCalc(startSystemVertex, system) <= distanceBetweenStartAndFinish || distanceCalc(destinationSystemVertex, system) <= distanceBetweenStartAndFinish) {
                    systems.put(system.getProperty("name"), new VertexWrapper((OrientVertex) system));
                }
            }
            
            computePaths(systems.get(startSystemVertex), maxJumpRange, systems);
            
            List<VertexWrapper> path = new ArrayList<VertexWrapper>();
            for (VertexWrapper vertex = systems.get(destinationSystemVertex); vertex != null; vertex = vertex.previous) {
                path.add(vertex);
            }
    
            Collections.reverse(path);
            
            int nodeIndex = 0;
            VertexWrapper currentVertex = path.get(0);
            Node systemNode = new Node(currentVertex.vertex.getProperty("name"), currentVertex.vertex.getProperty("x"), currentVertex.vertex.getProperty("y"), true);
            systemNode.getAdditionalProperties().put("idx", nodeIndex);
            out.getNodes().add(systemNode);
            nodeIndex ++; 
            
            while (currentVertex.previous != null) {
                systemNode = new Node(currentVertex.previous.vertex.getProperty("name"), currentVertex.previous.vertex.getProperty("x"), currentVertex.previous.vertex.getProperty("y"), true);
                systemNode.getAdditionalProperties().put("idx", nodeIndex);
                out.getNodes().add(systemNode);
                
                Edge frameshift = currentVertex.vertex.getEdges(currentVertex.previous.vertex, Direction.BOTH, "Frameshift").iterator().next();                    
                com.jhr.jarvis.model.Edge mapEdge = new com.jhr.jarvis.model.Edge(nodeIndex - 1, nodeIndex,  Math.round(((double) frameshift.getProperty("ly"))*100.0)/100.0);
                out.getEdges().add(mapEdge);
                
                currentVertex = currentVertex.previous;
                nodeIndex ++;
            }
            
            
//            OrientVertex lastSystem = null;
//            int nodeIndex = 0;
//            for (VertexWrapper vertexWrapper: path) {
//                if (lastSystem != null) {
//                    Edge frameshift = lastSystem.getEdges(vertex, Direction.BOTH, "Frameshift").iterator().next();                    
//                    com.jhr.jarvis.model.Edge mapEdge = new com.jhr.jarvis.model.Edge(nodeIndex - 1, nodeIndex,  Math.round(((double) frameshift.getProperty("ly"))*100.0)/100.0);
//                    out.getEdges().add(mapEdge);
//                }
//                Node systemNode = new Node(vertex.getProperty("name"), vertex.getProperty("x"), vertex.getProperty("y"), true);
//                systemNode.getAdditionalProperties().put("idx", nodeIndex);
//                out.getNodes().add(systemNode);
//                lastSystem = vertex;
//                nodeIndex ++;
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return out;
    }
    
    private void computePaths(VertexWrapper source, double maxJumpRange, Map<String, VertexWrapper> data) {
        
        source.minDistance = 0.0;
        PriorityQueue<VertexWrapper> vertexQueue = new PriorityQueue<>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            
            VertexWrapper current = vertexQueue.poll();
            
            // Visit each edge exiting origin
            for (Edge edgeFrameshift : current.vertex.getEdges(Direction.BOTH, "Frameshift")) {
                
                double distance = (double) edgeFrameshift.getProperty("ly");
                if (distance > maxJumpRange) {
                    continue;
                }
                
                VertexWrapper destinationSystem = null;
                for (Vertex possibleDestination :current.vertex.getVertices(Direction.BOTH, "System")) {
                    if (!possibleDestination.getProperty("name").equals(current.vertex.getProperty("name")) && data.containsKey(possibleDestination.getProperty("name"))) {
                        destinationSystem = data.get(possibleDestination.getProperty("name"));
                        break;
                    }
                }
                
                if (destinationSystem == null) {
                    continue;
                }

                double distanceThroughCurrent = current.minDistance + distance;
                if (distanceThroughCurrent < destinationSystem.minDistance) {
                    vertexQueue.remove(destinationSystem);
                    destinationSystem.minDistance = distanceThroughCurrent ;
                    destinationSystem.previous = current;
                    vertexQueue.add(destinationSystem);
                }
            }
        }
    }
    
    private class VertexWrapper implements Comparable<VertexWrapper> {
         public final OrientVertex vertex;
         public double minDistance = Double.POSITIVE_INFINITY;
         public VertexWrapper previous;
         public VertexWrapper(OrientVertex vertex) { this.vertex = vertex; }
         
         public int compareTo(VertexWrapper other)
         {
             return Double.compare(minDistance, other.minDistance);
         }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((vertex == null) ? 0 : vertex.getProperty("name").hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            VertexWrapper other = (VertexWrapper) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (vertex == null) {
                if (other.vertex != null)
                    return false;
            } else if (!vertex.getProperty("name").equals(other.vertex.getProperty("name")))
                return false;
            return true;
        }

        private StarSystemService getOuterType() {
            return StarSystemService.this;
        }
         
         
     
     }
}
