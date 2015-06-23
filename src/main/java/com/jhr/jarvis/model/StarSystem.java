
package com.jhr.jarvis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "x",
    "y",
    "z",
    "faction",
    "population",
    "government",
    "allegiance",
    "state",
    "security",
    "primary_economy",
    "needs_permit",
    "updated_at"
})
public class StarSystem implements Comparable<StarSystem>{

    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("x")
    private Float x;
    @JsonProperty("y")
    private Float y;
    @JsonProperty("z")
    private Float z;
    @JsonProperty("faction")
    private String faction;
    @JsonProperty("population")
    private Long population;
    @JsonProperty("government")
    private String government;
    @JsonProperty("allegiance")
    private String allegiance;
    @JsonProperty("state")
    private String state;
    @JsonProperty("security")
    private String security;
    @JsonProperty("primary_economy")
    private String primaryEconomy;
    @JsonProperty("secondary_economy")
    private String secondaryEconomy;
    @JsonProperty("needs_permit")
    private Boolean needsPermit;
    @JsonProperty("updated_at")
    private Long updatedAt;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    
    private List<Station> stations = new ArrayList<>();

    public StarSystem(String name) {
        super();
        this.name = name.toUpperCase();
    }
    
    public StarSystem(String name, float x, float y, float z) {
        super();
        this.name = name.toUpperCase();
        this.x = new Float(x);
        this.y = new Float(y);
        this.z = new Float(z);
    }
    
    public StarSystem() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

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
        this.name = name.toUpperCase();
    }

    /**
     * 
     * @return
     *     The x
     */
    @JsonProperty("x")
    public Float getX() {
        return x;
    }

    /**
     * 
     * @param x
     *     The x
     */
    @JsonProperty("x")
    public void setX(Float x) {
        this.x = x;
    }

    /**
     * 
     * @return
     *     The y
     */
    @JsonProperty("y")
    public Float getY() {
        return y;
    }

    /**
     * 
     * @param y
     *     The y
     */
    @JsonProperty("y")
    public void setY(Float y) {
        this.y = y;
    }

    /**
     * 
     * @return
     *     The z
     */
    @JsonProperty("z")
    public Float getZ() {
        return z;
    }

    /**
     * 
     * @param z
     *     The z
     */
    @JsonProperty("z")
    public void setZ(Float z) {
        this.z = z;
    }

    /**
     * 
     * @return
     *     The faction
     */
    @JsonProperty("faction")
    public String getFaction() {
        return faction;
    }

    /**
     * 
     * @param faction
     *     The faction
     */
    @JsonProperty("faction")
    public void setFaction(String faction) {
        this.faction = faction != null ? faction.toUpperCase() : null;
    }

    /**
     * 
     * @return
     *     The population
     */
    @JsonProperty("population")
    public Long getPopulation() {
        return population;
    }

    /**
     * 
     * @param population
     *     The population
     */
    @JsonProperty("population")
    public void setPopulation(Long population) {
        this.population = population;
    }

    /**
     * 
     * @return
     *     The government
     */
    @JsonProperty("government")
    public String getGovernment() {
        return government;
    }

    /**
     * 
     * @param government
     *     The government
     */
    @JsonProperty("government")
    public void setGovernment(String government) {
        this.government = government != null ? government.toUpperCase() : null;
    }

    /**
     * 
     * @return
     *     The allegiance
     */
    @JsonProperty("allegiance")
    public String getAllegiance() {
        return allegiance;
    }

    /**
     * 
     * @param allegiance
     *     The allegiance
     */
    @JsonProperty("allegiance")
    public void setAllegiance(String allegiance) {
        this.allegiance = allegiance != null ? allegiance.toUpperCase() : null;
    }

    /**
     * 
     * @return
     *     The state
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     * 
     * @param state
     *     The state
     */
    @JsonProperty("state")
    public void setState(String state) {
        this.state = state != null ? state.toUpperCase() : null;
    }

    /**
     * 
     * @return
     *     The security
     */
    @JsonProperty("security")
    public String getSecurity() {
        return security;
    }

    /**
     * 
     * @param security
     *     The security
     */
    @JsonProperty("security")
    public void setSecurity(String security) {
        this.security = security != null ? security.toUpperCase() : null;
    }

    /**
     * 
     * @return
     *     The primaryEconomy
     */
    @JsonProperty("primary_economy")
    public String getPrimaryEconomy() {
        return primaryEconomy;
    }

    /**
     * 
     * @param primaryEconomy
     *     The primary_economy
     */
    @JsonProperty("primary_economy")
    public void setPrimaryEconomy(String primaryEconomy) {
        this.primaryEconomy = primaryEconomy != null ? primaryEconomy.toUpperCase() : null;
    }

    @JsonProperty("secondary_economy")
    public void setSecondaryEconomy(String economy) {
        this.primaryEconomy = economy != null ? economy.toUpperCase() : null;
    }

    @JsonProperty("secondary_economy")
    public String getSecondaryEconomy() {
        return secondaryEconomy;
    }


    /**
     * 
     * @return
     *     The needsPermit
     */
    @JsonProperty("needs_permit")
    public Boolean getNeedsPermit() {
        return needsPermit;
    }

    /**
     * 
     * @param needsPermit
     *     The needs_permit
     */
    @JsonProperty("needs_permit")
    public void setNeedsPermit(Boolean needsPermit) {
        this.needsPermit = needsPermit;
    }

    /**
     * 
     * @return
     *     The updatedAt
     */
    @JsonProperty("updated_at")
    public Long getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 
     * @param updatedAt
     *     The updated_at
     */
    @JsonProperty("updated_at")
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        StarSystem other = (StarSystem) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StarSystem [id=" + id + ", name=" + name + ", x=" + x + ", y=" + y + ", z=" + z + ", faction=" + faction + ", population=" + population + ", government=" + government
                + ", allegiance=" + allegiance + ", state=" + state + ", security=" + security + ", primaryEconomy=" + primaryEconomy + ", secondaryEconomy=" + secondaryEconomy + ", needsPermit="
                + needsPermit + ", updatedAt=" + updatedAt + ", additionalProperties=" + additionalProperties + ", stations=" + stations + "]";
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    @Override
    public int compareTo(StarSystem o) {
        return this.name.compareTo(o.getName());
    }

}
