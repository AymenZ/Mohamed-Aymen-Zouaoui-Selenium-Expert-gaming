package tn.expertgaming.tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.config.Config;
import tn.expertgaming.pages.CartFlow;
import tn.expertgaming.pages.CheckoutPage;

import java.util.List;
import java.util.Locale;

/**
 * M05 - Commande et paiement (EF05).
 * SECURITE : ces tests ne finalisent JAMAIS une commande. Voir checkout.submitEmpty dans config.properties.
 */
public class CheckoutTest extends BaseTest {

    @BeforeClass(dependsOnMethods = "startBrowser")
    public void prepareCart() {
        CartFlow.AddedProduct added = CartFlow.addFirstPurchasable(driver, Config.get("products.list.path", "/souris-gaming"), 6);
        if (added == null) {
            throw new SkipException("Precondition : impossible d'ajouter un produit au panier pour atteindre la commande");
        }
    }

    @Test(priority = 1, groups = {"checkout"},
            description = "TC08 - Valider la commande avec des champs obligatoires vides")
    public void tc08_requiredFields() {
        CheckoutPage checkout = new CheckoutPage(driver);
        Assert.assertTrue(checkout.openFromCart(),
                "Page de commande introuvable depuis le panier. Adapter Locators.CHECKOUT_BUTTON / CHECKOUT_FORM.");

        if (Config.getBool("checkout.submitEmpty", false)) {
            Reporter.log("Mode checkout.submitEmpty=true : clic sur 'Commander' avec le formulaire vide");
            Assert.assertTrue(checkout.submitEmptyAndHasErrors(),
                    "Aucun message d'erreur affiche apres validation d'un formulaire vide");
            Assert.assertFalse(driver.getCurrentUrl().contains("order-received"),
                    "ALERTE : une commande semble avoir ete creee avec un formulaire vide !");
        } else {
            int required = checkout.requiredFieldCount();
            Reporter.log("Mode securise : " + required + " indicateur(s) de champ obligatoire trouve(s), "
                    + "aucun clic sur 'Commander'");
            Assert.assertTrue(required >= 3,
                    "Moins de 3 champs obligatoires detectes dans le formulaire de commande (trouves : " + required + ")");
        }
    }

    @Test(priority = 2, dependsOnMethods = "tc08_requiredFields", groups = {"checkout"},
            description = "TC09 - Parcourir le tunnel jusqu'au choix du mode de paiement")
    public void tc09_paymentMethods() {
        CheckoutPage checkout = new CheckoutPage(driver);
        if (!checkout.isLoaded()) {
            Assert.assertTrue(checkout.openFromCart(), "Page de commande introuvable");
        }
        List<String> methods = checkout.paymentMethods();
        Reporter.log("Modes de paiement affiches : " + methods);
        Assert.assertFalse(methods.isEmpty(),
                "Aucun mode de paiement affiche. Adapter Locators.PAYMENT_METHODS si le theme est different.");

        String all = String.join(" ", methods).toLowerCase(Locale.ROOT);
        SoftAssert soft = new SoftAssert();
        soft.assertTrue(all.contains("livraison") || all.contains("espèce") || all.contains("espece") || all.contains("cash"),
                "Paiement en especes / a la livraison non propose (modes : " + methods + ")");
        soft.assertAll();
    }
}
