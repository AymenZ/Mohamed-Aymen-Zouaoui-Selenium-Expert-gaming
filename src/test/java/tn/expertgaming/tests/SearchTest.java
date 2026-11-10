package tn.expertgaming.tests;

import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.data.DataProviders;
import tn.expertgaming.pages.HomePage;
import tn.expertgaming.pages.ProductListPage;

/** M02 - Recherche (EF02). */
public class SearchTest extends BaseTest {

    @Test(groups = {"smoke", "search"},
            dataProvider = "searchWithResults", dataProviderClass = DataProviders.class,
            description = "TC03 - Rechercher un terme existant et obtenir des produits pertinents")
    public void tc03_searchWithResults(String term) {
        new HomePage(driver).goTo().search(term);
        ProductListPage results = new ProductListPage(driver);
        results.waitForProducts();

        SoftAssert soft = new SoftAssert();
        int count = results.count();
        Reporter.log("Recherche '" + term + "' : " + count + " produit(s) affiche(s)");
        soft.assertTrue(count > 0, "Aucun produit trouve pour '" + term + "'");
        soft.assertTrue(results.cardsText().contains(term.toLowerCase()),
                "Aucun des produits affiches ne contient '" + term + "' (pertinence)");
        soft.assertAll();
    }

    @Test(groups = {"search"},
            dataProvider = "searchWithoutResults", dataProviderClass = DataProviders.class,
            description = "TC04 - Rechercher un terme inexistant et afficher un message clair")
    public void tc04_searchWithoutResults(String term) {
        new HomePage(driver).goTo().search(term);
        ProductListPage results = new ProductListPage(driver);
        results.waitForProducts();

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(results.count(), 0, "Des produits sont affiches pour le terme absurde '" + term + "'");
        soft.assertTrue(results.hasNoResultMessage(), "Aucun message 'aucun resultat' pour '" + term + "'");
        soft.assertAll();
    }
}
