package com.jhr.jarvis.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Station {
    
    @JsonIgnore
    private StringProperty system = new SimpleStringProperty("");
    @JsonIgnore
    private StringProperty name = new SimpleStringProperty("");
    @JsonIgnore
    private BooleanProperty blackMarket = new SimpleBooleanProperty(false);
    @JsonIgnore
    private final ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<LocalDateTime>(LocalDateTime.now());

    private List<Commodity> availableCommodityExchanges = new ArrayList<>();
    
    public Station(String name, String system) {
        super();
        
        if (name.contains("@")) {
            setName(name);
        } else {
            setName(name + "@" + system);
        }
        setSystem(system);
    }
    
    public Station(String name, String system, long date) {
        super();
        if (name.contains("@")) {
            setName(name);
        } else {
            setName(name + "@" + system);
        }
        setSystem(system);
        setDate(date);
    }

    public Station() {
        super();
    }

    @Override
	public String toString() {
		return "Station [system=" + system + ", name=" + name
				+ ", blackMarket=" + blackMarket + ", date=" + date + "]";
	}
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Station other = (Station) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    
    /**
     * @return the name
     */
    @JsonProperty("name")
    public String getName() {
        return name.get();
    }
    
    @JsonIgnore
    public StringProperty getNameProperty() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name.set(name.toUpperCase());
    }

    /**
     * @return the system
     */
    @JsonProperty("system")
    public String getSystem() {
        return system.get();
    }
    
    @JsonIgnore
    public StringProperty getSystemProperty() {
        return system;
    }

    /**
     * @param system the system to set
     */
    public void setSystem(String system) {
        this.system.set(system.toUpperCase());
    }

    /**
     * @return the date
     */
    @JsonProperty("date")
    public LocalDateTime getDate() {
        return date.get();
        //return date.get().toEpochSecond(ZoneOffset.UTC);
    }

    @JsonIgnore
    public ObjectProperty<LocalDateTime> getDateProperty() {
        return date;
    }
    
    /**
     * @param date the date to set
     */
    public void setDate(long date) {
        Instant instant = Instant.ofEpochMilli(date);
        this.date.set(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }
    
    @JsonProperty("blackMarket")
    public Boolean getBlackMarket() {
		return blackMarket.getValue();
	}
    
    @JsonIgnore
    public BooleanProperty getBlackMarketProperty() {
        return blackMarket;
    }

	public void setBlackMarket(Boolean blackMarket) {
		this.blackMarket.set(blackMarket);
	}

    public List<Commodity> getAvailableCommodityExchanges() {
        return availableCommodityExchanges;
    }

}
