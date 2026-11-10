package tn.expertgaming.base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import tn.expertgaming.driver.DriverFactory;
import tn.expertgaming.utils.ScreenshotUtil;

/**
 * Classe de base : une seule fenetre de navigateur pour toute la suite. Entre deux classes, la
 * session est remise a zero (cookies, stockage) ; les tests d'une meme classe partagent la session
 * (utile pour le panier : ajout, modification, suppression).
 */
public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void startBrowser() {
        ScreenshotUtil.resetLast();
        DriverFactory.start();
        driver = DriverFactory.getDriver();
    }

    /** Le navigateur reste ouvert entre les classes ; il est ferme une seule fois en fin de suite. */
    @AfterSuite(alwaysRun = true)
    public void stopBrowser() {
        DriverFactory.quitShared();
    }
}
