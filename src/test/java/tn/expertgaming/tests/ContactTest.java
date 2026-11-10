package tn.expertgaming.tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.data.DataProviders;
import tn.expertgaming.pages.ContactPage;

/**
 * M11 - Contact (EF10). Seuls des e-mails invalides sont envoyes : aucun message reel ne doit partir.
 */
public class ContactTest extends BaseTest {

    @Test(groups = {"contact"},
            dataProvider = "invalidContacts", dataProviderClass = DataProviders.class,
            description = "TC14 - Envoyer le formulaire de contact avec un e-mail invalide")
    public void tc14_contactWithInvalidEmail(String scenario, String name, String email, String subject, String message) {
        ContactPage contact = new ContactPage(driver).goTo();
        Assert.assertTrue(contact.hasForm(),
                "Formulaire de contact (avec zone de message) introuvable sur " + ContactPage.PATH);

        contact.fillAndSubmit(name, email, subject, message);
        boolean rejectedByBrowser = contact.emailRejectedByBrowser();
        boolean errorShown = contact.showsErrorMessage();
        Reporter.log("Scenario '" + scenario + "' : refus navigateur=" + rejectedByBrowser + ", message d'erreur=" + errorShown);

        SoftAssert soft = new SoftAssert();
        soft.assertTrue(rejectedByBrowser || errorShown,
                "L'e-mail invalide '" + email + "' n'est ni refuse par le navigateur ni signale par la page");
        soft.assertFalse(contact.showsSuccessMessage(),
                "ALERTE : un message de succes est affiche malgre l'e-mail invalide '" + email + "'");
        soft.assertAll();
    }
}
