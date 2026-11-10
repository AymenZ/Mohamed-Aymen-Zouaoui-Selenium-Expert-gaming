package tn.expertgaming.pages;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import tn.expertgaming.utils.PriceUtil;

import java.util.List;

public class CartPage extends BasePage {

    public static final String PATH = "/panier/";

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public CartPage goTo() {
        open(PATH);
        return this;
    }

    public int itemCount() {
        return visibleElements(Locators.CART_ITEM).size();
    }

    public boolean isEmpty() {
        return itemCount() == 0;
    }

    public boolean hasEmptyMessage() {
        return firstVisible(Locators.CART_EMPTY_MSG) != null
                || bodyText().toLowerCase().contains("panier est actuellement vide");
    }

    /** Quantite affichee de la premiere ligne, ou -1. */
    public int firstQuantity() {
        List<WebElement> inputs = visibleElements(Locators.CART_QTY);
        if (inputs.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(inputs.get(0).getAttribute("value").trim());
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }

    public double subtotal() {
        WebElement e = firstVisible(Locators.CART_SUBTOTAL);
        if (e == null) {
            e = firstVisible(Locators.CART_TOTAL);
        }
        return e == null ? -1 : PriceUtil.parse(e.getText());
    }

    /** Supprime la premiere ligne du panier et attend que la page se mette a jour. */
    public void removeFirstItem() {
        WebElement remove = firstVisible(Locators.CART_REMOVE);
        if (remove == null) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "Bouton de suppression introuvable : adapter Locators.CART_REMOVE");
        }
        int before = itemCount();
        click(remove);
        try {
            wait.until(d -> itemCount() < before || hasEmptyMessage());
        } catch (TimeoutException ignored) {
            // l'assertion du test signalera l'echec
        }
        sleep(800);
    }

    /** Clique sur "Valider la commande" depuis le panier. Renvoie false si le bouton est absent. */
    public boolean proceedToCheckout() {
        WebElement button = firstVisible(Locators.CHECKOUT_BUTTON);
        if (button == null) {
            return false;
        }
        click(button);
        return true;
    }
}
