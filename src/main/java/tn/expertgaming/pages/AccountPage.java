package tn.expertgaming.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AccountPage extends BasePage {

    public static final String PATH = "/my-account/";

    public AccountPage(WebDriver driver) {
        super(driver);
    }

    public AccountPage goTo() {
        open(PATH);
        return this;
    }

    public boolean hasLoginForm() {
        return firstVisible(Locators.LOGIN_USER) != null && firstVisible(Locators.LOGIN_PASSWORD) != null;
    }

    public boolean hasRegisterForm() {
        return firstVisible(Locators.REGISTER_EMAIL) != null;
    }

    private void type(WebElement field, String value) {
        field.clear();
        if (value != null && !value.isEmpty()) {
            field.sendKeys(value);
        }
    }

    /** Renvoie le champ identifiant pour verifier sa validite HTML5 apres la soumission. */
    public WebElement login(String user, String password) {
        WebElement userField = firstVisible(Locators.LOGIN_USER);
        WebElement passField = firstVisible(Locators.LOGIN_PASSWORD);
        WebElement submit = firstVisible(Locators.LOGIN_SUBMIT);
        if (userField == null || passField == null || submit == null) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "Formulaire de connexion introuvable : adapter Locators.LOGIN_*");
        }
        type(userField, user);
        type(passField, password);
        click(submit);
        sleep(1500);
        return userField;
    }

    /** Renvoie le champ e-mail du formulaire d'inscription. */
    public WebElement register(String email) {
        WebElement emailField = firstVisible(Locators.REGISTER_EMAIL);
        WebElement submit = firstVisible(Locators.REGISTER_SUBMIT);
        if (emailField == null || submit == null) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "Formulaire d'inscription introuvable : adapter Locators.REGISTER_*");
        }
        type(emailField, email);
        click(submit);
        sleep(1500);
        return emailField;
    }

    public boolean hasErrorMessage() {
        return firstVisible(Locators.ACCOUNT_ERRORS) != null;
    }

    public boolean isLoggedIn() {
        return firstVisible(Locators.LOGGED_IN_MARKERS) != null;
    }

    /** Validation native du navigateur : false si le champ est refuse (vide ou format invalide). */
    public boolean fieldIsValid(WebElement field) {
        try {
            Object valid = js("return arguments[0].checkValidity();", field);
            return !(valid instanceof Boolean) || (Boolean) valid;
        } catch (RuntimeException e) {
            return true;
        }
    }
}
