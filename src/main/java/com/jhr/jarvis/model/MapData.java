
package com.jhr.jarvis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "nodes",
    "edges"
})
public class MapData {

    @JsonProperty("nodes")
    private List<Node> nodes = new ArrayList<Node>();
    @JsonProperty("edges")
    private List<Edge> edges = new ArrayList<Edge>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The nodes
     */
    @JsonProperty("nodes")
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * 
     * @param nodes
     *     The nodes
     */
    @JsonProperty("nodes")
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    /**
     * 
     * @return
     *     The edges
     */
    @JsonProperty("edges")
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * 
     * @param edges
     *     The edges
     */
    @JsonProperty("edges")
    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    @JsonIgnore
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonIgnore
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
    
    @JsonIgnore
    public List<Double> getOptimalWindowSizeAndAdjustEverythingPositive() {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        List<Double> out = new ArrayList<>();
        
        for (Node n: this.getNodes()) {
            
            if (n.getX() < minX) {
                minX = n.getX();
            }
            
            if (n.getX() > maxX) {
                maxX = n.getX();
            }
            
            if (n.getY() < minY) {
                minY = n.getY();
            }
            
            if (n.getY() > maxY) {
                maxY = n.getY();
            }
        }
        
        out.add(maxX - minX);
        out.add(maxY - minY);
        
        if (minX < 0) {
            double adjust = 0 - minX;
            for (Node n: this.getNodes()) {
                n.setX(n.getX() + adjust);
            }
        }
        
        if (minY < 0) {
            double adjust = 0 - minY;
            for (Node n: this.getNodes()) {
                n.setY(n.getY() + adjust);
            }
        }
        
        return out;
    }

}
