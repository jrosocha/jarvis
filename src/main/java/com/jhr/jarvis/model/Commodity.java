package com.jhr.jarvis.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Commodity {

    private StringProperty name = new SimpleStringProperty("");
    private StringProperty group = new SimpleStringProperty("");
    
    private IntegerProperty buyPrice = new SimpleIntegerProperty(0);
    private IntegerProperty supply = new SimpleIntegerProperty(0);
    private IntegerProperty sellPrice = new SimpleIntegerProperty(0);
    private IntegerProperty demand = new SimpleIntegerProperty(0);
    private ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<LocalDateTime>(LocalDateTime.now());
    
    public Commodity(String name) {
        super();
        setName(name);
    }

    public Commodity(String name, String group) {
        super();
        setName(name);
        setGroup(group);
    }
    
    public Commodity(String name, int buyPrice, int supply, int sellPrice, int demand) {
        super();
        setName(name);
        setBuyPrice(buyPrice);
        setSupply(supply);
        setSellPrice(sellPrice);
        setDemand(demand);
    }
    
    public Commodity(String name, int buyPrice, int supply, int sellPrice, int demand, long date) {
        super();
        setName(name);
        setBuyPrice(buyPrice);
        setSupply(supply);
        setSellPrice(sellPrice);
        setDemand(demand);
        setDate(date);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Commodity [name=" + name + ", group=" + group + ", buyPrice=" + buyPrice + ", supply=" + supply + ", sellPrice=" + sellPrice + ", demand=" + demand + ", date=" + date + "]";
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("COMMODITY", name);
        map.put("GROUP", group);
        map.put("BUY @", buyPrice);
        map.put("SUPPLY", supply);
        map.put("SELL @", sellPrice);
        map.put("DEMAND", demand);
        //map.put("DAYS OLD", (((new Date().getTime() - date)/1000/60/60/24) * 100) / 100);
        
        return map;
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
        Commodity other = (Commodity) obj;
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
    public String getName() {
        return name.get();
    }
    
    public StringProperty getNameProperty() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group.get();
    }
    
    public StringProperty getGroupProperty() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group.set(group);
    }

    /**
     * @return the buyPrice
     */
    public int getBuyPrice() {
        return buyPrice.get();
    }
    
    public IntegerProperty getBuyPriceProperty() {
        return buyPrice;
    }

    /**
     * @param buyPrice the buyPrice to set
     */
    public void setBuyPrice(int buyPrice) {
        this.buyPrice.set(buyPrice);
    }

    /**
     * @return the supply
     */
    public int getSupply() {
        return supply.get();
    }
    
    public IntegerProperty getSupplyProperty() {
        return supply;
    }

    /**
     * @param supply the supply to set
     */
    public void setSupply(int supply) {
        this.supply.set(supply);
    }

    /**
     * @return the sellPrice
     */
    public int getSellPrice() {
        return sellPrice.get();
    }
    
    public IntegerProperty getSellPriceProperty() {
        return sellPrice;
    }

    /**
     * @param sellPrice the sellPrice to set
     */
    public void setSellPrice(int sellPrice) {
        this.sellPrice.set(sellPrice);
    }

    /**
     * @return the demand
     */
    public int getDemand() {
        return demand.get();
    }
    
    public IntegerProperty getDemandProperty() {
        return this.demand;
    }

    /**
     * @param demand the demand to set
     */
    public void setDemand(int demand) {
        this.demand.set(demand);
    }

    public ObjectProperty<LocalDateTime> getDateProperty() {
        return date;
    }
    
    public LocalDateTime getDate() {
        return date.get();
    }

    public void setDate(long date) {
        Instant instant = Instant.ofEpochMilli(date);
        this.date.set(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

}
