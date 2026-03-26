package updateprofile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;
import org.testng.annotations.Test; 

public class updateprofile {

	private static final Duration TIMEOUT = Duration.ofSeconds(30);
	private static final By LOGIN_BUTTON = By.xpath("//a[@id='login_Layer']");
	private static final By EMAIL_INPUT = By.xpath("//input[@placeholder='Enter your active Email ID / Username']");
	private static final By PASSWORD_INPUT = By.xpath("//input[@type='password']");
	private static final By SUBMIT_BUTTON = By.xpath("//button[@type='submit']");
	private static final By VIEW_PROFILE_LINK = By.xpath("//a[contains(text(),'View') and contains(@href,'/mnjuser/profile')]");
	private static final By PROFILE_ICON_LINK = By.xpath("//a[contains(@href,'/mnjuser/profile')]");
	private static final By PROFILE_EDIT_BUTTON = By.xpath("//em[contains(text(),'editOneTheme')]");
	private static final By NAME_TXTBOX = By.xpath("//input[@id='name']");
	private static final By SAVE_BUTTON = By.xpath("//button[@id='saveBasicDetailsBtn']");
	private static final By SUCCESS_POPUP_CLOSE = By.xpath("//span[@class='success-text' and text()='Profile updated successfully']/ancestor::div[contains(@class,'profileUpdatedProLayer')]//div[@class='crossLayer']");
	private static final By LOGOUT_MENU = By.xpath("//div[@class='nI-gNb-drawer']");
	private static final By LOGOUT_ICON = By.xpath("//div[@class='nI-gNb-drawer__icon']");
	private static final By LOGOUT_LINK = By.xpath("//a[@title='Logout']");

	public WebDriver driver;
	private String email = getCredential("NAUKRI_EMAIL", "sanketsbhosale2016@gmail.com");
	private String pass = getCredential("NAUKRI_PASSWORD", "Sankeypy@532");

	@Test
	public void updateNaukriTest() throws Exception {
		WebDriverWait wait = null;
		if (shouldSkipLiveTest()) {
			throw new SkipException("Skipping live Naukri automation in GitHub Actions. OTP cannot be completed reliably in hosted CI.");
		}
		try {
			WebDriverManager.chromedriver().setup();
			ChromeOptions options = new ChromeOptions();

			options.addArguments("--disable-blink-features=AutomationControlled");
			options.addArguments("start-maximized");
			options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
			options.addArguments("--remote-allow-origins=*");
			
			if (isCiRun()) {
				options.addArguments("--headless=new");
				options.addArguments("--disable-dev-shm-usage");
				options.addArguments("--window-size=1920,1080");
			}
 
			driver = new ChromeDriver(options);
			driver.get("https://www.naukri.com/");
			driver.manage().window().maximize();
			driver.manage().timeouts().implicitlyWait(Duration.ZERO);

			wait = new WebDriverWait(driver, TIMEOUT);

			wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON)).click();

			// credentials
			wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(email);
			driver.findElement(PASSWORD_INPUT).sendKeys(pass);

			// login
			driver.findElement(SUBMIT_BUTTON).click();

			openProfile(wait);

			WebElement EditProfile = wait.until(
					ExpectedConditions.elementToBeClickable(PROFILE_EDIT_BUTTON));
			EditProfile.click();

			wait.until(ExpectedConditions.visibilityOfElementLocated(NAME_TXTBOX)).sendKeys("e");

			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollBy(0,1000)");

			WebElement targetDiv = wait.until(
					ExpectedConditions.elementToBeClickable(SAVE_BUTTON));
			targetDiv.click();

			wait.until(ExpectedConditions.elementToBeClickable(SUCCESS_POPUP_CLOSE)).click();

			clickFirstVisible(wait, LOGOUT_MENU, LOGOUT_ICON);
			wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();

		} catch (Exception e) {
			captureDebugArtifacts();
			throw e;
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
	}

	private void openProfile(WebDriverWait wait) {
		FluentWait<WebDriver> loginWait = new FluentWait<>(driver)
				.withTimeout(TIMEOUT)
				.pollingEvery(Duration.ofMillis(500))
				.ignoring(Exception.class);

		Boolean profileReady = loginWait.until(d -> isVisible(VIEW_PROFILE_LINK) || isVisible(PROFILE_ICON_LINK));

		if (Boolean.TRUE.equals(profileReady)) {
			clickFirstVisible(wait, VIEW_PROFILE_LINK, PROFILE_ICON_LINK);
			return;
		}

		driver.navigate().to("https://www.naukri.com/mnjuser/profile");
		wait.until(ExpectedConditions.or(
				ExpectedConditions.elementToBeClickable(PROFILE_EDIT_BUTTON),
				ExpectedConditions.urlContains("/mnjuser/profile")));
	}

	private void clickFirstVisible(WebDriverWait wait, By... locators) {
		for (By locator : locators) {
			if (isVisible(locator)) {
				wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
				return;
			}
		}
		throw new IllegalStateException("None of the locators became visible: " + Arrays.toString(locators));
	}

	private boolean isVisible(By locator) {
		return !driver.findElements(locator).isEmpty() && driver.findElement(locator).isDisplayed();
	}

	private void captureDebugArtifacts() {
		if (driver == null) {
			return;
		}
		try {
			File screenshotsDir = new File(System.getProperty("user.dir"), "screenshots");
			if (!screenshotsDir.exists()) {
				screenshotsDir.mkdirs();
			}

			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			Files.copy(screenshot.toPath(), new File(screenshotsDir, "ci_debug.png").toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			Files.writeString(new File(screenshotsDir, "page_source.html").toPath(), driver.getPageSource());
		} catch (Exception ignored) {
		}
	}

	private boolean isCiRun() {
		return "true".equalsIgnoreCase(System.getenv("GITHUB_ACTIONS"))
				|| "true".equalsIgnoreCase(System.getenv("CI"));
	}

	private boolean shouldSkipLiveTest() {
		return isCiRun() && !"true".equalsIgnoreCase(System.getenv("RUN_NAUKRI_LIVE_TEST"));
	}

	private static String getCredential(String envKey, String fallback) {
		String value = System.getenv(envKey);
		return value == null || value.isBlank() ? fallback : value;
	}
}
