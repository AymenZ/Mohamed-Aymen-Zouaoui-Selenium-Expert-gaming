package tn.expertgaming.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tn.expertgaming.config.Config;
import tn.expertgaming.data.DataProviders;
import tn.expertgaming.driver.DriverFactory;
import tn.expertgaming.pages.Locators;
import tn.expertgaming.utils.ScreenshotUtil;

/**
 * M13 - Compatibilite navigateurs (ENF02). Chaque navigateur est lance a tour de role ;
 * s'il n'est pas installe, le test est marque Skipped. Safari n'est pas teste (macOS uniquement).
 */
public class CrossBrowserTest {

    @Test(groups = {"quality", "compatibility"},
            dataProvider = "browsers", dataProviderClass = DataProviders.class,
            description = "TC17 - Afficher la page d'accueil sur les navigateurs cibles")
    public void tc17_homePageOnBrowser(String browser) {
        WebDriver driver;
        try {
            driver = DriverFactory.create(browser, Config.getBool("headless", false));
        } catch (WebDriverException | IllegalStateException e) {
            throw new SkipException("Navigateur '" + browser + "' non disponible sur cette machine : " + firstLine(e.getMessage()));
        }
        ScreenshotUtil.resetLast();
        DriverFactory.setDriver(driver);
        try {
            driver.get(Config.url("/"));
            SoftAssert soft = new SoftAssert();
            JavascriptExecutor js = (JavascriptExecutor) driver;

            soft.assertEquals(js.executeScript("return document.readyState"), "complete", browser + " : page non chargee");
            soft.assertFalse(driver.getTitle() == null || driver.getTitle().isBlank(), browser + " : titre vide");

            boolean logoVisible = false;
            for (WebElement img : driver.findElements(By.cssSelector(Locators.LOGO))) {
                if (img.isDisplayed()) {
                    logoVisible = true;
                    break;
                }
            }
            soft.assertTrue(logoVisible, browser + " : logo non visible");

            int links = driver.findElements(By.cssSelector(Locators.HEADER_LINKS)).size();
            soft.assertTrue(links >= 5, browser + " : menu de navigation incomplet (" + links + " liens)");

            Object overflow = js.executeScript(
                    "return document.documentElement.scrollWidth > window.innerWidth + 5;");
            soft.assertFalse(Boolean.TRUE.equals(overflow), browser + " : defilement horizontal indesirable");

            soft.assertAll();
        } catch (AssertionError | RuntimeException e) {
            // Le navigateur sera ferme avant le listener : on prend la capture ici
            ScreenshotUtil.capture(driver, "TC17_" + browser);
            throw e;
        } finally {
            DriverFactory.quit();
            DriverFactory.clear();
        }
    }

    private static String firstLine(String text) {
        return text == null ? "" : text.strip().split("\\R")[0];
    }
}
