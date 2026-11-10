package tn.expertgaming.tests;

import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.config.Config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;

/** M13 - Securite de base (ENF03) : controles passifs uniquement (aucune attaque). */
public class SecurityTest extends BaseTest {

    @Test(groups = {"quality", "security", "smoke"},
            description = "TC18 - Verifier le certificat HTTPS et la redirection HTTP vers HTTPS")
    public void tc18_httpsAndRedirect() throws Exception {
        SoftAssert soft = new SoftAssert();
        String httpsUrl = Config.baseUrl().replaceFirst("^http://", "https://");
        String httpUrl = httpsUrl.replaceFirst("^https://", "http://");

        HttpClient noRedirect = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        // 1) Redirection HTTP -> HTTPS cote serveur
        HttpResponse<Void> http = noRedirect.send(
                HttpRequest.newBuilder(URI.create(httpUrl + "/")).timeout(Duration.ofSeconds(20)).GET().build(),
                HttpResponse.BodyHandlers.discarding());
        String location = http.headers().firstValue("location").orElse("");
        Reporter.log("HTTP " + httpUrl + "/ -> statut " + http.statusCode() + ", Location : " + location);
        soft.assertTrue(Set.of(301, 302, 307, 308).contains(http.statusCode()),
                "HTTP ne redirige pas (statut " + http.statusCode() + ")");
        soft.assertTrue(location.startsWith("https://"), "La redirection ne pointe pas vers HTTPS : '" + location + "'");

        // 2) Certificat valide : la connexion TLS aboutit avec la verification standard du JDK
        HttpResponse<Void> https = noRedirect.send(
                HttpRequest.newBuilder(URI.create(httpsUrl + "/")).timeout(Duration.ofSeconds(20)).GET().build(),
                HttpResponse.BodyHandlers.discarding());
        Reporter.log("HTTPS " + httpsUrl + "/ -> statut " + https.statusCode());
        soft.assertTrue(https.statusCode() < 400, "La page HTTPS repond en erreur (statut " + https.statusCode() + ")");
        for (String header : new String[]{"strict-transport-security", "x-content-type-options", "x-frame-options",
                "content-security-policy", "referrer-policy"}) {
            Reporter.log("En-tete " + header + " : " + https.headers().firstValue(header).orElse("ABSENT"));
        }

        // 3) Dans le navigateur, l'adresse http:// aboutit bien sur https://
        driver.get(httpUrl + "/");
        soft.assertTrue(driver.getCurrentUrl().startsWith("https://"),
                "Dans le navigateur, http:// n'aboutit pas sur https:// (URL : " + driver.getCurrentUrl() + ")");
        soft.assertAll();
    }
}
