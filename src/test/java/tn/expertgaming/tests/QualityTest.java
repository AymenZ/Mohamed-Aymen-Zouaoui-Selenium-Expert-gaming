package tn.expertgaming.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.config.Config;
import tn.expertgaming.pages.CartPage;
import tn.expertgaming.pages.HomePage;
import tn.expertgaming.pages.Locators;
import tn.expertgaming.pages.ProductListPage;
import tn.expertgaming.pages.ProductPage;
import tn.expertgaming.utils.CsvReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** M13 - Qualites transverses : performance (TC16), accessibilite clavier (TC19), SEO (TC20). */
public class QualityTest extends BaseTest {

    private static final String LCP_SCRIPT =
            "var cb = arguments[arguments.length - 1]; var v = null;"
                    + "try { new PerformanceObserver(function(l){ var e = l.getEntries(); v = e[e.length-1].startTime; })"
                    + ".observe({type:'largest-contentful-paint', buffered:true}); } catch(e) {}"
                    + "setTimeout(function(){ cb(v); }, 1500);";

    @Test(groups = {"quality", "performance"},
            description = "TC16 - Mesurer la performance de la page d'accueil (LCP et temps de chargement)")
    public void tc16_homePagePerformance() {
        new HomePage(driver).goTo();

        Object lcpRaw = ((JavascriptExecutor) driver).executeAsyncScript(LCP_SCRIPT);
        Object loadRaw = ((JavascriptExecutor) driver).executeScript(
                "var n = performance.getEntriesByType('navigation')[0]; return n ? n.loadEventEnd : null;");
        Object dclRaw = ((JavascriptExecutor) driver).executeScript(
                "var n = performance.getEntriesByType('navigation')[0]; return n ? n.domContentLoadedEventEnd : null;");

        Double lcp = lcpRaw instanceof Number ? ((Number) lcpRaw).doubleValue() : null;
        Double load = loadRaw instanceof Number ? ((Number) loadRaw).doubleValue() : null;
        Double dcl = dclRaw instanceof Number ? ((Number) dclRaw).doubleValue() : null;
        Reporter.log(String.format("LCP = %s ms | DOMContentLoaded = %s ms | load = %s ms",
                lcp == null ? "n/d" : Math.round(lcp), dcl == null ? "n/d" : Math.round(dcl),
                load == null ? "n/d" : Math.round(load)));

        double lcpMax = Config.getInt("perf.lcp.max.ms", 2500);
        double loadMax = Config.getInt("perf.load.max.ms", 8000);
        SoftAssert soft = new SoftAssert();
        if (lcp == null) {
            Reporter.log("LCP non disponible sur ce navigateur : seule la duree de chargement est verifiee");
        } else {
            soft.assertTrue(lcp <= lcpMax, "LCP trop lent : " + Math.round(lcp) + " ms (seuil " + Math.round(lcpMax) + " ms)");
        }
        soft.assertNotNull(load, "Temps de chargement non mesurable");
        if (load != null) {
            soft.assertTrue(load <= loadMax, "Chargement complet trop lent : " + Math.round(load) + " ms (seuil " + Math.round(loadMax) + " ms)");
        }
        soft.assertAll();
    }

    @Test(groups = {"quality", "accessibility"},
            description = "TC19 - Atteindre le bouton d'ajout au panier uniquement au clavier")
    public void tc19_keyboardAddToCart() {
        ProductListPage list = new ProductListPage(driver);
        list.open(Config.get("products.list.path", "/souris-gaming"));
        List<String> urls = list.productUrls(6);
        Assert.assertFalse(urls.isEmpty(), "Aucun produit trouve pour le test clavier");

        ProductPage product = new ProductPage(driver);
        String chosen = null;
        for (String url : urls) {
            product.openUrl(url);
            if (product.addToCartButton() != null) {
                chosen = url;
                break;
            }
        }
        Assert.assertNotNull(chosen, "Aucun produit avec bouton 'Ajouter au panier' parmi les 6 premiers");

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,0); if (document.activeElement) document.activeElement.blur();");
        Actions actions = new Actions(driver);
        boolean reached = false;
        for (int i = 0; i < 200 && !reached; i++) {
            actions.sendKeys(Keys.TAB).perform();
            WebElement active = driver.switchTo().activeElement();
            Object matches = ((JavascriptExecutor) driver).executeScript(
                    "return arguments[0].matches(arguments[1]);", active, Locators.ADD_TO_CART);
            reached = Boolean.TRUE.equals(matches);
        }
        Assert.assertTrue(reached, "Le bouton 'Ajouter au panier' n'est pas atteignable avec la touche Tab (200 tabulations)");

        WebElement active = driver.switchTo().activeElement();
        Object focusVisible = ((JavascriptExecutor) driver).executeScript(
                "var s = getComputedStyle(arguments[0]);"
                        + "return (s.outlineStyle !== 'none' && parseFloat(s.outlineWidth) > 0) || s.boxShadow !== 'none';", active);
        SoftAssert soft = new SoftAssert();
        soft.assertTrue(Boolean.TRUE.equals(focusVisible), "Aucun indicateur de focus visible sur le bouton (accessibilite)");

        actions.sendKeys(Keys.ENTER).perform();
        product.sleep(2500);
        CartPage cart = new CartPage(driver).goTo();
        soft.assertTrue(cart.itemCount() > 0, "L'appui sur Entree n'a pas ajoute le produit au panier");
        soft.assertAll();
    }

    @Test(groups = {"quality", "seo"},
            description = "TC20 - Controler title, meta description et titre H1 des pages principales")
    public void tc20_seoBasics() {
        SoftAssert soft = new SoftAssert();
        Set<String> titles = new HashSet<>();
        Set<String> descriptions = new HashSet<>();
        List<String> duplicates = new ArrayList<>();

        for (String[] row : CsvReader.read("data/seo_pages.csv")) {
            String path = row[0];
            driver.get(Config.url(path));
            String title = driver.getTitle() == null ? "" : driver.getTitle().trim();
            List<WebElement> metas = driver.findElements(By.cssSelector("meta[name='description']"));
            String description = metas.isEmpty() ? "" : String.valueOf(metas.get(0).getAttribute("content")).trim();
            int h1 = driver.findElements(By.tagName("h1")).size();

            soft.assertFalse(title.isEmpty(), path + " : title vide");
            soft.assertFalse(description.isEmpty(), path + " : meta description absente ou vide");
            soft.assertTrue(h1 >= 1, path + " : aucun titre H1");
            if (!title.isEmpty() && !titles.add(title)) {
                duplicates.add("title en double : '" + title + "' (" + path + ")");
            }
            if (!description.isEmpty() && !descriptions.add(description)) {
                duplicates.add("meta description en double (" + path + ")");
            }
            Reporter.log(path + " | title=" + title.length() + " car. | description=" + description.length()
                    + " car. | h1=" + h1);
        }
        soft.assertTrue(duplicates.isEmpty(), "Doublons detectes : " + duplicates);
        soft.assertAll();
    }
}
