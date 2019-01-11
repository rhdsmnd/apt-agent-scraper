package com.rhdes.aptagent.scraper.exception;

public class ScraperException extends RuntimeException {
    public ScraperException(String msg) {
        super(msg);
    }

    public ScraperException() {
        super();
    }
}