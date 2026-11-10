package tn.expertgaming.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import tn.expertgaming.utils.PriceUtil;

import java.time.Duration;

public class ProductPage extends BasePage {

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public void openUrl(String url) {
        open(url);
    }

    public String name() {
        WebElement e = firstVisible(Locators.PRODUCT_TITLE);
        return e == null ? "" : e.getText().trim();
    }

    /** Vrai si au moins une image de la galerie est affichee ET chargee (largeur naturelle > 0). */
    public boolean imageLoaded() {
        for (WebElement img : visibleElements(Locators.PRODUCT_IMAGE)) {
            Object width = js("return arguments[0].naturalWidth;", img);
            if (width instanceof Number && ((Number) width).intValue() > 0) {
                return true;
            }
        }
        return false;
    }

    public String priceText() {
        WebElement e = firstVisible(Locators.PRODUCT_PRICE_SALE);
        if (e == null) {
            e = firstVisible(Locators.PRODUCT_PRICE);
        }
        return e == null ? "" : e.getText().trim();
    }

    /** Prix actuel (prix promotionnel s'il existe). -1 si introuvable. */
    public double priceValue() {
        return PriceUtil.parse(priceText());
    }

    public boolean hasOldPrice() {
        return firstVisible(Locators.PRODUCT_OLD_PRICE) != null;
    }

    public WebElement addToCartButton() {
        return firstVisible(Locators.ADD_TO_CART);
    }

    public boolean isOutOfStock() {
        return firstVisible(Locators.OUT_OF_STOCK) != null
                || bodyText().toLowerCase().contains("rupture de stock");
    }

    /** Clique sur "Ajouter au panier". Renvoie false si le bouton est absent ou desactive. */
    public boolean addToCart() {
        WebElement button = addToCartButton();
        if (button == null || !button.isEnabled()) {
            return false;
        }
        click(button);
        waitForAddedFeedback();
        return true;
    }

    private void waitForAddedFeedback() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(6)).until(d -> !d.findElements(By.cssSelector(Locators.ADDED_NOTICE)).isEmpty()
                    || d.getCurrentUrl().contains("panier"));
        } catch (TimeoutException ignored) {
            // pas de message : le contenu du panier sera verifie ensuite
        }
        sleep(1200);
    }
}
