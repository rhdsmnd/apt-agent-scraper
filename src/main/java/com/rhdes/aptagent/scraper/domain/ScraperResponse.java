package com.rhdes.aptagent.scraper.domain;

import java.util.ArrayList;
import java.util.Collection;

public class ScraperResponse {
    private Collection<Listing> listings;
    private String errorMessage;

    public ScraperResponse(Collection<Listing> savedListings) {
        this.setSavedListings(savedListings);
    }

    public ScraperResponse(String errorMessage) {
        this.setErrorMessage(errorMessage);
    }

    public ScraperResponse() {
        this.setSavedListings(new ArrayList<Listing>());
    }

    public Collection<Listing> getListings() {
        return listings;
    }
    public void setSavedListings(Collection<Listing> listings) {
        this.listings = listings;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.listings = null;
    }
}
