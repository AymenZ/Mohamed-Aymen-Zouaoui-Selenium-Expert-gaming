package tn.expertgaming.driver;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import tn.expertgaming.config.Config;

import java.time.Duration;
import java.util.logging.Level;

/**
 * Creation des navigateurs. Selenium Manager (inclus dans Selenium 4) telecharge
 * automatiquement le bon driver : aucun chromedriver a installer a la main.
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> CURRENT = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static WebDriver create(String browser, boolean headless) {
        WebDriver driver;
        switch (browser.toLowerCase()) {
            case "firefox": {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) {
                    options.addArguments("-headless");
                }
                driver = new FirefoxDriver(options);
                break;
            }
            case "edge": {
                EdgeOptions options = new EdgeOptions();
                if (headless) {
                    options.addArguments("--headless=new");
                }
                options.addArguments("--window-size=1920,1080", "--lang=fr");
                driver = new EdgeDriver(options);
                break;
            }
            case "chrome":
            default: {
                ChromeOptions options = new ChromeOptions();
                if (headless) {
                    options.addArguments("--headless=new");
                }
                options.addArguments("--window-size=1920,1080", "--lang=fr",
                        "--disable-notifications", "--remote-allow-origins=*");
                LoggingPreferences prefs = new LoggingPreferences();
                prefs.enable(LogType.BROWSER, Level.ALL);
                options.setCapability("goog:loggingPrefs", prefs);
                driver = new ChromeDriver(options);
                break;
            }
        }
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Config.getInt("pageload.seconds", 60)));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(20));
        if (headless) {
            driver.manage().window().setSize(new Dimension(1920, 1080));
        } else {
            driver.manage().window().maximize();
        }
        return driver;
    }

    /** Navigateur partage par toute la suite : une seule fenetre ouverte du debut a la fin. */
    private static WebDriver shared;
    private static boolean hookRegistered;

    /**
     * Lie au thread courant le navigateur partage (cree au premier appel), apres l'avoir remis
     * a zero : cookies, stockage et onglets supplementaires. Chaque classe de test repart donc
     * d'une session propre (panier vide) sans rouvrir de fenetre.
     */
    public static synchronized void start() {
        if (shared == null || !isAlive(shared)) {
            shared = create(Config.get("browser", "chrome"), Config.getBool("headless", false));
            if (!hookRegistered) {
                hookRegistered = true;
                Runtime.getRuntime().addShutdownHook(new Thread(DriverFactory::quitShared));
            }
        } else {
            resetSession(shared);
        }
        CURRENT.set(shared);
    }

    /** Ferme le navigateur partage (fin de suite). */
    public static synchronized void quitShared() {
        if (shared != null) {
            try {
                shared.quit();
            } catch (Exception ignored) {
                // navigateur deja ferme
            }
            shared = null;
        }
        CURRENT.remove();
    }

    private static boolean isAlive(WebDriver driver) {
        try {
            driver.getWindowHandle();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void resetSession(WebDriver driver) {
        try {
            String main = driver.getWindowHandle();
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(main)) {
                    driver.switchTo().window(handle).close();
                }
            }
            driver.switchTo().window(main);
            try {
                ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("try{localStorage.clear();sessionStorage.clear();}catch(e){}");
            } catch (Exception ignored) {
                // page sans stockage (about:blank, erreur reseau...)
            }
            driver.manage().deleteAllCookies();
            driver.get("about:blank");
        } catch (Exception ignored) {
            // un echec de remise a zero ne doit pas bloquer la suite
        }
    }

    public static WebDriver getDriver() {
        return CURRENT.get();
    }

    public static void setDriver(WebDriver driver) {
        CURRENT.set(driver);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static void quit() {
        WebDriver driver = CURRENT.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
                // navigateur deja ferme
            }
            CURRENT.remove();
        }
    }
}
