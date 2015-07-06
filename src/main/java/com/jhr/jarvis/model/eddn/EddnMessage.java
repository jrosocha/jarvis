
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
    "header",
    "$schemaRef",
    "message"
})
public class EddnMessage {

    @JsonProperty("header")
    private Header header;
    @JsonProperty("$schemaRef")
    private String $schemaRef;
    @JsonProperty("message")
    private Message message;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The header
     */
    @JsonProperty("header")
    public Header getHeader() {
        return header;
    }

    /**
     * 
     * @param header
     *     The header
     */
    @JsonProperty("header")
    public void setHeader(Header header) {
        this.header = header;
    }

    /**
     * 
     * @return
     *     The $schemaRef
     */
    @JsonProperty("$schemaRef")
    public String get$schemaRef() {
        return $schemaRef;
    }

    /**
     * 
     * @param $schemaRef
     *     The $schemaRef
     */
    @JsonProperty("$schemaRef")
    public void set$schemaRef(String $schemaRef) {
        this.$schemaRef = $schemaRef;
    }

    /**
     * 
     * @return
     *     The message
     */
    @JsonProperty("message")
    public Message getMessage() {
        return message;
    }

    /**
     * 
     * @param message
     *     The message
     */
    @JsonProperty("message")
    public void setMessage(Message message) {
        this.message = message;
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
