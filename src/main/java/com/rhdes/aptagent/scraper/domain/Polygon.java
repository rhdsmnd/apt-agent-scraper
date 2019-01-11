package com.rhdes.aptagent.scraper.domain;

import java.util.Collection;
import java.util.Iterator;

public class Polygon {
    Collection<Location> points;

    public Polygon(Collection<Location> points) {
        this.points = points;
    }

    public Iterator<Location> points() {
        return points.iterator();
    }
}
