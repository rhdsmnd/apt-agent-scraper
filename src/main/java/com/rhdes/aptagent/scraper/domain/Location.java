package com.rhdes.aptagent.scraper.domain;

import java.math.BigDecimal;

public class Location {
    private final BigDecimal lat;
    private final BigDecimal lon;

    public Location(BigDecimal lat, BigDecimal lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    @Override
    public String toString() {
        return "(" + this.lat + ", " + this.lon + ")";
    }
}
