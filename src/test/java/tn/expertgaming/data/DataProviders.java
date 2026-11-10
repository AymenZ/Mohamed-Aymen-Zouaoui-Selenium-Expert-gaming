package tn.expertgaming.data;

import org.testng.annotations.DataProvider;
import tn.expertgaming.utils.CsvReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Fournisseurs de donnees TestNG alimentes par les fichiers CSV de src/test/resources/data. */
public final class DataProviders {

    private DataProviders() {
    }

    private static Object[][] rows(String file, int columns) {
        List<String[]> data = CsvReader.read("data/" + file);
        Object[][] out = new Object[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            String[] padded = Arrays.copyOf(data.get(i), columns);
            for (int c = 0; c < padded.length; c++) {
                if (padded[c] == null) {
                    padded[c] = "";
                }
            }
            out[i] = padded;
        }
        return out;
    }

    private static Object[][] searchRows(boolean expectResults) {
        List<Object[]> out = new ArrayList<>();
        for (String[] row : CsvReader.read("data/search_terms.csv")) {
            boolean expect = row.length > 1 && row[1].equalsIgnoreCase("true");
            if (expect == expectResults) {
                out.add(new Object[]{row[0]});
            }
        }
        return out.toArray(new Object[0][]);
    }

    /** menu_links.csv : label, path */
    @DataProvider(name = "menuLinks")
    public static Object[][] menuLinks() {
        return rows("menu_links.csv", 2);
    }

    /** search_terms.csv, lignes avec expect_results=true : term */
    @DataProvider(name = "searchWithResults")
    public static Object[][] searchWithResults() {
        return searchRows(true);
    }

    /** search_terms.csv, lignes avec expect_results=false : term */
    @DataProvider(name = "searchWithoutResults")
    public static Object[][] searchWithoutResults() {
        return searchRows(false);
    }

    /** register_data.csv : scenario, email */
    @DataProvider(name = "invalidRegistrations")
    public static Object[][] invalidRegistrations() {
        return rows("register_data.csv", 2);
    }

    /** login_data.csv : scenario, username, password */
    @DataProvider(name = "invalidLogins")
    public static Object[][] invalidLogins() {
        return rows("login_data.csv", 3);
    }

    /** tracking_data.csv : scenario, orderid, email */
    @DataProvider(name = "unknownOrders")
    public static Object[][] unknownOrders() {
        return rows("tracking_data.csv", 3);
    }

    /** contact_data.csv : scenario, name, email, subject, message */
    @DataProvider(name = "invalidContacts")
    public static Object[][] invalidContacts() {
        return rows("contact_data.csv", 5);
    }

    /** legal_pages.csv : label, path, min_chars */
    @DataProvider(name = "legalPages")
    public static Object[][] legalPages() {
        return rows("legal_pages.csv", 3);
    }

    /** browsers.csv : browser */
    @DataProvider(name = "browsers")
    public static Object[][] browsers() {
        return rows("browsers.csv", 1);
    }
}
