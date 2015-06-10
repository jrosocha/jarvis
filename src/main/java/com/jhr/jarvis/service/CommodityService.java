package com.jhr.jarvis.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhr.jarvis.exceptions.CommodityNotFoundException;
import com.jhr.jarvis.model.Commodity;
import com.jhr.jarvis.model.Settings;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

@Service
public class CommodityService {

    @Autowired
    private Settings settings;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrientDbService orientDbService;
    
    // commodity name->commodity
    private Map<String,Commodity> commodityByName = null;

    /**
     * Loads the Commodity.csv file to memory for use in giving groups to commodities
     * 
     * @param systemsCsvFile
     * @throws IOException
     */
    public synchronized void loadCommodities(File systemsCsvFile) throws IOException {
        Map<String, Commodity> out = Files.lines(systemsCsvFile.toPath()).parallel().map(parseCSVLineToCommodity).collect(Collectors.toMap(Commodity::getName, c->c));
        commodityByName = out;
    }
    
    public Commodity getCommodityByName(String commodity) throws IOException {
        if (commodityByName == null) {
            loadCommodities(new File(settings.getCommodityFile()));
        }
        
        return commodityByName.get(commodity);
    }
    
    public String getCommodityGroup(String commodity) {
        try {
            Commodity c = getCommodityByName(commodity);
            if (c.getGroup() != null) {
                return c.getGroup();
            }            
        } catch (Exception e) {
            return "_UNKNOWN";
        }
        return "_UNKNOWN";
    }
        
    public List<Commodity> findCommoditiesOrientDb(String partial) {
        
        List<Commodity> out = new ArrayList<>();
        
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            
            String whereClause = StringUtils.isBlank(partial) ? "" : " where name like '" + partial.toUpperCase() + "%'"; 
            
            for (Vertex comodityVertex : (Iterable<Vertex>) graph.command(new OCommandSQL("select from Commodity" + whereClause)).execute()) {
                Commodity commodity = new Commodity(comodityVertex.getProperty("name"));
                commodity.setGroup(getCommodityGroup(commodity.getName()));  
                out.add(commodity);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return out;
    }

    
    public Commodity findUniqueCommodityOrientDb(String partial) throws CommodityNotFoundException {
        
        Commodity foundCommodity = null;
        boolean found = false;
        
        try {
            foundCommodity = findExactCommodityOrientDb(partial);
            found = true;
        } catch (Exception e) {
            // not an exact patch. proceed
        }
     
        if (!found) {
            List<Commodity> commodities = findCommoditiesOrientDb(partial);
            
            if (commodities.size() == 0 || commodities.size() > 1 ) {
                throw new CommodityNotFoundException("Unique commodity could not be identified for '" + partial + "'.");
            }
            
            foundCommodity = commodities.get(0);
        }

        return foundCommodity;
    }
    
    public Commodity findExactCommodityOrientDb(String commodityName) throws CommodityNotFoundException {
        
        if (commodityName == null) {
            throw new CommodityNotFoundException("Exact commodity '" + commodityName + "' could not be identified");
        }
        
        Commodity commodity = null;
       
        OrientGraphNoTx graph = null;
        try {
            graph = orientDbService.getFactory().getNoTx();
            OrientVertex comodityVertex = (OrientVertex) graph.getVertexByKey("Commodity.name", commodityName);
            if (comodityVertex != null) {
                commodity = new Commodity(comodityVertex.getProperty("name"));
                commodity.setGroup(getCommodityGroup(commodity.getName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (commodity == null) {
            throw new CommodityNotFoundException("Exact commodity '" + commodity + "' could not be identified");
        }
        
        return commodity;
    }
        
    // commodity line
    // group,commodity.ordinal
    private Function<String, Commodity> parseCSVLineToCommodity = line -> {
        String[] splitLine = line.split(",");
        Commodity c = new Commodity(splitLine[1].toUpperCase(),splitLine[0].toUpperCase());
        return c;
    };

}
