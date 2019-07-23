package com.rhdes.aptagent.scraper;

public class Utils {

    public static String formatPluralModifier(String noun, int number) {
        if (number == 1) {
            return number + " " + noun;
        } else {
            return number + " " + noun + "s";
        }
    }

    public static String capitalize(String wordOrPhrase) {
        if (wordOrPhrase == null || wordOrPhrase.length() == 0) {
            return wordOrPhrase;
        }

        return wordOrPhrase.substring(0, 1).toUpperCase() + wordOrPhrase.substring(1);
    }
}
