
package com.jhr.jarvis.model.eddn;

import java.util.HashMap;
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
    "softwareVersion",
    "gatewayTimestamp",
    "softwareName",
    "uploaderID"
})
public class Header {

    @JsonProperty("softwareVersion")
    private String softwareVersion;
    @JsonProperty("gatewayTimestamp")
    private String gatewayTimestamp;
    @JsonProperty("softwareName")
    private String softwareName;
    @JsonProperty("uploaderID")
    private String uploaderID;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The softwareVersion
     */
    @JsonProperty("softwareVersion")
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    /**
     * 
     * @param softwareVersion
     *     The softwareVersion
     */
    @JsonProperty("softwareVersion")
    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    /**
     * 
     * @return
     *     The gatewayTimestamp
     */
    @JsonProperty("gatewayTimestamp")
    public String getGatewayTimestamp() {
        return gatewayTimestamp;
    }

    /**
     * 
     * @param gatewayTimestamp
     *     The gatewayTimestamp
     */
    @JsonProperty("gatewayTimestamp")
    public void setGatewayTimestamp(String gatewayTimestamp) {
        this.gatewayTimestamp = gatewayTimestamp;
    }

    /**
     * 
     * @return
     *     The softwareName
     */
    @JsonProperty("softwareName")
    public String getSoftwareName() {
        return softwareName;
    }

    /**
     * 
     * @param softwareName
     *     The softwareName
     */
    @JsonProperty("softwareName")
    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    /**
     * 
     * @return
     *     The uploaderID
     */
    @JsonProperty("uploaderID")
    public String getUploaderID() {
        return uploaderID;
    }

    /**
     * 
     * @param uploaderID
     *     The uploaderID
     */
    @JsonProperty("uploaderID")
    public void setUploaderID(String uploaderID) {
        this.uploaderID = uploaderID;
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
