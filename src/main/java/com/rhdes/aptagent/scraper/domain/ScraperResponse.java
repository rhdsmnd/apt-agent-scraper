package com.rhdes.aptagent.scraper.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class ScraperResponse {
    private int numberOfListingsParsed;
    private Collection<Listing> savedListings;
    private String earliestParsedListingDate;

    public ScraperResponse(int numberOfListings, Collection<Listing> savedListings,
                                 String earliestParsedListingDate) {
        this.setNumberOfListingsParsed(numberOfListings);
        this.setSavedListings(savedListings);
        this.setEarliestParsedListingDate(earliestParsedListingDate);
    }

    public ScraperResponse(int numberOfListings, String earliestParsedListingDate) {
        this.setNumberOfListingsParsed(numberOfListings);
        this.setSavedListings(new ArrayList<Listing>());
        this.setEarliestParsedListingDate(earliestParsedListingDate);
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

    public int getNumberOfListingsParsed() {
        return numberOfListingsParsed;
    }

    public void setNumberOfListingsParsed(int numberOfListingsParsed) {
        this.numberOfListingsParsed = numberOfListingsParsed;
    }
}
