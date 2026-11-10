package tn.expertgaming.pages;

import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HomePage extends BasePage {

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage goTo() {
        open("/");
        return this;
    }

    /** Lien du menu principal (zone en-tete) vers ce chemin, ou null. */
    public WebElement findMenuLink(String path) {
        return findVisibleLinkByPath(path, Locators.HEADER_LINKS);
    }

    /** Ouvre le menu "burger" en affichage mobile. Renvoie false si aucun bouton n'est trouve. */
    public boolean openMobileMenu() {
        WebElement toggle = firstVisible(Locators.MOBILE_MENU_TOGGLE);
        if (toggle == null) {
            return false;
        }
        click(toggle);
        sleep(800);
        return true;
    }

    private WebElement findSearchInput() {
        WebElement input = firstVisible(Locators.SEARCH_INPUT);
        if (input == null) {
            // Sur certains themes le champ n'apparait qu'apres un clic sur la loupe
            for (WebElement toggle : visibleElements(Locators.SEARCH_TOGGLE)) {
                click(toggle);
                sleep(600);
                input = firstVisible(Locators.SEARCH_INPUT);
                if (input != null) {
                    break;
                }
            }
        }
        return input;
    }

    /** Saisit un terme dans la barre de recherche et valide avec Entree. */
    public void search(String term) {
        WebElement input = findSearchInput();
        if (input == null) {
            throw new NoSuchElementException(
                    "Champ de recherche introuvable : adapter Locators.SEARCH_INPUT / SEARCH_TOGGLE");
        }
        input.clear();
        input.sendKeys(term, Keys.ENTER);
        wait.until(d -> d.getCurrentUrl().contains("s="));
    }
}
