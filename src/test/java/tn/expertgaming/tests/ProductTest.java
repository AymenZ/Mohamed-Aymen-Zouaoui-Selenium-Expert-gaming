package tn.expertgaming.tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.config.Config;
import tn.expertgaming.pages.ProductListPage;
import tn.expertgaming.pages.ProductPage;

import java.util.List;

/** M03 - Catalogue et fiches produit (EF03). */
public class ProductTest extends BaseTest {

    @Test(groups = {"smoke", "product"},
            description = "TC05 - Ouvrir la fiche d'un produit en promotion")
    public void tc05_promotionProductPage() {
        ProductListPage promos = new ProductListPage(driver);
        promos.open(Config.get("promo.path", "/promotions/"));
        List<String> urls = promos.productUrls(1);
        Assert.assertFalse(urls.isEmpty(), "Aucun produit trouve sur la page Promotions ("
                + driver.getCurrentUrl() + "). Adapter Locators.PRODUCT_CARD ou promo.path.");

        ProductPage product = new ProductPage(driver);
        product.openUrl(urls.get(0));
        Reporter.log("Produit : " + product.name() + " | prix : " + product.priceText());

        SoftAssert soft = new SoftAssert();
        soft.assertFalse(product.name().isBlank(), "Nom du produit absent");
        soft.assertTrue(product.imageLoaded(), "Image du produit absente ou non chargee");
        soft.assertTrue(product.priceValue() > 0, "Prix actuel absent ou illisible : '" + product.priceText() + "'");
        soft.assertTrue(product.hasOldPrice(), "Ancien prix barre absent sur un produit en promotion");
        soft.assertTrue(product.addToCartButton() != null || product.isOutOfStock(),
                "Ni bouton 'Ajouter au panier' ni mention de rupture de stock");
        soft.assertAll();
    }
}
