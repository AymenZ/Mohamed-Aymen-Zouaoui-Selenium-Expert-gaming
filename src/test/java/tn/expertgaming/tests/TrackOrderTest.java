package tn.expertgaming.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import tn.expertgaming.base.BaseTest;
import tn.expertgaming.data.DataProviders;
import tn.expertgaming.pages.TrackOrderPage;

/** M08 - Suivi de commande (EF08). Seuls des numeros de commande inexistants sont testes. */
public class TrackOrderTest extends BaseTest {

    @Test(groups = {"tracking"},
            dataProvider = "unknownOrders", dataProviderClass = DataProviders.class,
            description = "TC13 - Saisir un numero de commande inexistant dans le suivi")
    public void tc13_trackUnknownOrder(String scenario, String orderId, String email) {
        TrackOrderPage page = new TrackOrderPage(driver).goTo();
        Assert.assertTrue(page.hasForm(),
                "Formulaire de suivi introuvable sur " + TrackOrderPage.PATH + ". Adapter Locators.TRACK_*.");
        page.track(orderId, email);
        Assert.assertTrue(page.showsError(),
                "Aucun message d'erreur pour la commande inexistante '" + orderId + "' (" + scenario + ")");
    }
}
