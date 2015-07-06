
package com.jhr.jarvis.model.eddn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "commodities",
    "timestamp",
    "systemName",
    "stationName"
})
public class Message {

    @JsonProperty("commodities")
    private List<Commodity> commodities = new ArrayList<Commodity>();
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("systemName")
    private String systemName;
    @JsonProperty("stationName")
    private String stationName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The commodities
     */
    @JsonProperty("commodities")
    public List<Commodity> getCommodities() {
        return commodities;
    }

    /**
     * 
     * @param commodities
     *     The commodities
     */
    @JsonProperty("commodities")
    public void setCommodities(List<Commodity> commodities) {
        this.commodities = commodities;
    }

    /**
     * 
     * @return
     *     The timestamp
     */
    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * 
     * @param timestamp
     *     The timestamp
     */
    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 
     * @return
     *     The systemName
     */
    @JsonProperty("systemName")
    public String getSystemName() {
        return systemName;
    }

    /**
     * 
     * @param systemName
     *     The systemName
     */
    @JsonProperty("systemName")
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    /**
     * 
     * @return
     *     The stationName
     */
    @JsonProperty("stationName")
    public String getStationName() {
        return stationName;
    }

    /**
     * 
     * @param stationName
     *     The stationName
     */
    @JsonProperty("stationName")
    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
