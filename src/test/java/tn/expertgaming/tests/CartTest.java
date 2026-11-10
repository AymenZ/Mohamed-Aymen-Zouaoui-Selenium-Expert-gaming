package tn.expertgaming.tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.config.Config;
import tn.expertgaming.pages.CartFlow;
import tn.expertgaming.pages.CartPage;
import tn.expertgaming.pages.ProductPage;

/**
 * M04 - Panier (EF04). TC07 depend de TC06 : si TC06 echoue, TC07 est marque Skipped.
 * Aucune commande n'est passee.
 */
public class CartTest extends BaseTest {

    @Test(priority = 1, groups = {"cart"},
            description = "TC06 - Ajouter deux fois le meme produit au panier")
    public void tc06_addSameProductTwice() {
        CartFlow.AddedProduct added = CartFlow.addFirstPurchasable(driver, Config.get("products.list.path", "/souris-gaming"), 6);
        if (added == null) {
            Assert.fail("Impossible d'ajouter un produit au panier depuis " + Config.get("products.list.path")
                    + " (aucun produit achetable trouve). Adapter products.list.path ou Locators.ADD_TO_CART.");
        }
        Reporter.log("Produit ajoute : " + added.name + " | prix unitaire : " + added.unitPrice);

        // 2e ajout du meme produit
        ProductPage product = new ProductPage(driver);
        product.openUrl(added.url);
        Assert.assertTrue(product.addToCart(), "Le 2e ajout au panier a echoue");

        CartPage cart = new CartPage(driver).goTo();
        SoftAssert soft = new SoftAssert();
        soft.assertEquals(cart.firstQuantity(), 2, "La quantite du produit devrait etre 2");
        if (added.unitPrice > 0) {
            double subtotal = cart.subtotal();
            soft.assertTrue(Math.abs(subtotal - 2 * added.unitPrice) <= 1.0,
                    "Sous-total attendu ~" + (2 * added.unitPrice) + " DT, affiche : " + subtotal + " DT");
        } else {
            Reporter.log("Prix unitaire illisible : verification du total ignoree");
        }
        soft.assertAll();
    }

    @Test(priority = 2, dependsOnMethods = "tc06_addSameProductTwice", groups = {"cart"},
            description = "TC07 - Supprimer un produit du panier")
    public void tc07_removeProduct() {
        CartPage cart = new CartPage(driver).goTo();
        if (cart.isEmpty()) {
            throw new SkipException("Le panier est deja vide : rien a supprimer");
        }
        cart.removeFirstItem();
        SoftAssert soft = new SoftAssert();
        soft.assertTrue(cart.isEmpty(), "Le produit est toujours present dans le panier apres suppression");
        soft.assertTrue(cart.hasEmptyMessage(), "Aucun message 'panier vide' affiche");
        soft.assertAll();
    }
}
