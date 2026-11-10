package tn.expertgaming.listeners;

/** Resultat d'une execution de test, utilise pour construire le rapport HTML. */
public class TestRecord {
    public String tcId = "";
    public String description = "";
    public String className = "";
    public String methodName = "";
    public String parameters = "";
    public String status = "";      // PASSED | FAILED | SKIPPED
    public long durationMs;
    public String message = "";
    public String screenshot;       // chemin relatif a reports/ (ex. screenshots/x.png) ou null
    public String trace;            // chemin relatif a reports/ (ex. errors/x.txt) ou null
    public String logs = "";        // messages Reporter.log du test
}
