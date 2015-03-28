package com.jhr.jarvis.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class BestExchange {

    private String buySystemName;
    private String buyStationName;
    private String commodity;
    private int buyPrice;
    private int supply;

    private String sellSystemName;
    private String sellStationName;
    private int sellPrice;
    private int demand;
    
    private int perUnitProfit;
    private int quantity;
    
    private List<BestExchange> nextTrip = new CopyOnWriteArrayList<BestExchange>();
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
        out.put("CARGO COST", buyPrice * quantity);
         out.put("TO SYSTEM", sellSystemName);
        out.put("TO STATION", sellStationName);
        out.put("SELL @", sellPrice);
        out.put("DEMAND", demand);
        out.put("UNIT PROFIT", perUnitProfit);
        out.put("PROFIT", perUnitProfit * quantity);
        out.put("ROUTE UNIT PROFIT", routePerProfitUnit);
        out.put("ROUTE PROFIT", routePerProfitUnit * quantity);
        
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
        return buySystemName;
    }

    /**
     * @param buySystemName the buySystemName to set
     */
    public void setBuySystemName(String buySystemName) {
        this.buySystemName = buySystemName;
    }

    /**
     * @return the buyStationName
     */
    public String getBuyStationName() {
        return buyStationName;
    }

    /**
     * @param buyStationName the buyStationName to set
     */
    public void setBuyStationName(String buyStationName) {
        this.buyStationName = buyStationName;
    }

    /**
     * @return the commodity
     */
    public String getCommodity() {
        return commodity;
    }

    /**
     * @param commodity the commodity to set
     */
    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    /**
     * @return the buyPrice
     */
    public int getBuyPrice() {
        return buyPrice;
    }

    /**
     * @param buyPrice the buyPrice to set
     */
    public void setBuyPrice(int buyPrice) {
        this.buyPrice = buyPrice;
    }

    /**
     * @return the supply
     */
    public int getSupply() {
        return supply;
    }

    /**
     * @param supply the supply to set
     */
    public void setSupply(int supply) {
        this.supply = supply;
    }

    /**
     * @return the sellSystemName
     */
    public String getSellSystemName() {
        return sellSystemName;
    }

    /**
     * @param sellSystemName the sellSystemName to set
     */
    public void setSellSystemName(String sellSystemName) {
        this.sellSystemName = sellSystemName;
    }

    /**
     * @return the sellStationName
     */
    public String getSellStationName() {
        return sellStationName;
    }

    /**
     * @param sellStationName the sellStationName to set
     */
    public void setSellStationName(String sellStationName) {
        this.sellStationName = sellStationName;
    }

    /**
     * @return the sellPrice
     */
    public int getSellPrice() {
        return sellPrice;
    }

    /**
     * @param sellPrice the sellPrice to set
     */
    public void setSellPrice(int sellPrice) {
        this.sellPrice = sellPrice;
    }

    /**
     * @return the demand
     */
    public int getDemand() {
        return demand;
    }

    /**
     * @param demand the demand to set
     */
    public void setDemand(int demand) {
        this.demand = demand;
    }

    /**
     * @return the perUnitProfit
     */
    public int getPerUnitProfit() {
        return perUnitProfit;
    }

    /**
     * @param perUnitProfit the perUnitProfit to set
     */
    public void setPerUnitProfit(int perUnitProfit) {
        this.perUnitProfit = perUnitProfit;
    }

    /**
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * @return the nextTrip
     */
    public List<BestExchange> getNextTrip() {
        return nextTrip;
    }

    /**
     * @param nextTrip the nextTrip to set
     */
    public void setNextTrip(List<BestExchange> nextTrip) {
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

    
}
