package tn.expertgaming.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.data.DataProviders;
import tn.expertgaming.pages.HomePage;
import tn.expertgaming.pages.Locators;

/** M12 - Pages institutionnelles et legales (EF11). */
public class LegalPagesTest extends BaseTest {

    @Test(groups = {"legal"},
            dataProvider = "legalPages", dataProviderClass = DataProviders.class,
            description = "TC15 - Ouvrir les pages legales depuis le pied de page")
    public void tc15_legalPagesFromFooter(String label, String path, String minChars) {
        HomePage home = new HomePage(driver).goTo();
        SoftAssert soft = new SoftAssert();
        String expected = path.replaceAll("/+$", "");

        WebElement link = home.findVisibleLinkByPath(path, "footer a[href]");
        if (link != null) {
            home.click(link);
            soft.assertTrue(home.waitUrlContains(expected),
                    "Le lien '" + label + "' du pied de page n'ouvre pas " + path + " (URL : " + driver.getCurrentUrl() + ")");
        } else {
            soft.fail("Lien '" + label + "' (" + path + ") absent du pied de page");
            home.open(path);
        }

        String content = contentText();
        int min = Integer.parseInt(minChars.isBlank() ? "300" : minChars);
        Reporter.log("Page '" + label + "' : " + content.length() + " caracteres de contenu");
        soft.assertTrue(content.length() >= min,
                "Contenu de '" + label + "' trop court : " + content.length() + " caracteres (minimum " + min + ")");
        String title = home.title().toLowerCase();
        soft.assertFalse(title.contains("404") || title.contains("introuvable"), "Page en erreur : " + home.title());
        soft.assertAll();
    }

    private String contentText() {
        for (String css : Locators.CONTENT_AREAS) {
            for (WebElement e : driver.findElements(By.cssSelector(css))) {
                String text = e.getText();
                if (text != null && !text.isBlank()) {
                    return text.trim();
                }
            }
        }
        return driver.findElement(By.tagName("body")).getText().trim();
    }
}
