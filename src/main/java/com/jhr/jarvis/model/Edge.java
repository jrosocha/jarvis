
package com.jhr.jarvis.model;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "source",
    "target",
    "value"
})
public class Edge {

    @JsonProperty("source")
    private Integer source;
    @JsonProperty("target")
    private Integer target;
    @JsonProperty("value")
    private Double value;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Edge(Integer source, Integer target, Double value) {
        super();
        this.source = source;
        this.target = target;
        this.value = value;
    }
    
    public Edge(){}
    
    /**
     * 
     * @return
     *     The source
     */
    @JsonProperty("source")
    public Integer getSource() {
        return source;
    }

    /**
     * 
     * @param source
     *     The source
     */
    @JsonProperty("source")
    public void setSource(Integer source) {
        this.source = source;
    }

    /**
     * 
     * @return
     *     The target
     */
    @JsonProperty("target")
    public Integer getTarget() {
        return target;
    }

    /**
     * 
     * @param target
     *     The target
     */
    @JsonProperty("target")
    public void setTarget(Integer target) {
        this.target = target;
    }

    /**
     * 
     * @return
     *     The value
     */
    @JsonProperty("value")
    public Double getValue() {
        return value;
    }

    /**
     * 
     * @param value
     *     The value
     */
    @JsonProperty("value")
    public void setValue(Double value) {
        this.value = value;
    }

    @JsonIgnore
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonIgnore
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
