package tn.expertgaming.tests;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.data.DataProviders;
import tn.expertgaming.pages.AccountPage;

/**
 * M06 - Compte client (EF06).
 * SECURITE : aucun compte reel n'est cree ni utilise. Les inscriptions testees utilisent des e-mails
 * invalides (refuses), et les connexions des identifiants fictifs.
 */
public class AccountTest extends BaseTest {

    @Test(groups = {"account"},
            dataProvider = "invalidRegistrations", dataProviderClass = DataProviders.class,
            description = "TC10 - Creer un compte avec un e-mail invalide")
    public void tc10_registerWithInvalidEmail(String scenario, String email) {
        AccountPage account = new AccountPage(driver).goTo();
        Assert.assertTrue(account.hasRegisterForm(),
                "Formulaire d'inscription introuvable sur " + AccountPage.PATH + ". Adapter Locators.REGISTER_EMAIL.");

        WebElement emailField = account.register(email);
        boolean rejectedByBrowser = !account.fieldIsValid(emailField);
        boolean errorShown = account.hasErrorMessage();
        Reporter.log("Scenario '" + scenario + "' : refus navigateur=" + rejectedByBrowser + ", message d'erreur=" + errorShown);

        Assert.assertTrue(rejectedByBrowser || errorShown,
                "L'e-mail invalide '" + email + "' n'a pas ete refuse (ni validation navigateur, ni message d'erreur)");
        Assert.assertFalse(account.isLoggedIn(), "Une session client semble ouverte apres une inscription invalide");
    }

    @Test(groups = {"account", "smoke"},
            dataProvider = "invalidLogins", dataProviderClass = DataProviders.class,
            description = "TC11 - Se connecter avec des identifiants invalides")
    public void tc11_loginWithInvalidCredentials(String scenario, String user, String password) {
        AccountPage account = new AccountPage(driver).goTo();
        Assert.assertTrue(account.hasLoginForm(),
                "Formulaire de connexion introuvable sur " + AccountPage.PATH + ". Adapter Locators.LOGIN_*.");

        WebElement userField = account.login(user, password);
        boolean rejectedByBrowser = !account.fieldIsValid(userField);
        boolean errorShown = account.hasErrorMessage();
        Reporter.log("Scenario '" + scenario + "' : refus navigateur=" + rejectedByBrowser + ", message d'erreur=" + errorShown);

        Assert.assertFalse(account.isLoggedIn(), "Connexion acceptee avec des identifiants invalides (" + scenario + ")");
        Assert.assertTrue(rejectedByBrowser || errorShown,
                "Aucun retour utilisateur (message d'erreur ou validation) pour le scenario '" + scenario + "'");
    }
}
