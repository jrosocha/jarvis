package com.jhr.jarvis.model;

public class Ship {

    public int cargoSpace;
    public int cash;
    public float jumpDistance;
    
    public Ship() {
        super();
    }
    
    public Ship(int cargoSpace, float jumpDistance, int cash) {
        super();
        this.cargoSpace = cargoSpace;
        this.cash = cash;
        this.jumpDistance = jumpDistance;
    }
    
    @Override
    public String toString() {
        return "Ship [cargoSpace=" + cargoSpace + ", jumpDistance=" + jumpDistance + ", cash=" + cash + "]";
    }    
    
    /**
     * @return the cargoSpace
     */
    public int getCargoSpace() {
        return cargoSpace;
    }
    /**
     * @param cargoSpace the cargoSpace to set
     */
    public void setCargoSpace(int cargoSpace) {
        this.cargoSpace = cargoSpace;
    }
    /**
     * @return the cash
     */
    public int getCash() {
        return cash;
    }
    /**
     * @param cash the cash to set
     */
    public void setCash(int cash) {
        this.cash = cash;
    }
    /**
     * @return the jumpDistance
     */
    public float getJumpDistance() {
        return jumpDistance;
    }
    /**
     * @param jumpDistance the jumpDistance to set
     */
    public void setJumpDistance(float jumpDistance) {
        this.jumpDistance = jumpDistance;
    } 
}
