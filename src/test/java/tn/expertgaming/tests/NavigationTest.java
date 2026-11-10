package tn.expertgaming.tests;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.config.Config;
import tn.expertgaming.data.DataProviders;
import tn.expertgaming.pages.HomePage;
import tn.expertgaming.pages.Locators;

/** M01 - Navigation et menus (EF01). */
public class NavigationTest extends BaseTest {

    @Test(priority = 1, groups = {"smoke", "navigation"},
            dataProvider = "menuLinks", dataProviderClass = DataProviders.class,
            description = "TC01 - Parcourir chaque famille du menu principal sur ordinateur")
    public void tc01_menuFamilies(String label, String path) {
        HomePage home = new HomePage(driver);
        home.goTo();
        SoftAssert soft = new SoftAssert();
        String expected = path.replaceAll("/+$", "");

        WebElement link = home.findMenuLink(path);
        if (link != null) {
            home.click(link);
        } else {
            soft.fail("Lien '" + label + "' (" + path + ") introuvable dans le menu principal");
            driver.get(Config.url(path));
        }
        if (!home.waitUrlContains(expected)) {
            soft.fail("Le clic sur '" + label + "' n'ouvre pas " + path + " (URL : " + driver.getCurrentUrl() + ")");
        }

        String title = driver.getTitle() == null ? "" : driver.getTitle().toLowerCase();
        String body = home.bodyText().toLowerCase();
        soft.assertFalse(title.contains("404") || title.contains("introuvable") || title.contains("not found"),
                "Titre de page en erreur pour '" + label + "' : " + driver.getTitle());
        soft.assertFalse(body.contains("page non trouvée") || body.contains("page introuvable")
                        || body.contains("oops! that page can"),
                "Message d'erreur 404 affiche pour '" + label + "'");
        soft.assertFalse(title.isBlank(), "Titre de page vide pour '" + label + "'");
        Reporter.log("Menu '" + label + "' -> " + driver.getCurrentUrl());
        soft.assertAll();
    }

    @Test(priority = 2, groups = {"navigation"},
            description = "TC02 - Ouvrir le menu sur mobile et acceder a une famille de produits")
    public void tc02_mobileMenu() {
        try {
            driver.manage().window().setSize(new Dimension(390, 844));
            HomePage home = new HomePage(driver);
            home.goTo();
            boolean opened = home.openMobileMenu();
            Reporter.log("Bouton de menu mobile trouve : " + opened);

            WebElement link = home.findVisibleLinkByPath("/pc-gaming-bureautique/", Locators.ALL_LINKS);
            Assert.assertNotNull(link, "Le lien 'PC Gamer' n'est pas visible dans le menu mobile "
                    + "(bouton de menu trouve : " + opened + "). Adapter Locators.MOBILE_MENU_TOGGLE si besoin.");
            home.click(link);
            Assert.assertTrue(home.waitUrlContains("/pc-gaming-bureautique"),
                    "La page 'PC Gamer' ne s'ouvre pas depuis le menu mobile");
        } finally {
            driver.manage().window().setSize(new Dimension(1920, 1080));
        }
    }
}
