package tn.expertgaming.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Lecture de config.properties. Chaque cle peut etre surchargee avec -Dcle=valeur. */
public final class Config {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire config.properties", e);
        }
    }

    private Config() {
    }

    public static String get(String key) {
        String fromSystem = System.getProperty(key);
        return fromSystem != null ? fromSystem : PROPS.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBool(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    public static String baseUrl() {
        return get("base.url", "https://www.expert-gaming.tn").replaceAll("/+$", "");
    }

    /** Construit une URL absolue a partir d'un chemin ("/panier/") ou renvoie l'URL telle quelle. */
    public static String url(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        return baseUrl() + (path.startsWith("/") ? path : "/" + path);
    }

    public static String reportsDir() {
        return get("reports.dir", "reports");
    }
}
