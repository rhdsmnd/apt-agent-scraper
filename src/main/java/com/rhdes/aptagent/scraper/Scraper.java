package com.rhdes.aptagent.scraper;

import com.rhdes.aptagent.scraper.domain.Listing;
import com.rhdes.aptagent.scraper.domain.Location;
import com.rhdes.aptagent.scraper.domain.ScraperConfig;
import com.rhdes.aptagent.scraper.exception.ScraperException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Scraper {

    public static final Pattern brRegex = Pattern.compile("\\dbr");
    public static final Pattern sqFeetRegex = Pattern.compile("\\d+ft");

    public static final Logger logger = LogManager.getLogger(Scraper.class);

    public static void main(String[] args) {
        logger.info("Hello");
        String content = null;
        Element craigslistPage = null;
        try {
            craigslistPage = Jsoup.connect("https://sfbay.craigslist.org/d/apts-housing-for-rent/search/apa").get();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unsuccessful http request to get craigslist listings.");
        }

        ScraperConfig config = makeHardCodeScraperConfig();

        Collection<Listing> listings = parseCraigslistPage(craigslistPage, config);

        int num = (listings == null ? 0 : listings.size());

        System.out.println("Retrieved " + num + " listing" + (num == 1 ? "" : "s") + ".");

        if (listings != null) {
            for (Listing current : listings) {
                System.out.println("");
                System.out.println(current);
            }
        }
    }

    private static ScraperConfig makeHardCodeScraperConfig() {
        ScraperConfig ret = new ScraperConfig();

        Date lastSeen = null;
        try {
            lastSeen = new SimpleDateFormat("yyyy/MM/dd").parse("2019/01/01");
        } catch (ParseException e) {
            System.out.println("Unable to parse date: " + e);
        }
        ret.setLastSeen(lastSeen);

        ret.setMinPrice(new BigDecimal("800"));
        ret.setMaxPrice(new BigDecimal("1800"));
        ret.setMinBedrooms(0);
        ret.setMaxBedrooms(1);

        ret.setMaxToFetch(500);

        return ret;
    }

    public static Collection<Listing> parseCraigslistPage(Element craigslistPage, ScraperConfig config) {

        for (Element listing : craigslistPage.getElementsByClass("result-row")) {
            Listing currentListing = new Listing();
            try {
                currentListing.setHref(getLink(listing));
                currentListing.setTitle(getTitle(listing));
                currentListing.setDate(getDate(listing));
                currentListing.setPrice(getPrice(listing));
                currentListing.setNeighborhood(getNeighborhood(listing));
                currentListing.setBedrooms(getBedrooms(listing));
                currentListing.setSqFeet(getSqFeet(listing));
                currentListing.setLoc(getLocation(listing));
            } catch (ScraperException e) {
                System.err.println("Could not parse listing: " + listing);
            }

            System.out.println(currentListing);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static Integer getBedrooms(Element listing) {
        List<Element> bedroomText = listing.getElementsByClass("housing");
        if (bedroomText == null || bedroomText.size() == 0) {
            return null;
        }

        Matcher listingMatcher = brRegex.matcher(bedroomText.get(0).html());
        if (listingMatcher.find()) {
            try {
                return Integer.parseInt(listingMatcher.group().substring(0, 1));
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    private static Integer getSqFeet(Element listing) {
        List<Element> sqFeetText = listing.getElementsByClass("housing");
        if (sqFeetText == null || sqFeetText.size() == 0) {
            return null;
        }

        Matcher listingMatcher = sqFeetRegex.matcher(sqFeetText.get(0).html());
        if (listingMatcher.find()) {
            try {
                // Omit 'ft'
                return Integer.parseInt(listingMatcher.group().substring(0, listingMatcher.group().length() - 2));
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    private static int getPrice(Element listing) {
        List<Element> price = listing.getElementsByClass("result-price");
        if (price == null || price.size() == 0) {
            throw new ScraperException("Could not find listing price.");
        }
        String priceText = price.get(0).html();

        if (priceText == null) {
            throw new ScraperException("Price text is missing.");
        }

        try {
            // omit the $
            return Integer.parseInt(priceText.substring(1));
        } catch (NumberFormatException e) {
            throw new ScraperException("Couldn't parse the listing price: " + e);
        }

    }

    public static Date getDate(Element resultRow) {
        List<Element> timeElements = resultRow.getElementsByTag("time");
        if (timeElements == null || timeElements.size() == 0) {
            throw new ScraperException("Could not find the listing time.");
        }

        String listingDatetime = timeElements.get(0).attr("datetime");
        if (listingDatetime == null) {
            throw new ScraperException("Couldn't find datetime in listing");
        }

        SimpleDateFormat craigDateLayout = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        try {
            Date parsedListingDate = craigDateLayout.parse(listingDatetime);
            return parsedListingDate;
        } catch (ParseException e) {
            throw new ScraperException("Couldn't parse datetime: " + e);
        }
    }

    public static String getLink(Element resultRow) {
        List<Element> title = resultRow.getElementsByClass("result-title");
        if (title == null || title.size() == 0) {
            throw new ScraperException("Could not retrieve link from listing.");
        }

        return title.get(0).attr("href");
    }

    public static String getNeighborhood(Element resultRow) {
        List<Element> neighborhood = resultRow.getElementsByClass("result-hood");
        if (neighborhood == null || neighborhood.size() == 0) {
            return null;
        } else {
            return neighborhood.get(0).html();
        }
    }

    public static String getTitle(Element listing) {
        List<Element> title = listing.getElementsByClass("result-title");
        if (title == null || title.size() == 0) {
            throw new ScraperException("Could not retrieve title from listing.");
        }

        return Jsoup.clean(title.get(0).html(), Whitelist.basic());
    }

    public static Location getLocation(Element listing) {
        String href = null;
        try {
            href = getLink(listing);
        } catch (ScraperException e) {
            throw new ScraperException("Could not retrieve link from listing:\n\t" + e);
        }

        if (href == null || !href.startsWith("https://sfbay.craigslist.org")) {
            throw new ScraperException("Link is not a craigslist link: " + href);
        }

        try {
            Element listingPage = Jsoup.connect(href).get();
            Location location = getLocationFromListingPage(listingPage);
            if (location == null) {
                throw new ScraperException("Could not retrieve location from listing page.");
            }
            return location;
        } catch (IOException e) {
            throw new ScraperException("Error navigating to listing: " + e);
        }
    }

    public static Location getLocationFromListingPage(Element listingDocument) {
        if (listingDocument == null) {
            return null;
        }

        Element map = listingDocument.getElementById("map");
        if (map == null) {
            return null;
        }

        String latStr = map.attr("data-latitude");
        String lonStr = map.attr("data-longitude");
        try {
            BigDecimal latNum = new BigDecimal(latStr);
            BigDecimal lonNum = new BigDecimal(lonStr);
            return new Location(latNum, lonNum);
        } catch (NumberFormatException e) {
            throw new ScraperException("Error parsing listing location: " + e);
        }
    }


}
