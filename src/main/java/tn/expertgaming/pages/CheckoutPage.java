package tn.expertgaming.pages;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class CheckoutPage extends BasePage {

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isPresent(Locators.CHECKOUT_FORM);
    }

    /** Ouvre la page de commande depuis le panier, avec des chemins de secours. */
    public boolean openFromCart() {
        CartPage cart = new CartPage(driver).goTo();
        if (cart.proceedToCheckout()) {
            try {
                wait.until(d -> isLoaded());
                return true;
            } catch (TimeoutException ignored) {
                // on tente les chemins directs
            }
        }
        for (String path : Locators.CHECKOUT_PATHS) {
            open(path);
            if (isLoaded()) {
                return true;
            }
        }
        return false;
    }

    public List<String> paymentMethods() {
        List<String> names = new ArrayList<>();
        for (WebElement li : driver.findElements(org.openqa.selenium.By.cssSelector(Locators.PAYMENT_METHODS))) {
            String text = li.getText().trim();
            if (text.isEmpty()) {
                text = li.getAttribute("textContent") == null ? "" : li.getAttribute("textContent").trim();
            }
            if (!text.isEmpty()) {
                names.add(text.split("\n")[0].trim());
            }
        }
        return names;
    }

    /** Nombre d'indicateurs de champs obligatoires dans le formulaire. */
    public int requiredFieldCount() {
        return driver.findElements(org.openqa.selenium.By.cssSelector(Locators.REQUIRED_FIELDS)).size();
    }

    /**
     * Clique sur "Commander" avec le formulaire vide et renvoie true si des erreurs de validation s'affichent.
     * ATTENTION : n'est appele que si checkout.submitEmpty=true.
     */
    public boolean submitEmptyAndHasErrors() {
        WebElement place = firstVisible(Locators.PLACE_ORDER);
        if (place == null) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "Bouton de validation introuvable : adapter Locators.PLACE_ORDER");
        }
        click(place);
        try {
            wait.until(d -> firstVisible(Locators.CHECKOUT_ERRORS) != null || d.getCurrentUrl().contains("order-received"));
        } catch (TimeoutException ignored) {
            return false;
        }
        return firstVisible(Locators.CHECKOUT_ERRORS) != null;
    }
}
