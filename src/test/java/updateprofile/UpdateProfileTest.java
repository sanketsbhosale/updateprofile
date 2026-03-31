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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateProfileTest {

    private static final Logger logger = Logger.getLogger(UpdateProfileTest.class.getName());
    private static final String EMAIL = System.getenv("NAUKRI_EMAIL");
    private static final String PASSWORD = System.getenv("NAUKRI_PASSWORD");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeMethod
    public void setUp() {
        try {
            if (isBlank(EMAIL) || isBlank(PASSWORD)) {
                throw new RuntimeException("NAUKRI_EMAIL and NAUKRI_PASSWORD environment variables must be set");
            }

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
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize browser", e);
            throw new RuntimeException("Browser initialization failed", e);
        }
    }

    @Test
    public void updateNaukriTest() {
        try {
            logger.info("Starting Naukri profile update test");
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@id='login_Layer']"))).click();

            wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//input[@placeholder='Enter your active Email ID / Username']")))
                    .sendKeys(EMAIL);
            driver.findElement(By.xpath("//input[@type='password']")).sendKeys(PASSWORD);

            driver.findElement(By.xpath("//button[@type='submit']")).click();
            logger.info("Login credentials submitted");

            WebElement viewProfile = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'View') and @href='/mnjuser/profile']")));
            viewProfile.click();
            logger.info("Navigated to profile page");

            WebElement editProfile = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//em[contains(text(),'editOneTheme')]")));
            editProfile.click();
            logger.info("Clicked edit profile button");

            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollBy(0,1000)");

            WebElement saveButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='saveBasicDetailsBtn']")));
            saveButton.click();
            logger.info("Clicked save button");

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//span[@class='success-text' and text()='Profile updated successfully']/ancestor::div[contains(@class,'profileUpdatedProLayer')]//div[@class='crossLayer']")))
                    .click();

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='nI-gNb-drawer']"))).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@title='Logout']"))).click();
            logger.info("Test completed successfully - logged out");
        } catch (Exception e) {
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
