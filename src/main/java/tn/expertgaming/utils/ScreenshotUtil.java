package tn.expertgaming.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import tn.expertgaming.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Captures d'ecran PNG enregistrees dans reports/screenshots. */
public final class ScreenshotUtil {

    private static final ThreadLocal<String> LAST = new ThreadLocal<>();

    private ScreenshotUtil() {
    }

    /** Enregistre une capture et renvoie son chemin relatif au dossier reports (ex. "screenshots/x.png"). */
    public static String capture(WebDriver driver, String baseName) {
        try {
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String fileName = sanitize(baseName) + "_" + stamp + ".png";
            Path dir = Paths.get(Config.reportsDir(), "screenshots");
            Files.createDirectories(dir);
            Files.copy(source.toPath(), dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            String relative = "screenshots/" + fileName;
            LAST.set(relative);
            return relative;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /** Derniere capture prise par le thread courant (utile quand le navigateur est deja ferme). */
    public static String lastCaptured() {
        return LAST.get();
    }

    public static void resetLast() {
        LAST.remove();
    }

    public static String sanitize(String name) {
        return name.replaceAll("[^A-Za-z0-9_.-]", "_");
    }
}
