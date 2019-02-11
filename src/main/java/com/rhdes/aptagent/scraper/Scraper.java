package com.rhdes.aptagent.scraper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.rhdes.aptagent.scraper.domain.Listing;
import com.rhdes.aptagent.scraper.domain.Location;
import com.rhdes.aptagent.scraper.domain.ScraperConfig;
import com.rhdes.aptagent.scraper.domain.ScraperResponse;
import com.rhdes.aptagent.scraper.exception.ScraperException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.LogManager;

public class Scraper implements RequestHandler<ScraperConfig, ScraperResponse> {

    public static final Pattern BR_REGEX = Pattern.compile("\\dbr");
    public static final Pattern SQ_FT_REGEX = Pattern.compile("\\d+ft");
    public static final String DT_FORMAT_STR = "yyyy/MM/dd HH:mm";

    @Override
    public ScraperResponse handleRequest(ScraperConfig scraperConfig, Context context) {
        ScraperResponse scraperStats = scrapeListings(scraperConfig);
        System.out.println("Saved " + scraperStats.getSavedListings().size() + " out of "
                + Utils.formatPluralModifier("listing", scraperStats.getNumberOfListings()) + ".");
        return scraperStats;
    }

    public static ScraperResponse scrapeListings(ScraperConfig config) {
        Date currentDateTime = new Date();
        Collection<Listing> savedListings = new ArrayList<Listing>();
        int listingsSeen = 0;
        while (true) {
            Element listingsPage = null;
            try {
                listingsPage = fetchCraigslistPage(listingsSeen);
            } catch (IOException e) {
                break;
            }
            ParsedCraigslistPage parsedPage = parseCraigslistPage(listingsPage, config);
            if (parsedPage.getNumberOfListingsSeen() == 0
                    && !parsedPage.isStopParsing()) {
                System.out.println("No listings found on Craigslist page; exiting.");
                break;
            }

            savedListings.addAll(parsedPage.getSavedListings());
            listingsSeen += parsedPage.getNumberOfListingsSeen();

            if (parsedPage.isStopParsing()) {
                break;
            }
        }

        return new ScraperResponse(listingsSeen, savedListings, new SimpleDateFormat(DT_FORMAT_STR).format(currentDateTime));
    }

    public static ParsedCraigslistPage parseCraigslistPage(Element craigslistPage, ScraperConfig config) {
        ArrayList<Listing> savedListings = new ArrayList<Listing>();
        int numSeen = 0;
        boolean stopParsing = false;

        Date lastSeen = null;
        try {
            lastSeen = new SimpleDateFormat(DT_FORMAT_STR).parse(config.getLastSeen());
        } catch (ParseException e) {
            System.err.println("Unable to parse date: " + e);
        }

        for (Element listing : craigslistPage.getElementsByClass("result-row")) {
            Listing currentListing = new Listing();
            try {
                Date listingDate = getDate(listing);
                if (lastSeen != null && listingDate.compareTo(lastSeen)< 0) {
                    System.out.println("Current listing was posted earlier (" + listingDate + ") than the last time the parser was run.  Exiting.");
                    stopParsing = true;
                    break;
                }

                numSeen += 1;

                currentListing.setDate(listingDate);
                currentListing.setHref(getLink(listing));
                currentListing.setTitle(getTitle(listing));
                currentListing.setPrice(getPrice(listing));
                currentListing.setNeighborhood(getNeighborhood(listing));
                currentListing.setBedrooms(getBedrooms(listing));
                currentListing.setSqFeet(getSqFeet(listing));
                currentListing.setLoc(getLocation(listing));

                System.out.println("Saving listing with date " + currentListing.getDate() + ": " + currentListing.getTitle());

                savedListings.add(currentListing);
            } catch (ScraperException e) {
                System.err.println("Could not parse listing: " + listing);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        return new ParsedCraigslistPage(savedListings, numSeen, stopParsing);

    }

    private static Integer getBedrooms(Element listing) {
        List<Element> bedroomText = listing.getElementsByClass("housing");
        if (bedroomText == null || bedroomText.size() == 0) {
            return null;
        }

        Matcher listingMatcher = BR_REGEX.matcher(bedroomText.get(0).html());
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

        Matcher listingMatcher = SQ_FT_REGEX.matcher(sqFeetText.get(0).html());
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

    public static Element fetchCraigslistPage(int startListingIndex) throws IOException {
        Element craigslistPage = null;
        String fetchUrl = "https://sfbay.craigslist.org/d/apts-housing-for-rent/search/apa";
        if (startListingIndex > 0) {
            fetchUrl += "?s=" + startListingIndex;
        }
        try {
            System.out.println("Fetching: " + fetchUrl);
            return Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unsuccessful http request to get craigslist listings. Url: " + fetchUrl);
            throw e;
        }
    }

    public static class ParsedCraigslistPage {
        private Collection<Listing> savedListings;
        private int numberOfListingsSeen;
        private boolean stopParsing;

        public ParsedCraigslistPage(Collection<Listing> savedListings, int numberOfListingsSeen, boolean stopParsing) {
            this.setSavedListings(savedListings);
            this.setNumberOfListingsSeen(numberOfListingsSeen);
            this.setStopParsing(stopParsing);
        }

        public Collection<Listing> getSavedListings() {
            return savedListings;
        }

        public void setSavedListings(Collection<Listing> savedListings) {
            this.savedListings = savedListings;
        }

        public int getNumberOfListingsSeen() {
            return numberOfListingsSeen;
        }

        public void setNumberOfListingsSeen(int numberOfListingsSeen) {
            this.numberOfListingsSeen = numberOfListingsSeen;
        }

        public boolean isStopParsing() {
            return stopParsing;
        }

        public void setStopParsing(boolean stopParsing) {
            this.stopParsing = stopParsing;
        }
    }
}
