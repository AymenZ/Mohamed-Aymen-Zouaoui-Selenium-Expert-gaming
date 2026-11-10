package tn.expertgaming.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TrackOrderPage extends BasePage {

    public static final String PATH = "/suivre-la-commande/";

    public TrackOrderPage(WebDriver driver) {
        super(driver);
    }

    public TrackOrderPage goTo() {
        open(PATH);
        return this;
    }

    public boolean hasForm() {
        return firstVisible(Locators.TRACK_ORDER_ID) != null;
    }

    public void track(String orderId, String email) {
        WebElement id = firstVisible(Locators.TRACK_ORDER_ID);
        WebElement mail = firstVisible(Locators.TRACK_EMAIL);
        WebElement submit = firstVisible(Locators.TRACK_SUBMIT);
        if (id == null || submit == null) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "Formulaire de suivi introuvable : adapter Locators.TRACK_*");
        }
        id.clear();
        id.sendKeys(orderId);
        if (mail != null) {
            mail.clear();
            mail.sendKeys(email);
        }
        click(submit);
        sleep(2000);
    }

    /** Vrai si un message d'erreur est affiche (balise d'erreur ou texte explicite). */
    public boolean showsError() {
        if (firstVisible(Locators.TRACK_ERRORS) != null) {
            return true;
        }
        String t = bodyText().toLowerCase();
        String[] words = {"invalide", "introuvable", "incorrect", "erreur", "n'a pas été trouvé",
                "aucune commande", "not found", "error", "veuillez"};
        for (String w : words) {
            if (t.contains(w)) {
                return true;
            }
        }
        return false;
    }
}
