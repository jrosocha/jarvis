package com.jhr.jarvis.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BestExchange implements Comparable<BestExchange>{

    private StringProperty buySystemName = new SimpleStringProperty("");
    private StringProperty buyStationName = new SimpleStringProperty("");
    private StringProperty commodity = new SimpleStringProperty("");
    private IntegerProperty buyPrice = new SimpleIntegerProperty(0);
    private IntegerProperty supply = new SimpleIntegerProperty(0);
    private ObjectProperty<LocalDateTime> buyStationDataAge = new SimpleObjectProperty<LocalDateTime>(LocalDateTime.now());

    private StringProperty sellSystemName = new SimpleStringProperty("");
    private StringProperty sellStationName = new SimpleStringProperty("");
    private IntegerProperty sellPrice = new SimpleIntegerProperty(0);
    private IntegerProperty demand = new SimpleIntegerProperty(0);
    private ObjectProperty<LocalDateTime> sellStationDataAge = new SimpleObjectProperty<LocalDateTime>(LocalDateTime.now());
    
    private IntegerProperty perUnitProfit = new SimpleIntegerProperty(0);
    private IntegerProperty quantity = new SimpleIntegerProperty(0);
    
    private DoubleProperty distanceFromOrigin = new SimpleDoubleProperty(0);
    private Set<BestExchange> nextTrip = new ConcurrentSkipListSet<BestExchange>();
    private int routePerProfitUnit = 0;
    private BestExchange parent = null;
    
    public BestExchange() {
        super();
    }
    
    public BestExchange(Station fromStation, Station toStation, Commodity buyCommodity, Commodity sellCommodity, int quantity) {
        super();
        setBuySystemName(fromStation.getSystem());
        setBuyStationName(fromStation.getName());
        setCommodity(buyCommodity.getName());
        setBuyPrice(buyCommodity.getBuyPrice());
        setSupply(buyCommodity.getSupply());
        setSellSystemName(toStation.getSystem());
        setSellStationName(toStation.getName());
        setSellPrice(sellCommodity.getSellPrice());
        setDemand(sellCommodity.getDemand());
        setPerUnitProfit(sellCommodity.getSellPrice() - buyCommodity.getBuyPrice());
        setQuantity(quantity);
    }
    
    public Map<String, Object> toMap(Integer index) {
        
        Map<String, Object> out = new HashMap<>();
        if (index != null) {
            out.put("#", index);
        }
        
        out.put("FROM SYSTEM", buySystemName);
        out.put("FROM STATION", buyStationName);
        out.put("COMMODITY", commodity);
        out.put("BUY @", buyPrice);
        out.put("SUPPLY", supply);
        out.put("CARGO COST", getBuyPrice() * getQuantity());
         out.put("TO SYSTEM", sellSystemName);
        out.put("TO STATION", sellStationName);
        out.put("SELL @", sellPrice);
        out.put("DEMAND", demand);
        out.put("UNIT PROFIT", perUnitProfit);
        out.put("PROFIT", getPerUnitProfit() * getQuantity());
        out.put("ROUTE UNIT PROFIT", routePerProfitUnit);
        out.put("ROUTE PROFIT", getRoutePerProfitUnit() * getQuantity());
        
        return out;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "BestExchange [buySystemName=" + buySystemName + ", buyStationName=" + buyStationName + ", commodity=" + commodity + ", buyPrice=" + buyPrice + ", supply=" + supply
                + ", sellSystemName=" + sellSystemName + ", sellStationName=" + sellStationName + ", sellPrice=" + sellPrice + ", demand=" + demand + ", perUnitProfit=" + perUnitProfit + "]";
    }
    
    
    /* accessors / mutators */
    
    /**
     * @return the buySystemName
     */
    public String getBuySystemName() {
        return buySystemName.get();
    }
    
    public StringProperty getBuySystemNameProperty() {
        return buySystemName;
    }

    /**
     * @param buySystemName the buySystemName to set
     */
    public void setBuySystemName(String buySystemName) {
        this.buySystemName.set(buySystemName);
    }

    /**
     * @return the buyStationName
     */
    public String getBuyStationName() {
        return buyStationName.get();
    }
    
    public StringProperty getBuyStationNameProperty() {
        return buyStationName;
    }

    /**
     * @param buyStationName the buyStationName to set
     */
    public void setBuyStationName(String buyStationName) {
        this.buyStationName.set(buyStationName);
    }

    /**
     * @return the commodity
     */
    public String getCommodity() {
        return commodity.get();
    }
    
    public StringProperty getCommodityProperty() {
        return commodity;
    }

    /**
     * @param commodity the commodity to set
     */
    public void setCommodity(String commodity) {
        this.commodity.set(commodity);
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
     * @return the sellSystemName
     */
    public String getSellSystemName() {
        return sellSystemName.get();
    }
    
    public StringProperty getSellSystemNameProperty() {
        return sellSystemName;
    }

    /**
     * @param sellSystemName the sellSystemName to set
     */
    public void setSellSystemName(String sellSystemName) {
        this.sellSystemName.set(sellSystemName);
    }

    /**
     * @return the sellStationName
     */
    public String getSellStationName() {
        return sellStationName.get();
    }
    
    public StringProperty getSellStationNameProperty() {
        return sellStationName;
    }

    /**
     * @param sellStationName the sellStationName to set
     */
    public void setSellStationName(String sellStationName) {
        this.sellStationName.set(sellStationName);
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
        return demand;
    }

    /**
     * @param demand the demand to set
     */
    public void setDemand(int demand) {
        this.demand.set(demand);
    }

    /**
     * @return the perUnitProfit
     */
    public int getPerUnitProfit() {
        return perUnitProfit.get();
    }
    
    public IntegerProperty getPerUnitProfitProperty() {
        return perUnitProfit;
    }

    /**
     * @param perUnitProfit the perUnitProfit to set
     */
    public void setPerUnitProfit(int perUnitProfit) {
        this.perUnitProfit.set(perUnitProfit);
    }

    /**
     * @return the quantity
     */
    public int getQuantity() {
        return quantity.get();
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    /**
     * @return the nextTrip
     */
    public Set<BestExchange> getNextTrip() {
        return nextTrip;
    }

    /**
     * @param nextTrip the nextTrip to set
     */
    public void setNextTrip(Set<BestExchange> nextTrip) {
        this.nextTrip = nextTrip;
    }

    /**
     * @return the routePerProfitUnit
     */
    public int getRoutePerProfitUnit() {
        return routePerProfitUnit;
    }

    /**
     * @param routePerProfitUnit the routePerProfitUnit to set
     */
    public void setRoutePerProfitUnit(int routePerProfitUnit) {
        this.routePerProfitUnit = routePerProfitUnit;
    }

    /**
     * @return the parent
     */
    public BestExchange getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(BestExchange parent) {
        this.parent = parent;
    }
    
    public int getExchangeStopProfit() {
        return getPerUnitProfit() * getQuantity();
    }

    public LocalDateTime getBuyStationDataAge() {
        return buyStationDataAge.get();
    }
    
    public ObjectProperty<LocalDateTime> getBuyStationDataAgeProperty() {
        return buyStationDataAge;
    }

    public void setBuyStationDataAge(long buyPriceAge) {
        Instant instant = Instant.ofEpochMilli(buyPriceAge);
        this.buyStationDataAge.set(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    public LocalDateTime getSellStationDataAge() {
        return sellStationDataAge.get();
    }
    
    public ObjectProperty<LocalDateTime> getSellStationDataAgeProperty() {
        return sellStationDataAge;
    }

    public void setSellStationDataAge(long sellPriceAge) {
        Instant instant = Instant.ofEpochMilli(sellPriceAge);
        this.sellStationDataAge.set(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    public double getDistanceFromOrigin() {
        return distanceFromOrigin.get();
    }
    
    public DoubleProperty getDistanceFromOriginProperty() {
        return distanceFromOrigin;
    }

    public void setDistanceFromOrigin(double distanceFromOrigin) {
        this.distanceFromOrigin.set(distanceFromOrigin);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((buyStationName.get() == null) ? 0 : buyStationName.get().hashCode());
        result = prime * result + ((buySystemName.get() == null) ? 0 : buySystemName.get().hashCode());
        result = prime * result + ((commodity.get() == null) ? 0 : commodity.get().hashCode());
        result = prime * result + ((sellStationName.get() == null) ? 0 : sellStationName.get().hashCode());
        result = prime * result + ((sellSystemName.get() == null) ? 0 : sellSystemName.get().hashCode());
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
        BestExchange other = (BestExchange) obj;
        if (buyStationName == null) {
            if (other.buyStationName != null)
                return false;
        } else if (!buyStationName.get().equals(other.buyStationName.get()))
            return false;
        if (buySystemName == null) {
            if (other.buySystemName != null)
                return false;
        } else if (!buySystemName.get().equals(other.buySystemName.get()))
            return false;
        if (commodity == null) {
            if (other.commodity != null)
                return false;
        } else if (!commodity.get().equals(other.commodity.get()))
            return false;
        if (sellStationName == null) {
            if (other.sellStationName != null)
                return false;
        } else if (!sellStationName.get().equals(other.sellStationName.get()))
            return false;
        if (sellSystemName == null) {
            if (other.sellSystemName != null)
                return false;
        } else if (!sellSystemName.get().equals(other.sellSystemName.get()))
            return false;
        return true;
    }

    @Override
    public int compareTo(BestExchange o) {        
        return  new Integer(this.hashCode()).compareTo(new Integer(o.hashCode()));
    }
   
}
