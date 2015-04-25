package com.jhr.jarvis.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.jhr.jarvis.exceptions.SystemNotFoundException;
import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.model.Ship;
import com.jhr.jarvis.model.StarSystem;
import com.jhr.jarvis.model.Station;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.filter.OSQLPredicate;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientDynaElementIterable;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientElement;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
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
    
    private StarSystem currentStarSystem = null;

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

    public Set<Vertex> findSystemsWithinNFrameshiftJumpsOfDistance(OrientGraph graph, Vertex system, float jumpDistance, int jumps) {

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
    public synchronized void loadSystems(File systemsCsvFile) throws IOException {
        Set<StarSystem> c = Files.lines(systemsCsvFile.toPath()).parallel().map(parseCSVLineToSystem).collect(Collectors.toSet());
        starSystemData = c;
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
            loadSystems(new File(settings.getSystemsFile()));
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

    public void saveSystemToOrient(StarSystem system) {

        OrientGraph graph = null;
        try {
            graph = orientDbService.getFactory().getTx();

            OrientVertex vertexSystem = (OrientVertex) graph.getVertexByKey("System.name", system.getName().toUpperCase());
            if (vertexSystem == null) {

                vertexSystem = graph.addVertex("class:System");
                vertexSystem.setProperty("name", system.getName().toUpperCase());
                vertexSystem.setProperty("x", system.getX());
                vertexSystem.setProperty("y", system.getY());
                vertexSystem.setProperty("z", system.getZ());

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
                        String edgeId = createFrameshiftEdgeId(vertexSystem.getProperty("name"), vertexSystem2.getProperty("name"));
                        OrientEdge frameshiftEdge = graph.getEdge(edgeId);
                        if (frameshiftEdge == null) {
                            System.out.println("Creating Edge:" + edgeId);
                            frameshiftEdge = graph.addEdge(edgeId, vertexSystem, vertexSystem2, "Frameshift");
                            frameshiftEdge.setProperty("ly", distance);
                        }
                    }
                }
            }
            graph.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (graph != null) {
                graph.rollback();
            }
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
            foundSystem = findExactSystemAndStationsOrientDb(partial);
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

    /**
     * Matches if the system exists as typed
     * also populates stations
     * 
     * @param systemName
     * @return
     * @throws Exception
     */
    public StarSystem findExactSystemAndStationsOrientDb(String systemName) throws SystemNotFoundException {

        if (systemName == null) {
            throw new SystemNotFoundException("Exact system '" + systemName + "' could not be identified");
        }

        StarSystem foundSystem = null;

        OrientGraph graph = null;
        try {
            graph = orientDbService.getFactory().getTx();
            OrientVertex vertexSystem = (OrientVertex) graph.getVertexByKey("System.name", systemName);
            if (vertexSystem != null) {
                foundSystem = new StarSystem(vertexSystem.getProperty("name"));
                foundSystem.setX(vertexSystem.getProperty("x"));
                foundSystem.setY(vertexSystem.getProperty("y"));
                foundSystem.setZ(vertexSystem.getProperty("z"));
            }
            graph.commit();
        } catch (Exception e) {
            if (graph != null) {
                graph.rollback();
            }
        }
       
        if (foundSystem == null) {
            throw new SystemNotFoundException("Exact system '" + systemName + "' could not be identified");
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
        OrientGraph graph = null;
        List<StarSystem> out = new ArrayList<>();
        try {
            graph = orientDbService.getFactory().getTx();
            
            String queryWhere = partial == null ? "" : " where name like '" + partial.toUpperCase() + "%'";
            
            for (Vertex systemVertex : (Iterable<Vertex>) graph.command(new OCommandSQL("select from System" + queryWhere)).execute()) {
                StarSystem foundSystem = new StarSystem(systemVertex.getProperty("name"));
                foundSystem.setX((float) systemVertex.getProperty("x"));
                foundSystem.setY((float) systemVertex.getProperty("y"));
                foundSystem.setZ((float) systemVertex.getProperty("z"));
                out.add(foundSystem);
            }
        } catch (Exception e) {
            if (graph != null) {
                graph.rollback();
            }
        }
        return out;
    }

    public long systemCountOrientDb() {
        
        long systemCount = 0;
        try {
            OrientGraph graph = orientDbService.getFactory().getTx();
            systemCount = graph.countVertices("System");
            graph.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return systemCount;        
    }
    
    public long shiftCountOrientDb() {
        
        long shiftCount = 0;
        try {
            OrientGraph graph = orientDbService.getFactory().getTx();
            shiftCount = graph.countEdges("Frameshift");
            graph.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shiftCount;
    }

    private Function<String, StarSystem> parseCSVLineToSystem = line -> {
        String[] splitLine = line.split(",");
        splitLine[0] = (splitLine[0].startsWith("'") || splitLine[0].startsWith("\"")) ? splitLine[0].substring(0, splitLine[0].length() - 1) : splitLine[0];
        StarSystem s = new StarSystem(splitLine[0].toUpperCase(), Float.parseFloat(splitLine[1]), Float.parseFloat(splitLine[2]), Float.parseFloat(splitLine[3]));
        return s;
    };

    private String createFrameshiftEdgeId(String systemName1, String systemName2) {
        return (systemName1.compareTo(systemName2) < 0) ? (systemName1 + '-' + systemName2) : (systemName2 + '-' + systemName1);
    }

    private double distanceCalc(float x1, float x2, float y1, float y2, float z1, float z2) {
        return Math.sqrt((Math.pow((x1 - x2), 2.0) + Math.pow((y1 - y2), 2.0) + Math.pow((z1 - z2), 2.0)));
    }
    
    public double distanceCalc(Vertex systemA, Vertex systemB) {
        return distanceCalc(systemA.getProperty("x"), systemB.getProperty("x"), systemA.getProperty("y"), systemB.getProperty("y"), systemA.getProperty("z"), systemB.getProperty("z"));    
    }

    public String calculateShortestPathBetweenSystems(Ship ship, String startSystemName, String finishSystemName) {
        OrientGraph graph = null;
        
        String out = "";
        LinkedList<OrientVertex> path = null;
        try {
            graph = orientDbService.getFactory().getTx();
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
                return String.format("No path could be found between %s and %s with a %.2f jump distance", startSystemName, finishSystemName, ship.getJumpDistance());
            }
            
            OrientVertex lastSystem = null;
            for (OrientVertex vertex: path) {
                if (lastSystem != null) {
                    
                    Edge frameshift = lastSystem.getEdges(vertex, Direction.BOTH, "Frameshift").iterator().next();
                    out += "--[" + String.format("%.2f", (double) frameshift.getProperty("ly")) + "]-->";
                }
                out += "(" + vertex.getProperty("name") + ")";
                lastSystem = vertex;
            }
            graph.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (graph != null) {
                graph.rollback();
            }
        }
        return out;
    }

    public StarSystem getCurrentStarSystem() {
        return currentStarSystem;
    }

    public void setCurrentStarSystem(StarSystem currentStarSystem) {
        this.currentStarSystem = currentStarSystem;
    }

}
