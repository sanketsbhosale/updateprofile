package updateprofile;

import java.time.Duration;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test; 

public class updateprofile {

	public WebDriver driver;
	private String email = "sanketsbhosale2016@gmail.com";
	private String pass = "Sankeypy@532";

	@Test
	public void updateNaukriTest() throws Exception {
		try {
			WebDriverManager.chromedriver().setup();
			ChromeOptions options = new ChromeOptions();

			options.addArguments("--disable-blink-features=AutomationControlled");
			options.addArguments("start-maximized");
			options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
			
//			options.addArguments("--no-sandbox");
//			options.addArguments("--ignore-ssl-errors=yes");
//			options.addArguments("--ignore-certificate-errors");
//			options.addArguments("--disable-dev-shm-usage");
//			options.addArguments("--headless=new");
 
			driver = new ChromeDriver(options);
			driver.get("https://www.naukri.com/");
			driver.manage().window().maximize();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@id='login_Layer']"))).click();

			// credentials
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//input[@placeholder='Enter your active Email ID / Username']"))).sendKeys(email);
			driver.findElement(By.xpath("//input[@type='password']")).sendKeys(pass);

			// login
			driver.findElement(By.xpath("//button[@type='submit']")).click();

			WebElement ViewProfile = wait.until(ExpectedConditions
					.elementToBeClickable(By.xpath("//a[contains(text(),'View') and @href='/mnjuser/profile']")));
			ViewProfile.click();

			WebElement EditProfile = wait.until(
					ExpectedConditions.elementToBeClickable(By.xpath("//em[contains(text(),'editOneTheme')]")));
			EditProfile.click();

			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollBy(0,1000)");

			WebElement targetDiv = wait.until(
					ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='saveBasicDetailsBtn']")));
			targetDiv.click();

			wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
					"//span[@class='success-text' and text()='Profile updated successfully']/ancestor::div[contains(@class,'profileUpdatedProLayer')]//div[@class='crossLayer']")))
					.click();

			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='nI-gNb-drawer']"))).click();
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@title='Logout']"))).click();

		} catch (Exception e) {
			throw e;
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
	}
}
