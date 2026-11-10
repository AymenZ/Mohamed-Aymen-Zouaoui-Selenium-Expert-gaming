package tn.expertgaming.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import tn.expertgaming.config.Config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Liste de produits : categorie, promotions ou resultats de recherche. */
public class ProductListPage extends BasePage {

    public ProductListPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void open(String path) {
        super.open(path);
        waitForProducts();
    }

    public void waitForProducts() {
        try {
            wait.until(d -> !visibleElements(Locators.PRODUCT_CARD).isEmpty());
        } catch (TimeoutException ignored) {
            // liste vide : les tests verifient ensuite le nombre de produits
        }
    }

    public List<WebElement> cards() {
        return visibleElements(Locators.PRODUCT_CARD);
    }

    public int count() {
        return cards().size();
    }

    /** Vrai si la page affiche un message "aucun produit / aucun resultat". */
    public boolean hasNoResultMessage() {
        String text = bodyText().toLowerCase();
        return text.contains("aucun produit") || text.contains("aucun résultat") || text.contains("aucun resultat")
                || text.contains("no products") || text.contains("rien trouvé") || text.contains("nothing found")
                || text.contains("aucun article");
    }

    /** Texte cumule des cartes visibles (en minuscules) pour verifier la pertinence d'une recherche. */
    public String cardsText() {
        StringBuilder sb = new StringBuilder();
        for (WebElement card : cards()) {
            sb.append(card.getText().toLowerCase()).append('\n');
        }
        return sb.toString();
    }

    /** URLs de fiches produit trouvees dans la liste (celles contenant /produit/ ou /product/ en premier). */
    public List<String> productUrls(int max) {
        Set<String> preferred = new LinkedHashSet<>();
        Set<String> others = new LinkedHashSet<>();
        String[] tokens = Config.get("product.url.tokens", "/produit/,/product/").split(",");
        String currentPath = pathOf(driver.getCurrentUrl());
        for (WebElement card : cards()) {
            String href = linkOf(card);
            if (href == null || pathOf(href).equals(currentPath)) {
                continue;
            }
            boolean matchesToken = false;
            for (String token : tokens) {
                if (!token.isBlank() && href.contains(token.trim())) {
                    matchesToken = true;
                    break;
                }
            }
            (matchesToken ? preferred : others).add(href);
        }
        List<String> all = new ArrayList<>(preferred);
        all.addAll(others);
        return all.size() > max ? all.subList(0, max) : all;
    }

    private String linkOf(WebElement card) {
        for (String css : Locators.PRODUCT_LINK_PRIORITY) {
            for (WebElement a : card.findElements(By.cssSelector(css))) {
                String href = a.getAttribute("href");
                if (isProductHref(href)) {
                    return href;
                }
            }
        }
        return null;
    }

    private boolean isProductHref(String href) {
        if (href == null || !href.startsWith("http") || !href.contains("expert-gaming.tn")) {
            return false;
        }
        String lower = href.toLowerCase();
        String[] excluded = {"add-to-cart", "wishlist", "compare", "#", "my-account", "javascript", "?add",
                "quick_view", "product_cat"};
        for (String bad : excluded) {
            if (lower.contains(bad)) {
                return false;
            }
        }
        return true;
    }
}
