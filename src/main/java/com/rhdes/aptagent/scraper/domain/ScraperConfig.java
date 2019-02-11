package com.rhdes.aptagent.scraper.domain;

import java.math.BigDecimal;
import java.util.Collection;

public class ScraperConfig {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private int minBedrooms;
    private int maxBedrooms;
    private Collection<Polygon> desiredAreas;
    private String lastSeen;

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public int getMinBedrooms() {
        return minBedrooms;
    }

    public void setMinBedrooms(int minBedrooms) {
        this.minBedrooms = minBedrooms;
    }

    public int getMaxBedrooms() {
        return maxBedrooms;
    }

    public void setMaxBedrooms(int maxBedrooms) {
        this.maxBedrooms = maxBedrooms;
    }

    public Collection<Polygon> getDesiredAreas() {
        return desiredAreas;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }
}
