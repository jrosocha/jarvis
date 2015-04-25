package com.jhr.jarvis.model;

import java.util.ArrayList;
import java.util.List;

public class StarSystem {

    private String name;
    private float x;
    private float y;
    private float z;
    private List<Station> stations = new ArrayList<>();
    
    public StarSystem(String name, float x, float y, float z) {
        super();
        this.name = name.toUpperCase();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public StarSystem(String name) {
        super();
        this.name = name.toUpperCase();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Float.floatToIntBits(x);
        result = prime * result + Float.floatToIntBits(y);
        result = prime * result + Float.floatToIntBits(z);
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
        StarSystem other = (StarSystem) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
            return false;
        }
        if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
            return false;
        }
        if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "StarSystem [name=" + name + ", x=" + x + ", y=" + y + ", z=" + z + ", stations=" + stations + "]";
    }

    /* accessors / mutators */
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name.toUpperCase();
    }
    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }
    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }
    public float getZ() {
        return z;
    }
    public void setZ(float z) {
        this.z = z;
    }
    
    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }
    
}
