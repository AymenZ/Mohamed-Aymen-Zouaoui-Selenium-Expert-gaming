package tn.expertgaming.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ContactPage extends BasePage {

    public static final String PATH = "/contact/";

    private WebElement form;
    private WebElement emailField;

    public ContactPage(WebDriver driver) {
        super(driver);
    }

    public ContactPage goTo() {
        open(PATH);
        form = locateForm();
        // le formulaire peut etre injecte tardivement (JS) : on reessaie quelques secondes
        for (int i = 0; i < 6 && form == null; i++) {
            sleep(1000);
            form = locateForm();
        }
        return this;
    }

    /** Le formulaire de contact est celui qui contient une zone de message (textarea). */
    private WebElement locateForm() {
        for (WebElement f : driver.findElements(By.tagName("form"))) {
            try {
                if (f.isDisplayed() && !f.findElements(By.tagName("textarea")).isEmpty()) {
                    return f;
                }
            } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
                // on passe au suivant
            }
        }
        return null;
    }

    public boolean hasForm() {
        return form != null;
    }

    /** Remplit et envoie le formulaire. Seules des donnees invalides doivent etre fournies par les tests. */
    public void fillAndSubmit(String name, String email, String subject, String message) {
        if (form == null) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "Formulaire de contact introuvable (page " + PATH + ")");
        }
        for (WebElement input : form.findElements(By.cssSelector("input"))) {
            String type = String.valueOf(input.getAttribute("type")).toLowerCase();
            String hint = (String.valueOf(input.getAttribute("name")) + String.valueOf(input.getAttribute("id"))
                    + String.valueOf(input.getAttribute("placeholder"))).toLowerCase();
            if (!input.isDisplayed()) {
                continue;
            }
            if (type.equals("email") || hint.contains("mail")) {
                set(input, email);
                emailField = input;
            } else if (hint.contains("subject") || hint.contains("sujet")  || hint.contains("objet")) {
                set(input, subject);
            } else if (type.equals("text") || type.equals("tel") || type.isEmpty()) {
                set(input, name);
            }
        }
        for (WebElement area : form.findElements(By.tagName("textarea"))) {
            if (area.isDisplayed()) {
                set(area, message);
            }
        }
        WebElement submit = null;
        for (WebElement s : form.findElements(By.cssSelector(Locators.CONTACT_SUBMIT))) {
            if (s.isDisplayed()) {
                submit = s;
                break;
            }
        }
        if (submit == null) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "Bouton d'envoi du formulaire de contact introuvable : adapter Locators.CONTACT_SUBMIT");
        }
        click(submit);
        sleep(2000);
    }

    private void set(WebElement field, String value) {
        field.clear();
        if (value != null && !value.isEmpty()) {
            field.sendKeys(value);
        }
    }

    /** Vrai si le navigateur ou la page refuse le champ e-mail (validation native HTML5). */
    public boolean emailRejectedByBrowser() {
        if (emailField == null) {
            return false;
        }
        try {
            Object valid = js("return arguments[0].checkValidity();", emailField);
            return valid instanceof Boolean && !((Boolean) valid);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean showsErrorMessage() {
        return firstVisible(Locators.CONTACT_ERRORS) != null;
    }

    public boolean showsSuccessMessage() {
        String t = bodyText().toLowerCase();
        return t.contains("merci pour votre message") || t.contains("message a bien été envoyé")
                || t.contains("message a été envoyé") || t.contains("message envoyé")
                || t.contains("thank you for your message") || t.contains("message was sent");
    }
}
