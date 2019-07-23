package com.rhdes.scraper;

import com.rhdes.aptagent.scraper.Scraper;
import com.rhdes.aptagent.scraper.domain.ScraperConfig;

public class ScraperRunner {

    public static void main(String[] args) {
        Scraper clScraper = new Scraper();
        clScraper.handleRequest(new ScraperConfig("2019/06/13 18:10"), null);
        System.out.println("Hello world");
    }
}