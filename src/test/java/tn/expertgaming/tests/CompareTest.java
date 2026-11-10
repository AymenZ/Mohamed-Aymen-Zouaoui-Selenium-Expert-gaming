package tn.expertgaming.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.config.Config;
import tn.expertgaming.pages.BasePage;
import tn.expertgaming.pages.Locators;
import tn.expertgaming.pages.ProductListPage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** M07 - Comparateur de produits (EF07). */
public class CompareTest extends BaseTest {

    /** Petite page technique pour reutiliser clic robuste et attentes. */
    private static final class Helper extends BasePage {
        Helper(org.openqa.selenium.WebDriver d) {
            super(d);
        }
    }

    @Test(groups = {"compare"},
            description = "TC12 - Ajouter trois produits au comparateur")
    public void tc12_compareThreeProducts() {
        Helper helper = new Helper(driver);
        ProductListPage list = new ProductListPage(driver);
        list.open(Config.get("products.list.path", "/souris-gaming"));

        List<WebElement> buttons = driver.findElements(By.cssSelector(Locators.COMPARE_BUTTON));
        Assert.assertTrue(buttons.size() >= 3,
                "Moins de 3 boutons 'Comparer' trouves sur la liste (trouves : " + buttons.size()
                        + "). Adapter Locators.COMPARE_BUTTON.");

        int clicked = 0;
        for (WebElement button : buttons) {
            if (clicked == 3) {
                break;
            }
            try {
                helper.click(button);
                clicked++;
                Thread.sleep(1500);
            } catch (RuntimeException e) {
                Reporter.log("Bouton Comparer ignore : " + e.getClass().getSimpleName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Assert.assertEquals(clicked, 3, "Impossible de cliquer sur 3 boutons 'Comparer'");

        helper.open("/compare/");
        helper.sleep(1500);
        Set<String> productLinks = new LinkedHashSet<>();
        String[] tokens = Config.get("product.url.tokens", "/produit/,/product/").split(",");
        for (WebElement a : driver.findElements(By.cssSelector("main a[href], #content a[href], .site-content a[href]"))) {
            String href = a.getAttribute("href");
            if (href == null) {
                continue;
            }
            for (String token : tokens) {
                if (!token.isBlank() && href.contains(token.trim())) {
                    productLinks.add(href);
                }
            }
        }
        Reporter.log("Produits distincts sur la page de comparaison : " + productLinks.size());
        Assert.assertTrue(productLinks.size() >= 3,
                "La page de comparaison devrait afficher 3 produits, trouves : " + productLinks.size()
                        + " (adapter product.url.tokens dans config.properties si les URLs produit sont differentes)");
    }
}
