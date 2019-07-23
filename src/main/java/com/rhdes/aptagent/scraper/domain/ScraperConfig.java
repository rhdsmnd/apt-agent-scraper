package com.rhdes.aptagent.scraper.domain;

public class ScraperConfig {

    public ScraperConfig() {}

    public ScraperConfig(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    private String lastSeen;

    public String getLastSeen() {
        return lastSeen;
    }
    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

}
