
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
import org.joda.time.DateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "uploaderID",
    "softwareName",
    "softwareVersion",
    "gatewayTimestamp"
})
public class Header {

    @JsonProperty("uploaderID")
    private String uploaderID;
    @JsonProperty("softwareName")
    private String softwareName;
    @JsonProperty("softwareVersion")
    private String softwareVersion;
    /**
     * Timestamp upon receipt at the gateway. If present, this property will be overwritten by the gateway; submitters are not intended to populate this property.
     * 
     */
    @JsonProperty("gatewayTimestamp")
    private DateTime gatewayTimestamp;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     * Timestamp upon receipt at the gateway. If present, this property will be overwritten by the gateway; submitters are not intended to populate this property.
     * 
     * @return
     *     The gatewayTimestamp
     */
    @JsonProperty("gatewayTimestamp")
    public DateTime getGatewayTimestamp() {
        return gatewayTimestamp;
    }

    /**
     * Timestamp upon receipt at the gateway. If present, this property will be overwritten by the gateway; submitters are not intended to populate this property.
     * 
     * @param gatewayTimestamp
     *     The gatewayTimestamp
     */
    @JsonProperty("gatewayTimestamp")
    public void setGatewayTimestamp(DateTime gatewayTimestamp) {
        this.gatewayTimestamp = gatewayTimestamp;
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
