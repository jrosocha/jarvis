package com.jhr.jarvis.service;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jhr.jarvis.model.Settings;
import com.jhr.jarvis.orientDb.functions.OSQLFunctionDijkstraWithWeightMax;
import com.orientechnologies.orient.core.sql.OSQLEngine;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

@Service
public class OrientDbService {
    
    @Autowired 
    private Settings settings;

    private OrientGraphFactory factory = null;
    
    private void startDb() {
        
        if (factory == null) {
            
            // looks like the factory wont create a db without a little help.
            OrientGraph graph;
            try {
                
                graph = new OrientGraph(dbConfig());
                graph.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            factory = new OrientGraphFactory("plocal:" + settings.getOrientGraphDb(),"admin","admin").setupPool(1,10);
            factory.setUseLightweightEdges(false);            
    
            OrientGraphNoTx graphNoTx = null;
            try {
                graphNoTx = factory.getNoTx();
                
                OSQLEngine.getInstance().registerFunction(OSQLFunctionDijkstraWithWeightMax.NAME, OSQLFunctionDijkstraWithWeightMax.class);
                
                if (graphNoTx.getVertexType("System") == null) {
                    graphNoTx.createVertexType("System");
                    graphNoTx.createKeyIndex("name", Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", "System"));
                }               
                
                if (graphNoTx.getEdgeType("Frameshift") == null) {
                    graphNoTx.createEdgeType("Frameshift");
                }
                
                if (graphNoTx.getVertexType("Station") == null) {
                    graphNoTx.createVertexType("Station");
                    graphNoTx.createKeyIndex("name", Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", "Station"));
                }
                
                if (graphNoTx.getEdgeType("Has") == null) {
                    graphNoTx.createEdgeType("Has");
                }
                
                if (graphNoTx.getVertexType("Commodity") == null) {
                    graphNoTx.createVertexType("Commodity");
                    graphNoTx.createKeyIndex("name", Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", "Commodity"));
                }
                
                if (graphNoTx.getEdgeType("Exchange") == null) {
                    graphNoTx.createEdgeType("Exchange");
                }
                

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    private Configuration dbConfig() {
        BaseConfiguration c = new BaseConfiguration();
        c.addProperty("blueprints.orientdb.url", "plocal:" + settings.getOrientGraphDb());
        c.addProperty("blueprints.orientdb.username", "admin");
        c.addProperty("blueprints.orientdb.password", "admin");
        c.addProperty("blueprints.orientdb.lightweightEdges", Boolean.FALSE);
        return c;
    }

    /**
     * @return the factory
     */
    public OrientGraphFactory getFactory() {
        
        startDb();
        
        return factory;
    }
}
