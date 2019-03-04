package com.rhdes.aptagent.scraper;

import com.rhdes.aptagent.scraper.domain.ScraperConfig;
import com.rhdes.aptagent.scraper.exception.ScraperInputException;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Set;

public class ScraperInputValidator {

    public static final BigDecimal MIN_PRICE = new BigDecimal("0");
    public static final BigDecimal MAX_PRICE = new BigDecimal("50000");

    public static final int MIN_BEDROOMS = 0;
    public static final int MAX_BEDROOMS = 20;

    public static final String ACCEPTED_PROTOCOL = "https";

    public static void validateInputConfig(ScraperConfig inputConfig) throws ScraperInputException {
        validateMinAndMaxAmounts(inputConfig.getMinPrice(), inputConfig.getMaxPrice(),
                                    MIN_PRICE, MAX_PRICE, "listing price");
        validateMinAndMaxAmounts(inputConfig.getMinBedrooms(), inputConfig.getMaxBedrooms(),
                                    MIN_BEDROOMS, MAX_BEDROOMS, "number of bedrooms");

        validateUrl(inputConfig.getBaseUrl());
    }


    private static void validateUrl(URL baseUrl, Set<String> acceptedHostnames) {
        validateUrl(baseUrl);
        if (acceptedHostnames != null && !acceptedHostnames.contains(baseUrl.getHost())) {
            throw new ScraperInputException("Hostname '" + baseUrl.getHost() + "' is " +
                                            " not an accepted hostname.");
        }
    }

    private static void validateUrl(URL baseUrl) {
        if (baseUrl == null) {
            throw new ScraperInputException("Url is missing.");
        } else if (!ACCEPTED_PROTOCOL.equals(baseUrl.getProtocol())) {
            throw new ScraperInputException("Url must use the " + ACCEPTED_PROTOCOL + " protocol");
        }
    }


    public static void validateMinAndMaxAmounts(BigDecimal inputMinAmount, BigDecimal inputMaxAmount,
                                           BigDecimal minAmount, BigDecimal maxAmount,
                                           String quantityName) {
        if (inputMinAmount.compareTo(inputMaxAmount) > -1) {
            throw new ScraperInputException("Minimum " + quantityName + " is greater than or equal to the maximum "
                                                + quantityName + ".");
        } else {
            validateRange(inputMinAmount, minAmount, maxAmount, "minimum " + quantityName);
            validateRange(inputMaxAmount, minAmount, maxAmount, "maximum " + quantityName);
        }
    }

    public static void validateRange(BigDecimal inputAmount, BigDecimal minAmount,
                                     BigDecimal maxAmount, String quantityName) {
        if (inputAmount.compareTo(minAmount) < 0) {
            throw new ScraperInputException(Utils.capitalize(quantityName) + " is less than the minimum allowed value " + minAmount + ".");
        } else if (inputAmount.compareTo(maxAmount) > 0) {
            throw new ScraperInputException(Utils.capitalize(quantityName) + " is greater than the maximum allowed value " + maxAmount + ".");
        }
    }


    public static void validateMinAndMaxAmounts(int inputMinAmount, int inputMaxAmount,
                                                int minAmount, int maxAmount,
                                                String quantityName) {
        if (inputMinAmount >= inputMaxAmount) {
            throw new ScraperInputException("Minimum " + quantityName + " is greater than or equal to the maximum "
                    + quantityName + ".");
        } else {
            validateRange(inputMinAmount, minAmount, maxAmount, "minimum " + quantityName);
            validateRange(inputMaxAmount, minAmount, maxAmount, "maximum " + quantityName);
        }
    }

    public static void validateRange(int inputAmount, int minAmount,
                                     int maxAmount, String quantityName) {
        if (inputAmount < minAmount) {
            throw new ScraperInputException(Utils.capitalize(quantityName) + " is less than the minimum allowed value " + minAmount + ".");
        } else if (inputAmount > maxAmount) {
            throw new ScraperInputException(Utils.capitalize(quantityName) + " is greater than the maximum allowed value " + maxAmount + ".");
        }
    }


}
