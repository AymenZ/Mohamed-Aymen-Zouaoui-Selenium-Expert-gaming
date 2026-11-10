# Expert Gaming – Tests automatisés Selenium (Java / Maven / TestNG)

Projet de tests automatisés du site **https://www.expert-gaming.tn** : 20 cas de test (TC01 à TC20), alimentés par des jeux de données CSV, avec rapport HTML, captures d'écran et traces d'erreurs.
Il applique le *Plan de test* et le *Cahier des charges* (modules M01 à M13, exigences EF/ENF).

> **Statut :** le code a été vérifié à la compilation et la logique de rapport a été testée, mais **les tests n'ont pas encore été exécutés contre le site réel**. Les sélecteurs CSS sont écrits pour WooCommerce / thème Woodmart avec plusieurs variantes de secours ; lors du premier lancement, certains peuvent demander un ajustement (voir section 7). Un échec « élément introuvable » n'est donc pas forcément un défaut du site.

---

## 1. Prérequis

| Outil | Version | Remarque |
|---|---|---|
| JDK | **17 ou plus** | `java -version` |
| Maven | 3.9+ | Inclus dans IntelliJ ; pour VS Code, installer Maven ou utiliser l'extension *Maven for Java* |
| Google Chrome | dernière version | Firefox et Edge sont optionnels (TC17, ou `-Dbrowser=firefox`) |
| Connexion Internet | – | Selenium Manager télécharge tout seul le bon *driver* (chromedriver, etc.) : rien à installer |

## 2. Importer le projet

**IntelliJ IDEA** : `File > Open…` → choisir le dossier du projet (celui qui contient `pom.xml`) → *Trust Project*. Attendre la fin de l'import Maven (barre de progression en bas).

**VS Code** : installer *Extension Pack for Java* (Microsoft) et, pour lancer facilement les tests, l'extension *TestNG* intégrée au pack. `File > Open Folder…` → dossier du projet. Si Maven n'est pas installé, l'extension *Maven for Java* propose de l'utiliser en mode intégré.

## 3. Lancer les tests

**Toute la suite (TC01–TC20)**
- IntelliJ : clic droit sur `testng.xml` → *Run '…\testng.xml'*
- Terminal (racine du projet) : `mvn test`

**Suite rapide (parcours vitaux uniquement)** : `mvn test -DsuiteXmlFile=testng-smoke.xml`

**Une seule classe ou un seul test** : `mvn test -Dtest=CartTest` ou `mvn test -Dtest=CartTest#tc06_addSameProductTwice` (avec cette option, la suite `testng.xml` est ignorée, mais le rapport HTML n'est généré que si le listener est actif : préférez `testng.xml` ou lancez la classe depuis l'IDE avec le listener, voir section 8).

**Options utiles** (ajoutez-les à la commande Maven, ou dans *VM options* de l'IDE avec le même format) :

| Option | Effet |
|---|---|
| `-Dbrowser=firefox` | `chrome` (défaut), `firefox` ou `edge` |
| `-Dheadless=true` | navigateur sans fenêtre (plus rapide, idéal pour la 1re exécution complète) |
| `-Dbase.url=https://…` | autre environnement (recette / staging) |
| `-Dcheckout.submitEmpty=true` | TC08 clique réellement sur « Commander » avec un formulaire vide (voir section 9) |

Exemple : `mvn test -Dheadless=true -Dbrowser=chrome`

## 4. Résultats produits (dossier `reports/`)

| Fichier / dossier | Contenu |
|---|---|
| `reports/execution-report.html` | **Rapport d'exécution** : Passed / Failed / Skipped, durée, message d'erreur, vignette de capture, lien vers la trace. Ouvrir dans un navigateur. |
| `reports/screenshots/*.png` | **Captures d'écran** automatiques des tests échoués |
| `reports/errors/*.txt` | **Traces** : test, paramètres, URL, titre de page, exception complète, messages du test, console du navigateur |
| `target/surefire-reports/` et `test-output/` | Rapports natifs TestNG / Surefire (complémentaires) |

Les dossiers `screenshots` et `errors` sont vidés au début de chaque exécution complète.

## 5. Structure du projet

```
expert-gaming-selenium/
├── pom.xml                       Dépendances (Selenium 4, TestNG 7) et plugin Surefire
├── testng.xml                    Suite complète : 4 blocs <test>, 13 classes
├── testng-smoke.xml              Suite rapide (groupe "smoke")
├── README.md
├── reports/                      Rapport HTML, captures, traces (générés)
└── src
    ├── main
    │   ├── java/tn/expertgaming
    │   │   ├── config/Config.java            Lecture de config.properties (+ surcharge -D)
    │   │   ├── driver/DriverFactory.java     Création Chrome / Firefox / Edge
    │   │   ├── utils/                        CsvReader, PriceUtil, ScreenshotUtil
    │   │   └── pages/                        Page Objects + Locators.java (TOUS les sélecteurs)
    │   └── resources/config.properties       URL, navigateur, délais, seuils
    └── test
        ├── java/tn/expertgaming
        │   ├── base/BaseTest.java            Démarrage / arrêt du navigateur (1 par classe)
        │   ├── data/DataProviders.java       Lecture des CSV pour TestNG
        │   ├── listeners/                    TestListener (rapport HTML, captures, traces)
        │   └── tests/                        Les 20 cas de test
        └── resources/data/                   Jeux de données (CSV + Jeux_de_donnees.xlsx)
```

## 6. Correspondance avec le Plan de test

| ID | Classe | Module / exigence | Données | Remarque |
|---|---|---|---|---|
| TC01 | NavigationTest | M01 / EF01 | `menu_links.csv` (14 liens) | Un test par lien du menu |
| TC02 | NavigationTest | M01 / EF01 | – | Viewport 390×844, ouverture du menu, accès à « PC Gamer » |
| TC03 | SearchTest | M02 / EF02 | `search_terms.csv` (expect=true) | Résultats + pertinence |
| TC04 | SearchTest | M02 / EF02 | `search_terms.csv` (expect=false) | Message « aucun résultat » |
| TC05 | ProductTest | M03 / EF03 | – | 1er produit de `/promotions/` : nom, image, prix, ancien prix, bouton |
| TC06 | CartTest | M04 / EF04 | – | Ajout ×2 → quantité 2 et sous-total ≈ 2 × prix |
| TC07 | CartTest | M04 / EF04 | – | Dépend de TC06 (sinon *Skipped*) |
| TC08 | CheckoutTest | M05 / EF05 | – | Mode sécurisé par défaut (voir section 9) |
| TC09 | CheckoutTest | M05 / EF05 | – | S'arrête au choix du paiement, ne valide rien. Dépend de TC08 |
| TC10 | AccountTest | M06 / EF06 | `register_data.csv` | **E-mails invalides** (voir note ci-dessous) |
| TC11 | AccountTest | M06 / EF06 | `login_data.csv` | Identifiants fictifs refusés |
| TC12 | CompareTest | M07 / EF07 | – | 3 produits dans le comparateur |
| TC13 | TrackOrderTest | M08 / EF08 | `tracking_data.csv` | Commandes inexistantes |
| TC14 | ContactTest | M11 / EF10 | `contact_data.csv` | E-mails invalides, aucun message réel envoyé |
| TC15 | LegalPagesTest | M12 / EF11 | `legal_pages.csv` | Ouverture depuis le pied de page + contenu minimal |
| TC16 | QualityTest | M13 / ENF01 | – | LCP et temps de chargement mesurés par l'API du navigateur (seuils dans `config.properties`) |
| TC17 | CrossBrowserTest | M13 / ENF02 | `browsers.csv` | Chrome, Firefox, Edge ; navigateur absent → *Skipped* ; Safari non testé |
| TC18 | SecurityTest | M13 / ENF03 | – | Redirection HTTP→HTTPS, certificat valide, en-têtes de sécurité (informatif) |
| TC19 | QualityTest | M13 / ENF05 | – | Bouton « Ajouter au panier » atteint à la touche Tab, focus visible, Entrée |
| TC20 | QualityTest | M13 / ENF06 | `seo_pages.csv` | Title, meta description, H1, doublons |

Chaque cas peut produire plusieurs exécutions grâce aux jeux de données : **50 exécutions** au total.

**Écarts volontaires par rapport au plan** (site en production) :
- **TC10** teste des e-mails *invalides* au lieu d'un e-mail « déjà utilisé » : cela évite de créer de vrais comptes sur le site.
- **TC16** utilise les métriques du navigateur (LCP, `loadEventEnd`) et non Lighthouse ; les valeurs dépendent de votre machine et de votre réseau. Les scores Lighthouse du plan restent à relever à la main dans Chrome (onglet *Lighthouse* des DevTools).
- **TC02** vérifie l'accès à une famille de produits depuis le menu mobile, pas à une sous-catégorie.

## 7. Si un test échoue parce qu'un élément n'est pas trouvé

Tous les sélecteurs CSS sont dans **`src/main/java/tn/expertgaming/pages/Locators.java`**. La démarche :
1. Ouvrir la capture et la trace du test échoué (`reports/`) pour voir où le test s'est arrêté.
2. Sur le site, clic droit sur l'élément → *Inspecter* → repérer sa classe ou son `id`.
3. Ajouter ce sélecteur à la constante concernée (variantes séparées par une virgule), par exemple `SEARCH_INPUT`.
4. Relancer uniquement la classe concernée.

Autres réglages dans `src/main/resources/config.properties` : `products.list.path` (liste utilisée pour trouver un produit achetable), `promo.path`, `product.url.tokens` (fragments d'URL des fiches produit, utile si les URLs ne contiennent ni `/produit/` ni `/product/`), seuils `perf.*`.

## 8. Lancer depuis l'IDE sans testng.xml

Le rapport HTML et les captures sont produits par `TestListener`. Si vous lancez une classe seule depuis l'IDE, ajoutez le listener : dans IntelliJ, *Run > Edit Configurations > TestNG > Listeners* → ajouter `tn.expertgaming.listeners.TestListener`. Le plus simple reste de passer par `testng.xml`, où il est déjà déclaré.

## 9. Règles de sécurité des tests (site en production)

- **Aucune commande n'est jamais finalisée.** TC08 et TC09 s'arrêtent avant la validation.
- **TC08 est « sécurisé » par défaut** (`checkout.submitEmpty=false`) : il vérifie la présence des champs obligatoires sans cliquer sur « Commander ». Avec `-Dcheckout.submitEmpty=true`, il clique sur « Commander » avec un formulaire vide ; à n'utiliser qu'avec l'accord du propriétaire ou sur un environnement de recette.
- Aucun compte réel n'est utilisé ou créé ; aucun message de contact valide n'est envoyé ; aucun test d'intrusion actif.
- Le panier de test est vidé à chaque exécution car chaque classe démarre un navigateur neuf.

## 10. Dépannage

| Symptôme | Piste |
|---|---|
| `SessionNotCreatedException` / driver introuvable | Vérifier la connexion Internet (Selenium Manager) et que Chrome est installé |
| `release version 17 not supported` | Le JDK est trop ancien : installer JDK 17+ et le choisir dans *Project Structure* |
| Les clics n'ont aucun effet / timeouts | Un bandeau ou le widget de chat masque la page : ajouter son sélecteur dans `Locators.COOKIE_BUTTONS` |
| TC17 *Skipped* pour Firefox ou Edge | Navigateur non installé : comportement normal |
| TC12 / TC05 échouent sur « aucun produit » | Adapter `PRODUCT_CARD` et `product.url.tokens` |
| `reports/execution-report.html` absent | La suite a été lancée sans le listener : voir section 8 |
