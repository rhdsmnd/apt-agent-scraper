package com.rhdes.aptagent.scraper.domain;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

public class ScraperConfig {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private int minBedrooms;
    private int maxBedrooms;
    private Collection<Polygon> desiredAreas;
    private Date lastSeen;
    private URL listingsUrl;

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

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public URL getBaseUrl() {
        return listingsUrl;
    }

    public void setBaseUrl(URL newUrl) {
        this.listingsUrl = newUrl;
    }
}
