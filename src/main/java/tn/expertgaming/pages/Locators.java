package tn.expertgaming.pages;

/**
 * TOUS les selecteurs CSS du projet sont ici.
 * Chaque constante liste plusieurs variantes separees par des virgules (WooCommerce standard,
 * theme Woodmart, selecteurs generiques). Si un test echoue parce qu'un element n'est pas trouve,
 * ajustez le selecteur correspondant dans ce fichier (clic droit > Inspecter dans le navigateur).
 */
public final class Locators {

    private Locators() {
    }

    // ---------- Bandeaux cookies / popups ----------
    public static final String[] COOKIE_BUTTONS = {
            "#onetrust-accept-btn-handler",
            "#cookie-law-info-bar .cli_action_button",
            "#wt-cli-accept-all-btn",
            ".cmplz-accept",
            "#CybotCookieDialogBodyLevelButtonLevelOptinAllowAll",
            ".cc-allow",
            ".cc-dismiss",
            "button#accept-cookies"
    };

    // ---------- Navigation et recherche ----------
    public static final String HEADER_LINKS = "header a[href], nav a[href], .wd-nav a[href]";
    public static final String ALL_LINKS = "a[href]";
    public static final String SEARCH_INPUT =
            "form.searchform input[name='s'], input.s, input[name='s'], input[type='search']";
    public static final String SEARCH_TOGGLE =
            ".wd-header-search > a, .wd-header-search a, .search-button, button[class*='search'], "
                    + "a[class*='search-toggle'], a[class*='search-icon']";
    public static final String MOBILE_MENU_TOGGLE =
            ".wd-header-mobile-nav a, .wd-header-mobile-nav, .mobile-nav-icon, button.menu-toggle, "
                    + ".menu-toggle, .navbar-toggler, [class*='hamburger'], [class*='mobile-menu'] a";
    public static final String LOGO = "header img, .site-logo img, .wd-logo img, a.logo img";

    // ---------- Catalogue ----------
    public static final String PRODUCT_CARD =
            "li.product, .wd-product, .product-grid-item, article.product, .products .product";
    /** Ordre de preference pour trouver le lien d'un produit dans une carte. */
    public static final String[] PRODUCT_LINK_PRIORITY = {
            "a.woocommerce-LoopProduct-link",
            ".wd-entities-title a",
            ".woocommerce-loop-product__title a",
            ".product-title a",
            "h2 a",
            "h3 a",
            "a.product-image-link",
            "a[href]"
    };

    // ---------- Fiche produit ----------
    public static final String PRODUCT_TITLE = "h1.product_title, h1.entry-title, h1";
    public static final String PRODUCT_PRICE_SALE =
            ".summary ins .amount, p.price ins .amount, .price ins .amount";
    public static final String PRODUCT_PRICE =
            ".summary .price .amount, p.price .amount, .price .amount, .summary .price, p.price";
    public static final String PRODUCT_OLD_PRICE =
            ".summary del .amount, p.price del .amount, .price del .amount, .summary del, p.price del";
    public static final String PRODUCT_IMAGE =
            ".woocommerce-product-gallery img, .product-images img, img.wp-post-image";
    public static final String ADD_TO_CART =
            "button.single_add_to_cart_button, .single_add_to_cart_button, "
                    + "form.cart button[type='submit'], button[name='add-to-cart']";
    public static final String OUT_OF_STOCK = ".out-of-stock, p.stock.out-of-stock";
    public static final String ADDED_NOTICE =
            ".woocommerce-message, .added_to_cart, .wd-notice, .woocommerce-notices-wrapper .woocommerce-message";

    // ---------- Panier ----------
    public static final String CART_ITEM =
            "tr.woocommerce-cart-form__cart-item, tr.cart_item, .cart_item";
    public static final String CART_QTY =
            "tr.woocommerce-cart-form__cart-item input.qty, tr.cart_item input.qty, input.qty, "
                    + "input[name*='quantity']";
    public static final String CART_REMOVE =
            "tr.woocommerce-cart-form__cart-item a.remove, td.product-remove a, a.remove";
    public static final String CART_UPDATE = "button[name='update_cart'], input[name='update_cart']";
    public static final String CART_SUBTOTAL = ".cart_totals .cart-subtotal .amount, .cart-subtotal .amount";
    public static final String CART_TOTAL = ".cart_totals .order-total .amount, .order-total .amount";
    public static final String CART_EMPTY_MSG = ".cart-empty, p.cart-empty, .wc-empty-cart-message";

    // ---------- Commande ----------
    public static final String CHECKOUT_BUTTON = "a.checkout-button, .wc-proceed-to-checkout a";
    public static final String CHECKOUT_FORM = "form.checkout, form[name='checkout']";
    public static final String PAYMENT_METHODS = "ul.wc_payment_methods > li, li.wc_payment_method";
    public static final String REQUIRED_FIELDS =
            "form.checkout .validate-required, form.checkout abbr.required, form.checkout input[required], "
                    + "form.checkout [aria-required='true']";
    public static final String PLACE_ORDER = "#place_order, button[name='woocommerce_checkout_place_order']";
    public static final String CHECKOUT_ERRORS =
            ".woocommerce-error, .woocommerce-NoticeGroup, .woocommerce-invalid, .woocommerce-notice--error";
    public static final String[] CHECKOUT_PATHS = {"/commander/", "/checkout/", "/passer-la-commande/"};

    // ---------- Compte client ----------
    public static final String LOGIN_USER = "#username, input[name='username']";
    public static final String LOGIN_PASSWORD = "#password, input[name='password']";
    public static final String LOGIN_SUBMIT =
            "button[name='login'], button.woocommerce-form-login__submit, form.login button[type='submit']";
    public static final String REGISTER_EMAIL = "#reg_email, form.register input[name='email']";
    public static final String REGISTER_SUBMIT =
            "button[name='register'], button.woocommerce-form-register__submit, form.register button[type='submit']";
    public static final String ACCOUNT_ERRORS =
            ".woocommerce-error, .woocommerce-notice--error, .woocommerce-message--error";
    public static final String LOGGED_IN_MARKERS =
            "a[href*='customer-logout'], .woocommerce-MyAccount-navigation";

    // ---------- Suivi de commande ----------
    public static final String TRACK_ORDER_ID = "#orderid, input[name='orderid']";
    public static final String TRACK_EMAIL = "#order_email, input[name='order_email']";
    public static final String TRACK_SUBMIT = "button[name='track'], form.track_order button, form.track_order input[type='submit']";
    public static final String TRACK_ERRORS = ".woocommerce-error, .woocommerce-notice--error";

    // ---------- Contact ----------
    public static final String CONTACT_SUBMIT =
            "input[type='submit'], button[type='submit'], .wpcf7-submit, button.elementor-button";
    public static final String CONTACT_ERRORS =
            ".wpcf7-not-valid-tip, .wpcf7-response-output, .elementor-message-danger, "
                    + ".forminator-error-message, .wpforms-error, label.error, .error-message, .is-invalid";

    // ---------- Comparateur ----------
    public static final String COMPARE_BUTTON =
            ".wd-compare-btn a, a.compare, a.compare-button, a[class*='compare'], button[class*='compare']";

    // ---------- Contenu des pages ----------
    public static final String[] CONTENT_AREAS = {
            ".entry-content", ".page-content", "article", "main", "#main-content", ".site-content", "#content"
    };
}
