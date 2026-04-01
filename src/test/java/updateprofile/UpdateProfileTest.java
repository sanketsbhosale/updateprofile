package updateprofile;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateProfileTest {

    private static final Logger logger = Logger.getLogger(UpdateProfileTest.class.getName());
    private static final String EMAIL = System.getenv("NAUKRI_EMAIL");
    private static final String PASSWORD = System.getenv("NAUKRI_PASSWORD");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final Duration SAVE_TIMEOUT = Duration.ofSeconds(20);

    private static final By LOGIN_BUTTON = By.id("login_Layer");
    private static final By EMAIL_INPUT = By.xpath("//input[@placeholder='Enter your active Email ID / Username']");
    private static final By PASSWORD_INPUT = By.xpath("//input[@type='password']");
    private static final By SUBMIT_BUTTON = By.xpath("//button[@type='submit']");
    private static final By VIEW_PROFILE_LINK = By.xpath("//a[contains(text(),'View') and @href='/mnjuser/profile']");
    private static final By PROFILE_EDIT_BUTTON = By.xpath("//em[contains(text(),'editOneTheme')]");
    private static final By BASIC_DETAILS_MODAL = By.cssSelector(".lightbox.profileEditDrawer.model_open");
    private static final By NOTICE_PERIOD_INPUT = By.id("hid_noticePeriod");
    private static final By NOTICE_PERIOD_CHIPS = By.cssSelector(".notice-period-container .chip-item");
    private static final By SAVE_BUTTON = By.id("saveBasicDetailsBtn");
    private static final By BASIC_DETAILS_ERRORS = By.cssSelector("#editBasicDetailsForm .erLbl");
    private static final By SUCCESS_LAYER = By.cssSelector(".profileUpdatedProLayer");
    private static final By SUCCESS_LAYER_CLOSE = By.cssSelector(".profileUpdatedProLayer .crossLayer");
    private static final By LOGOUT_MENU = By.cssSelector(".nI-gNb-drawer");
    private static final By LOGOUT_LINK = By.xpath("//a[@title='Logout']");

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeMethod
    public void setUp() {
        try {
            validateTestPrerequisites();

            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("start-maximized");
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            if (isCiRun()) {
                options.addArguments("--headless=new");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--window-size=1920,1080");
            }

            driver = new ChromeDriver(options);
            driver.get("https://www.naukri.com/");
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(TIMEOUT);
            wait = new WebDriverWait(driver, TIMEOUT);
            logger.info("Browser initialized successfully");
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize browser", e);
            throw new RuntimeException("Browser initialization failed", e);
        }
    }

    @Test
    public void updateNaukriTest() {
        String originalNoticePeriodId = null;
        String temporaryNoticePeriodId = null;
        boolean temporaryNoticePeriodSaved = false;

        try {
            logger.info("Starting Naukri profile update test");
            wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON)).click();

            wait.until(ExpectedConditions.presenceOfElementLocated(EMAIL_INPUT))
                    .sendKeys(EMAIL);
            driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);

            driver.findElement(SUBMIT_BUTTON).click();
            logger.info("Login credentials submitted");

            WebElement viewProfile = wait.until(ExpectedConditions.elementToBeClickable(VIEW_PROFILE_LINK));
            viewProfile.click();
            logger.info("Navigated to profile page");

            openBasicDetailsEditor();
            originalNoticePeriodId = getSelectedNoticePeriodId();
            temporaryNoticePeriodId = chooseAlternateNoticePeriodId(originalNoticePeriodId);
            selectNoticePeriod(temporaryNoticePeriodId);
            saveBasicDetails();
            temporaryNoticePeriodSaved = true;

            openBasicDetailsEditor();
            selectNoticePeriod(originalNoticePeriodId);
            saveBasicDetails();
            temporaryNoticePeriodSaved = false;
            logout();
            logger.info("Test completed successfully - logged out");
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            if (temporaryNoticePeriodSaved && originalNoticePeriodId != null) {
                restoreOriginalNoticePeriod(originalNoticePeriodId);
            }
            logger.log(Level.SEVERE, "Error occurred during test execution", e);
            captureDebugArtifacts();
            throw new RuntimeException("Test execution failed", e);
        }
    }

    @AfterMethod
    public void tearDown() {
        try {
            if (driver != null) {
                driver.quit();
                logger.info("Browser closed successfully");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error closing browser", e);
        }
    }

    private void captureDebugArtifacts() {
        if (driver == null) {
            return;
        }

        try {
            Path screenshotsDir = Path.of(System.getProperty("user.dir"), "screenshots");
            Files.createDirectories(screenshotsDir);

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot.toPath(), screenshotsDir.resolve("ci_debug.png"), StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(screenshotsDir.resolve("page_source.html"), driver.getPageSource(),
                    StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }

    private boolean isCiRun() {
        return "true".equalsIgnoreCase(System.getenv("GITHUB_ACTIONS"))
                || "true".equalsIgnoreCase(System.getenv("CI"));
    }

    private void openBasicDetailsEditor() {
        WebElement editProfile = wait.until(ExpectedConditions.elementToBeClickable(PROFILE_EDIT_BUTTON));
        clickElement(editProfile);
        wait.until(ExpectedConditions.visibilityOfElementLocated(BASIC_DETAILS_MODAL));
        logger.info("Opened basic details editor");
    }

    private String getSelectedNoticePeriodId() {
        WebElement noticePeriodInput = wait.until(ExpectedConditions.visibilityOfElementLocated(NOTICE_PERIOD_INPUT));
        String currentValue = noticePeriodInput.getAttribute("value");
        if (isBlank(currentValue)) {
            throw new IllegalStateException("Notice period value is blank in the basic details modal");
        }
        return currentValue;
    }

    private String chooseAlternateNoticePeriodId(String currentId) {
        List<WebElement> noticePeriodChips = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(NOTICE_PERIOD_CHIPS));
        for (WebElement chip : noticePeriodChips) {
            String chipId = chip.getAttribute("data-id");
            if (chip.isDisplayed() && !isBlank(chipId) && !chipId.equals(currentId)) {
                return chipId;
            }
        }
        throw new IllegalStateException("Could not find an alternate notice period option");
    }

    private void selectNoticePeriod(String noticePeriodId) {
        for (WebElement chip : wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(NOTICE_PERIOD_CHIPS))) {
            if (!chip.isDisplayed()) {
                continue;
            }

            if (noticePeriodId.equals(chip.getAttribute("data-id"))) {
                clickElement(chip);
                wait.until(driver -> noticePeriodId.equals(driver.findElement(NOTICE_PERIOD_INPUT).getAttribute("value")));
                logger.info("Selected notice period option with id " + noticePeriodId);
                return;
            }
        }

        throw new IllegalStateException("Notice period option not found for id " + noticePeriodId);
    }

    private void restoreOriginalNoticePeriod(String originalNoticePeriodId) {
        try {
            openBasicDetailsEditor();
            selectNoticePeriod(originalNoticePeriodId);
            saveBasicDetails();
            logger.info("Restored original notice period after a failed run");
        } catch (Exception restoreException) {
            logger.log(Level.WARNING, "Failed to restore the original notice period", restoreException);
        }
    }

    private void saveBasicDetails() {
        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(SAVE_BUTTON));
        clickElement(saveButton);
        logger.info("Clicked save button");
        waitForSaveOutcome();
    }

    private void waitForSaveOutcome() {
        WebDriverWait saveWait = new WebDriverWait(driver, SAVE_TIMEOUT);

        saveWait.until(d -> {
            List<String> validationErrors = getVisibleValidationErrors();
            if (!validationErrors.isEmpty()) {
                throw new IllegalStateException("Basic details validation failed: " + String.join(" | ", validationErrors));
            }

            return !isElementVisible(BASIC_DETAILS_MODAL) || isElementVisible(SUCCESS_LAYER);
        });

        dismissSuccessLayerIfVisible();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(BASIC_DETAILS_MODAL));
    }

    private List<String> getVisibleValidationErrors() {
        List<String> validationErrors = new java.util.ArrayList<>();

        for (WebElement errorElement : driver.findElements(BASIC_DETAILS_ERRORS)) {
            if (!errorElement.isDisplayed()) {
                continue;
            }

            String errorText = normalizeWhitespace(errorElement.getText());
            if (!errorText.isEmpty()) {
                validationErrors.add(errorText);
            }
        }

        return validationErrors;
    }

    private void dismissSuccessLayerIfVisible() {
        if (!isElementVisible(SUCCESS_LAYER)) {
            return;
        }

        WebElement successClose = wait.until(ExpectedConditions.elementToBeClickable(SUCCESS_LAYER_CLOSE));
        clickElement(successClose);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(SUCCESS_LAYER));
    }

    private void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_MENU)).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();
    }

    private void clickElement(WebElement element) {
        try {
            element.click();
        } catch (Exception clickException) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private boolean isElementVisible(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            if (element.isDisplayed()) {
                return true;
            }
        }
        return false;
    }

    private String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("\\s+", " ").trim();
    }

    private void validateTestPrerequisites() {
        if (isBlank(EMAIL) || isBlank(PASSWORD)) {
            throw new SkipException("Skipping live Naukri automation. Set NAUKRI_EMAIL and NAUKRI_PASSWORD to run it.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
