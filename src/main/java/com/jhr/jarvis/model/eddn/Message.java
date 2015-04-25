
package com.jhr.jarvis.model.eddn;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "systemName",
    "stationName",
    "itemName",
    "buyPrice",
    "stationStock",
    "supplyLevel",
    "sellPrice",
    "demand",
    "demandLevel",
    "timestamp"
})
public class Message {

    @JsonProperty("systemName")
    private String systemName;
    @JsonProperty("stationName")
    private String stationName;
    @JsonProperty("itemName")
    private String itemName;
    /**
     * Price to buy from the market
     * 
     */
    @JsonProperty("buyPrice")
    private Long buyPrice;
    @JsonProperty("stationStock")
    private Long stationStock;
    @JsonProperty("supplyLevel")
    private Message.SupplyLevel supplyLevel;
    /**
     * Price to sell to the market
     * 
     */
    @JsonProperty("sellPrice")
    private Long sellPrice;
    @JsonProperty("demand")
    private Long demand;
    @JsonProperty("demandLevel")
    private Message.DemandLevel demandLevel;
    @JsonProperty("timestamp")
    private DateTime timestamp;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    /**
     * 
     * @return
     *     The itemName
     */
    @JsonProperty("itemName")
    public String getItemName() {
        return itemName;
    }

    /**
     * 
     * @param itemName
     *     The itemName
     */
    @JsonProperty("itemName")
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * Price to buy from the market
     * 
     * @return
     *     The buyPrice
     */
    @JsonProperty("buyPrice")
    public Long getBuyPrice() {
        return buyPrice;
    }

    /**
     * Price to buy from the market
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
     *     The stationStock
     */
    @JsonProperty("stationStock")
    public Long getStationStock() {
        return stationStock;
    }

    /**
     * 
     * @param stationStock
     *     The stationStock
     */
    @JsonProperty("stationStock")
    public void setStationStock(Long stationStock) {
        this.stationStock = stationStock;
    }

    /**
     * 
     * @return
     *     The supplyLevel
     */
    @JsonProperty("supplyLevel")
    public Message.SupplyLevel getSupplyLevel() {
        return supplyLevel;
    }

    /**
     * 
     * @param supplyLevel
     *     The supplyLevel
     */
    @JsonProperty("supplyLevel")
    public void setSupplyLevel(Message.SupplyLevel supplyLevel) {
        this.supplyLevel = supplyLevel;
    }

    /**
     * Price to sell to the market
     * 
     * @return
     *     The sellPrice
     */
    @JsonProperty("sellPrice")
    public Long getSellPrice() {
        return sellPrice;
    }

    /**
     * Price to sell to the market
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
     *     The demandLevel
     */
    @JsonProperty("demandLevel")
    public Message.DemandLevel getDemandLevel() {
        return demandLevel;
    }

    /**
     * 
     * @param demandLevel
     *     The demandLevel
     */
    @JsonProperty("demandLevel")
    public void setDemandLevel(Message.DemandLevel demandLevel) {
        this.demandLevel = demandLevel;
    }

    /**
     * 
     * @return
     *     The timestamp
     */
    @JsonProperty("timestamp")
    public DateTime getTimestamp() {
        return timestamp;
    }

    /**
     * 
     * @param timestamp
     *     The timestamp
     */
    @JsonProperty("timestamp")
    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
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

    @Generated("org.jsonschema2pojo")
    public static enum DemandLevel {

        LOW("Low"),
        MED("Med"),
        HIGH("High");
        private final String value;
        private static Map<String, Message.DemandLevel> constants = new HashMap<String, Message.DemandLevel>();

        static {
            for (Message.DemandLevel c: values()) {
                constants.put(c.value, c);
            }
        }

        private DemandLevel(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static Message.DemandLevel fromValue(String value) {
            Message.DemandLevel constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("org.jsonschema2pojo")
    public static enum SupplyLevel {

        LOW("Low"),
        MED("Med"),
        HIGH("High");
        private final String value;
        private static Map<String, Message.SupplyLevel> constants = new HashMap<String, Message.SupplyLevel>();

        static {
            for (Message.SupplyLevel c: values()) {
                constants.put(c.value, c);
            }
        }

        private SupplyLevel(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static Message.SupplyLevel fromValue(String value) {
            Message.SupplyLevel constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
