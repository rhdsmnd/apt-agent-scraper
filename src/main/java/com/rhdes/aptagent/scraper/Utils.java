package com.rhdes.aptagent.scraper;

public class Utils {
    public static String formatPluralModifier(String noun, int number) {
        if (number == 1) {
            return number + " " + noun;
        } else {
            return number + " " + noun + "s";
        }
    }
}
