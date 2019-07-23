package com.rhdes.aptagent.scraper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.rhdes.aptagent.scraper.domain.Listing;
import com.rhdes.aptagent.scraper.domain.Location;
import com.rhdes.aptagent.scraper.domain.ScraperConfig;
import com.rhdes.aptagent.scraper.domain.ScraperResponse;
import com.rhdes.aptagent.scraper.exception.ScraperException;
import com.rhdes.aptagent.scraper.exception.ScraperInputException;
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
    public static final Pattern CL_DOMAIN_REGEX = Pattern.compile("[a-z.]+\\.craigslist.org");

    @Override
    public ScraperResponse handleRequest(ScraperConfig scraperConfig, Context context) {
        try {
            ScraperResponse newListings = scrapeListings(scraperConfig);
            if (newListings.getErrorMessage() == null) {
                System.out.println("Saved " + newListings.getListings().size() + " "
                        + Utils.formatPluralModifier("listing", newListings.getListings().size()) + ".");
            } else {
                System.out.println("Returning error to client: " + newListings.getErrorMessage());
            }
            return newListings;
        } catch (ScraperInputException e) {
            System.out.println("Error encountered while running scraper.");
            return new ScraperResponse(e.getMessage());
        }
    }

    public static ScraperResponse scrapeListings(ScraperConfig config) throws ScraperException {
        Date lastSeen = null;
        try {
            lastSeen = parseInputDate(config.getLastSeen());
        } catch(ScraperInputException e) {
            return new ScraperResponse(e.getMessage());
        }
        Collection<Listing> listings = new ArrayList<Listing>();
        int counter = 0;
        boolean continueParsing = true;

        while (continueParsing) {
            Element listingsPage = null;
            try {
                listingsPage = fetchCraigslistPage(counter);
            } catch (IOException e) {
                e.printStackTrace();
                return new ScraperResponse(e.getMessage());
            }
            ParsedCraigslistPage parsedPage = parseCraigslistPage(listingsPage, lastSeen);
            continueParsing = parsedPage.isContinueParsing();
            counter += parsedPage.getTotalOnPage();
            listings.addAll(parsedPage.getSavedListings());
        }

        return new ScraperResponse(listings);
    }

    public static ParsedCraigslistPage parseCraigslistPage(Element craigslistPage, Date lastSeen) {
        ArrayList<Listing> savedListings = new ArrayList<Listing>();
        boolean continueParsing = true;

        Collection<Element> listingRows = craigslistPage.getElementsByClass("result-row");
        if (listingRows.size() == 0) {
            return new ParsedCraigslistPage(savedListings, false, listingRows.size());
        }

        for (Element listing : listingRows) {
            Listing currentListing = new Listing();
            try {
                Date listingDate = retrieveDate(listing);
                if (lastSeen != null && listingDate.compareTo(lastSeen)< 0) {
                    System.out.println("Current listing was posted earlier (" + listingDate + ") than the last time the parser was run.  Exiting.");
                    continueParsing = false;
                    break;
                }

                currentListing.setHref(retrieveLink(listing));
                savedListings.add(currentListing);
                System.out.println("Saving listing with url: " + currentListing.getHref());

                currentListing.setDate(listingDate);
                currentListing.setTitle(retrieveTitle(listing));
                currentListing.setPrice(retrievePrice(listing));
                currentListing.setNeighborhood(retrieveNeighborhood(listing));
                currentListing.setBedrooms(retrieveBedrooms(listing));
                currentListing.setSqFeet(retrieveSqFeet(listing));
                currentListing.setLoc(retrieveLocation(listing));

                System.out.println("Listing date " + currentListing.getDate() + ": " + currentListing.getTitle());
            } catch (ScraperException e) {
                System.err.println("Error parsing listing: " + e);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
        return new ParsedCraigslistPage(savedListings, continueParsing, listingRows.size());
    }

    private static Integer retrieveBedrooms(Element listing) {
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

    private static Integer retrieveSqFeet(Element listing) {
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

    private static int retrievePrice(Element listing) {
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

    public static Date retrieveDate(Element resultRow) {
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

    public static String retrieveLink(Element resultRow) {
        List<Element> title = resultRow.getElementsByClass("result-title");
        if (title == null || title.size() == 0) {
            throw new ScraperException("Could not retrieve link from listing.");
        }

        return title.get(0).attr("href");
    }

    public static String retrieveNeighborhood(Element resultRow) {
        List<Element> neighborhood = resultRow.getElementsByClass("result-hood");
        if (neighborhood == null || neighborhood.size() == 0) {
            return null;
        } else {
            return neighborhood.get(0).html();
        }
    }

    public static String retrieveTitle(Element listing) {
        List<Element> title = listing.getElementsByClass("result-title");
        if (title == null || title.size() == 0) {
            throw new ScraperException("Could not retrieve title from listing.");
        }

        return Jsoup.clean(title.get(0).html(), Whitelist.basic());
    }

    public static Location retrieveLocation(Element listing) {
        String href = null;
        try {
            href = retrieveLink(listing);
        } catch (ScraperException e) {
            throw new ScraperException("Could not retrieve link from listing:\n\t" + e);
        }

        if (href == null || !href.startsWith("https://sfbay.craigslist.org")) {
            throw new ScraperException("Link is not a craigslist link: " + href);
        }

        try {
            Element listingPage = Jsoup.connect(href).get();
            Location location = retrieveLocationFromListingPage(listingPage);
            if (location == null) {
                throw new ScraperException("Could not retrieve location from listing page.");
            }
            return location;
        } catch (IOException e) {
            throw new ScraperException("Error navigating to listing: " + e);
        }
    }

    public static Location retrieveLocationFromListingPage(Element listingDocument) {
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

        int tries = 1;
        int maxRetries = 3;

        while (true) {
            try {
                System.out.println("Fetching: " + fetchUrl);
                return Jsoup.connect(fetchUrl).get();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Retrying unsuccessful http request to get craigslist listings. Url: " + fetchUrl);
                if (tries == maxRetries) {
                    throw e;
                }
            }
            tries += 1;
        }
    }

    public static Element fetchCraigslistPageRetry(int startListingIndex) throws IOException {
        int numRetries = 3;
        IOException caughtException = null;
        for (int i = 0; i < numRetries; i += 1) {
            try {
                return fetchCraigslistPage(startListingIndex);
            } catch (IOException e) {
                caughtException = e;
                System.err.println("Retrying http request ("+ i + "/" + numRetries + ") to Craigslist."
                                    + "  IOException encountered while fetching Craigslist page: " + e);
            }
        }
        throw caughtException;
    }

    public static Date parseInputDate(String dateStr) throws ScraperInputException {
        Date lastSeen = null;
        try {
            lastSeen = new SimpleDateFormat(DT_FORMAT_STR).parse(dateStr);
        } catch (ParseException e) {
            String dateParseErrMsg = "Unable to parse input date: " + e.getMessage();
            System.err.println(dateParseErrMsg);
            throw new ScraperInputException(dateParseErrMsg);
        }
        return lastSeen;
    }

    public static class ParsedCraigslistPage {
        private Collection<Listing> savedListings;
        private boolean continueParsing;
        private int totalOnPage;

        public ParsedCraigslistPage(Collection<Listing> savedListings, boolean continueParsing, int totalOnPage) {
            this.setSavedListings(savedListings);
            this.setContinueParsing(continueParsing);
            this.setTotalOnPage(totalOnPage);
        }

        public Collection<Listing> getSavedListings() {
            return savedListings;
        }

        public void setSavedListings(Collection<Listing> savedListings) {
            this.savedListings = savedListings;
        }

        public boolean isContinueParsing() {
            return continueParsing;
        }

        public void setContinueParsing(boolean continueParsing) {
            this.continueParsing = continueParsing;
        }

        public int getTotalOnPage() {
            return totalOnPage;
        }

        public void setTotalOnPage(int totalOnPage) {
            this.totalOnPage = totalOnPage;
        }
    }
}
