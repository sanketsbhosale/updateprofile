package updateprofile;

import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateProfileTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final Logger logger = Logger.getLogger(UpdateProfileTest.class.getName());
    private static final String EMAIL = "sanketsbhosale2016@gmail.com";
    private static final String PASSWORD = "Sankeypy@532";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    @BeforeMethod
    public void setUp() {
        try {
            // Validate environment variables
            if (EMAIL == null || EMAIL.isEmpty() || PASSWORD == null || PASSWORD.isEmpty()) {
                throw new RuntimeException("NAUKRI_EMAIL and NAUKRI_PASSWORD environment variables must be set");
            }
            
            // Automatically download and setup the correct ChromeDriver version
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();

            options.addArguments("--headless=new");   // ⭐ REQUIRED
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--remote-allow-origins=*");
            
            WebDriver driver = new ChromeDriver(options);
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
            // Login
            logger.info("Starting Naukri profile update test");
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@id='login_Layer']")))
                    .click();

            // Enter credentials
            wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//input[@placeholder='Enter your active Email ID / Username']")))
                    .sendKeys(EMAIL);
            driver.findElement(By.xpath("//input[@type='password']")).sendKeys(PASSWORD);

            // Submit login
            driver.findElement(By.xpath("//button[@type='submit']")).click();
            logger.info("Login credentials submitted");

            // Navigate to profile
            WebElement viewProfile = wait.until(ExpectedConditions
                    .elementToBeClickable(By.xpath("//a[contains(text(),'View') and @href='/mnjuser/profile']")));
            viewProfile.click();
            logger.info("Navigated to profile page");

            // Edit profile
            WebElement editProfile = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//em[contains(text(),'editOneTheme')]")));
            editProfile.click();
            logger.info("Clicked edit profile button");

            // Scroll down
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollBy(0,1000)");

            // Save basic details
            WebElement saveButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='saveBasicDetailsBtn']")));
            saveButton.click();
            logger.info("Clicked save button");

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[@class='success-text' and text()='Profile updated successfully']/ancestor::div[contains(@class,'profileUpdatedProLayer')]//div[@class='crossLayer']")))
                    .click();

            // Logout
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='nI-gNb-drawer']")))
                    .click();
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@title='Logout']"))).click();
            logger.info("Test completed successfully - logged out");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occurred during test execution", e);
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshot, new File("ci_debug.png"));
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
}
