package tn.expertgaming.utils;

/** Conversion d'un prix affiche ("1 250,000 DT") en nombre. */
public final class PriceUtil {

    private PriceUtil() {
    }

    /** Renvoie -1 si aucun nombre n'est trouve. */
    public static double parse(String text) {
        if (text == null) {
            return -1;
        }
        String cleaned = text.replaceAll("[^0-9,.]", "");
        if (cleaned.isEmpty()) {
            return -1;
        }
        // "1250,000" -> "1250.000" ; si plusieurs separateurs, le dernier est le decimal
        cleaned = cleaned.replace(',', '.');
        int last = cleaned.lastIndexOf('.');
        if (last >= 0) {
            String integerPart = cleaned.substring(0, last).replace(".", "");
            cleaned = integerPart + "." + cleaned.substring(last + 1);
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
