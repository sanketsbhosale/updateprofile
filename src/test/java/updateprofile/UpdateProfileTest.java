package updateprofile;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateProfileTest {

    private static final Logger logger = Logger.getLogger(UpdateProfileTest.class.getName());
    private static final String EMAIL = System.getenv("NAUKRI_EMAIL");
    private static final String PASSWORD = System.getenv("NAUKRI_PASSWORD");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final Duration SAVE_TIMEOUT = Duration.ofSeconds(20);
    private static final String PROFILE_UPDATE_MARKER = ".";

    private static final By LOGIN_BUTTON = By.id("login_Layer");
    private static final By EMAIL_INPUT = By.xpath("//input[@placeholder='Enter your active Email ID / Username']");
    private static final By PASSWORD_INPUT = By.xpath("//input[@type='password']");
    private static final By SUBMIT_BUTTON = By.xpath("//button[@type='submit']");
    private static final By VIEW_PROFILE_LINK = By.xpath("//a[contains(text(),'View') and @href='/mnjuser/profile']");
    private static final By PROFILE_EDIT_BUTTON = By.xpath("//em[contains(text(),'editOneTheme')]");
    private static final By BASIC_DETAILS_MODAL = By.cssSelector(".lightbox.profileEditDrawer.model_open");
    private static final By NAME_INPUT = By.id("name");
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
                options.addArguments("--disable-gpu"); // Added for Linux stability
                options.addArguments("--window-size=1920,1080");
            }

            driver = new ChromeDriver(options);
            driver.get("https://www.naukri.com/");
            driver.manage().timeouts().implicitlyWait(TIMEOUT);
            wait = new WebDriverWait(driver, TIMEOUT);
            logger.info("Browser initialized successfully in " + (isCiRun() ? "headless" : "headed") + " mode");
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize browser", e);
            throw new RuntimeException("Browser initialization failed", e);
        }
    }

    @Test
    public void updateNaukriTest() {
        try {
            logger.info("Starting Naukri profile update test");
            wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON)).click();

            wait.until(ExpectedConditions.presenceOfElementLocated(EMAIL_INPUT)).sendKeys(EMAIL);
            driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
            driver.findElement(SUBMIT_BUTTON).click();
            
            WebElement viewProfile = wait.until(ExpectedConditions.elementToBeClickable(VIEW_PROFILE_LINK));
            viewProfile.click();

            WebElement editProfile = wait.until(ExpectedConditions.elementToBeClickable(PROFILE_EDIT_BUTTON));
            editProfile.click();

            saveBasicDetails();
            logout();
            logger.info("Test completed successfully");
        } catch (Exception e) {
            captureDebugArtifacts();
            throw e;
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void captureDebugArtifacts() {
        try {
            Path screenshotsDir = Path.of(System.getProperty("user.dir"), "screenshots");
            Files.createDirectories(screenshotsDir);
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot.toPath(), screenshotsDir.resolve("ci_debug.png"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {}
    }

    private boolean isCiRun() {
        return "true".equalsIgnoreCase(System.getenv("CI"));
    }

    private void saveBasicDetails() {
        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(SAVE_BUTTON));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(BASIC_DETAILS_MODAL));
    }

    private void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_MENU)).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();
    }

    private void validateTestPrerequisites() {
        if (EMAIL == null || PASSWORD == null) {
            throw new SkipException("Missing environment variables.");
        }
    }
}
