package tn.expertgaming.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import tn.expertgaming.config.Config;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Fonctions communes a toutes les pages. */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.getInt("timeout.seconds", 15)));
    }

    /** Ouvre un chemin du site ("/panier/") et ferme le bandeau cookies s'il y en a un. */
    public void open(String path) {
        driver.get(Config.url(path));
        dismissOverlays();
    }

    public void dismissOverlays() {
        for (String css : Locators.COOKIE_BUTTONS) {
            try {
                for (WebElement e : driver.findElements(By.cssSelector(css))) {
                    if (e.isDisplayed()) {
                        click(e);
                        sleep(300);
                        return;
                    }
                }
            } catch (WebDriverException ignored) {
                // pas de bandeau : on continue
            }
        }
    }

    /** Attend que l'URL contienne ce fragment. Renvoie false si le delai est depasse. */
    public boolean waitUrlContains(String fragment) {
        try {
            wait.until(d -> d.getCurrentUrl().contains(fragment));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String title() {
        String t = driver.getTitle();
        return t == null ? "" : t;
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public String bodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    /** Clic robuste : repasse par JavaScript si un bandeau ou le chat masque l'element. */
    public void click(WebElement element) {
        try {
            js("arguments[0].scrollIntoView({block:'center'});", element);
            element.click();
        } catch (ElementNotInteractableException e) {
            js("arguments[0].click();", element);
        }
    }

    protected Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    protected List<WebElement> visibleElements(String css) {
        List<WebElement> visible = new ArrayList<>();
        for (WebElement e : driver.findElements(By.cssSelector(css))) {
            try {
                if (e.isDisplayed()) {
                    visible.add(e);
                }
            } catch (StaleElementReferenceException ignored) {
                // element remplace entre-temps
            }
        }
        return visible;
    }

    protected WebElement firstVisible(String css) {
        List<WebElement> list = visibleElements(css);
        return list.isEmpty() ? null : list.get(0);
    }

    protected boolean isPresent(String css) {
        return !driver.findElements(By.cssSelector(css)).isEmpty();
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Chemin normalise d'une URL, sans domaine, sans parametres, sans "/" final. */
    public static String pathOf(String href) {
        try {
            String path = new URI(href).getPath();
            if (path == null) {
                return "";
            }
            return path.replaceAll("/+$", "");
        } catch (Exception e) {
            return "";
        }
    }

    /** Premier lien visible dont le chemin est exactement 'path' dans la zone CSS donnee. */
    public WebElement findVisibleLinkByPath(String path, String scopeCss) {
        String expected = path.replaceAll("/+$", "");
        for (WebElement a : visibleElements(scopeCss)) {
            String href = a.getAttribute("href");
            if (href != null && pathOf(href).equals(expected)) {
                return a;
            }
        }
        return null;
    }
}
