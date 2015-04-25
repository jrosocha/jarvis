
package com.jhr.jarvis.model.eddn;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "$schemaRef",
    "header",
    "message"
})
public class EddnMessage {

    @JsonProperty("$schemaRef")
    private String $schemaRef;
    @JsonProperty("header")
    private Header header;
    /**
     * 
     */
    @JsonProperty("message")
    private Message message;

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

}
