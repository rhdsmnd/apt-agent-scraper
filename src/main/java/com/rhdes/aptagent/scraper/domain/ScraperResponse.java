package com.rhdes.aptagent.scraper.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class ScraperResponse {
    private int numberOfListings;
    private Collection<Listing> savedListings;
    private String earliestParsedListingDate;

    public ScraperResponse(int numberOfListings, Collection<Listing> savedListings,
                                 String earliestParsedListingDate) {
        this.setNumberOfListings(numberOfListings);
        this.setSavedListings(savedListings);
        this.setEarliestParsedListingDate(earliestParsedListingDate);
    }

    public ScraperResponse(int numberOfListings, String earliestParsedListingDate) {
        this.setNumberOfListings(numberOfListings);
        this.setSavedListings(new ArrayList<Listing>());
        this.setEarliestParsedListingDate(earliestParsedListingDate);
    }

    public int getNumberOfListings() {
        return numberOfListings;
    }

    public void setNumberOfListings(int numberOfListings) {
        this.numberOfListings = numberOfListings;
    }

    public Collection<Listing> getSavedListings() {
        return savedListings;
    }

    public void setSavedListings(Collection<Listing> savedListings) {
        this.savedListings = savedListings;
    }

    public String getEarliestParsedListingDate() {
        return earliestParsedListingDate;
    }

    public void setEarliestParsedListingDate(String earliestParsedListingDate) {
        this.earliestParsedListingDate = earliestParsedListingDate;
    }

}
