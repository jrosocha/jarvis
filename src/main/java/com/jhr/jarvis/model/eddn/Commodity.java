
package com.jhr.jarvis.model.eddn;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "buyPrice",
    "supply",
    "demand",
    "sellPrice",
    "demandLevel",
    "supplyLevel"
})
public class Commodity {

    @JsonProperty("name")
    private String name;
    @JsonProperty("buyPrice")
    private Long buyPrice;
    @JsonProperty("supply")
    private Long supply;
    @JsonProperty("demand")
    private Long demand;
    @JsonProperty("sellPrice")
    private Long sellPrice;
    @JsonProperty("demandLevel")
    private String demandLevel;
    @JsonProperty("supplyLevel")
    private String supplyLevel;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The buyPrice
     */
    @JsonProperty("buyPrice")
    public Long getBuyPrice() {
        return buyPrice;
    }

    /**
     * 
     * @param buyPrice
     *     The buyPrice
     */
    @JsonProperty("buyPrice")
    public void setBuyPrice(Long buyPrice) {
        this.buyPrice = buyPrice;
    }

    /**
     * 
     * @return
     *     The supply
     */
    @JsonProperty("supply")
    public Long getSupply() {
        return supply;
    }

    /**
     * 
     * @param supply
     *     The supply
     */
    @JsonProperty("supply")
    public void setSupply(Long supply) {
        this.supply = supply;
    }

    /**
     * 
     * @return
     *     The demand
     */
    @JsonProperty("demand")
    public Long getDemand() {
        return demand;
    }

    /**
     * 
     * @param demand
     *     The demand
     */
    @JsonProperty("demand")
    public void setDemand(Long demand) {
        this.demand = demand;
    }

    /**
     * 
     * @return
     *     The sellPrice
     */
    @JsonProperty("sellPrice")
    public Long getSellPrice() {
        return sellPrice;
    }

    /**
     * 
     * @param sellPrice
     *     The sellPrice
     */
    @JsonProperty("sellPrice")
    public void setSellPrice(Long sellPrice) {
        this.sellPrice = sellPrice;
    }

    /**
     * 
     * @return
     *     The demandLevel
     */
    @JsonProperty("demandLevel")
    public String getDemandLevel() {
        return demandLevel;
    }

    /**
     * 
     * @param demandLevel
     *     The demandLevel
     */
    @JsonProperty("demandLevel")
    public void setDemandLevel(String demandLevel) {
        this.demandLevel = demandLevel;
    }

    /**
     * 
     * @return
     *     The supplyLevel
     */
    @JsonProperty("supplyLevel")
    public String getSupplyLevel() {
        return supplyLevel;
    }

    /**
     * 
     * @param supplyLevel
     *     The supplyLevel
     */
    @JsonProperty("supplyLevel")
    public void setSupplyLevel(String supplyLevel) {
        this.supplyLevel = supplyLevel;
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
