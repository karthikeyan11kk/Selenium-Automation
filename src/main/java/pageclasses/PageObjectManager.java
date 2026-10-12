package pageclasses;

import org.openqa.selenium.WebDriver;

/**
 * Matches the demo framework's pageclasses.PageObjectManager: one place
 * that creates page objects on demand instead of scattering "new XPage()"
 * calls across step definitions. New1CShop/New1CPropertyOwners take no
 * constructor argument (they read the current driver from
 * utils.Initialize.getWebDriver() internally) - kept that way here rather
 * than changing their constructors, to avoid touching their much larger,
 * business-critical method bodies.
 */
public class PageObjectManager {

    private final WebDriver driver;

    public PageObjectManager(WebDriver driver) {
        this.driver = driver;
    }

    public LoginPage getLoginPage() {
        return new LoginPage(driver);
    }

    public New1CShop getNew1CShop() {
        return new New1CShop();
    }

    public New1CPropertyOwners getNew1CPropertyOwners() {
        return new New1CPropertyOwners();
    }

    /** Feature 3: dedicated Shop renewal page class, switch-cased on the "Renewal Execution" column. */
    public ShopRenewal getShopRenewal() {
        return new ShopRenewal();
    }
}
