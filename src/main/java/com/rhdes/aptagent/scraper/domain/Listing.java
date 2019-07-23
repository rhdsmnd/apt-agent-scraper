package com.rhdes.aptagent.scraper.domain;

import java.math.BigDecimal;
import java.util.Date;


public class Listing {
    private String title;
    private Date date;
    private int price;
    private Integer bedrooms;
    private Integer sqFeet;
    private String neighborhood;
    private Location loc;
    private String href;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public boolean isValid() {
        return href != null && loc != null && date != null && price > 0;
    }

    @Override
    public String toString() {
        return "{"
                + "\n    title: \"" + this.title + "\""
                + "\n    date: " + this.date
                + "\n    price: " + this.price
                + "\n    bedrooms: " + this.bedrooms
                + "\n    square feet: " + this.sqFeet
                + "\n    location: " + this.loc
                + "\n    link: " + this.href
                + "\n}";
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Integer getSqFeet() {
        return sqFeet;
    }

    public void setSqFeet(Integer sqFeet) {
        this.sqFeet = sqFeet;
    }
}
