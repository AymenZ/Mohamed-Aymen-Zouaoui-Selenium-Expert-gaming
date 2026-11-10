package tn.expertgaming.pages;

import org.openqa.selenium.WebDriver;

import java.util.List;

/** Scenario reutilisable : trouver un produit achetable et l'ajouter au panier. */
public final class CartFlow {

    /** Produit ajoute au panier. */
    public static final class AddedProduct {
        public final String url;
        public final String name;
        public final double unitPrice;

        AddedProduct(String url, String name, double unitPrice) {
            this.url = url;
            this.name = name;
            this.unitPrice = unitPrice;
        }
    }

    private CartFlow() {
    }

    /**
     * Parcourt les premiers produits de la liste jusqu'a en trouver un qui s'ajoute au panier.
     * Renvoie null si aucun produit n'a pu etre ajoute.
     */
    public static AddedProduct addFirstPurchasable(WebDriver driver, String listPath, int maxTries) {
        ProductListPage list = new ProductListPage(driver);
        list.open(listPath);
        List<String> urls = list.productUrls(maxTries);
        for (String url : urls) {
            ProductPage product = new ProductPage(driver);
            product.openUrl(url);
            String name = product.name();
            double price = product.priceValue();
            if (product.addToCart()) {
                CartPage cart = new CartPage(driver).goTo();
                if (cart.itemCount() > 0) {
                    return new AddedProduct(url, name, price);
                }
            }
        }
        return null;
    }
}
