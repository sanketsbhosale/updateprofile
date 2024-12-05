package updateprofile;

import java.time.Duration;

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
	private String pass = "Sankeypy@789";

	@Test
	public void updateNaukriTest() throws Exception {
		try {
			// System.setProperty("webdriver.chrome.driver", "driver/chromedriver.exe");
//			EdgeOptions options2 = new EdgeOptions();
			ChromeOptions options = new ChromeOptions();

			String chromeBinaryPath = System.getenv("CHROME_BINARY_PATH");
			if (chromeBinaryPath != null) {
			    options.setBinary(chromeBinaryPath);
			}
			
			options.addArguments("--no-sandbox");
			options.addArguments("--ignore-ssl-errors=yes");
			options.addArguments("--ignore-certificate-errors");
			options.addArguments("--disable-dev-shm-usage");
			options.addArguments("--headless=new"); 
 
			driver = new ChromeDriver(options);
//			driver = new EdgeDriver(options);
			driver.get("https://www.naukri.com/");
			driver.manage().window().maximize();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
			Thread.sleep(2000);

			driver.findElement(By.xpath("//a[@id='login_Layer']")).click();// input[@class='err-border']

			// credentials
			driver.findElement(By.xpath("//input[@placeholder='Enter your active Email ID / Username']"))
					.sendKeys(email);
			driver.findElement(By.xpath("//input[@type='password']")).sendKeys(pass);

			// login
			driver.findElement(By.xpath("//button[@type='submit']")).click();
			Thread.sleep(2000);

			WebElement ViewProfile = driver
					.findElement(By.xpath("//a[contains(text(),'View') and @href='/mnjuser/profile']"));
			ViewProfile.click();
			Thread.sleep(2000);

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

			WebElement EditProfile = driver.findElement(By.xpath("//em[contains(text(),'editOneTheme')]"));
			wait.until(ExpectedConditions.elementToBeClickable(EditProfile));
			EditProfile.click();
			Thread.sleep(2000);

//			WebElement targetDiv = driver.findElement(By.xpath("//div[@class='col s12 action']"));
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollBy(0,1000)");

			WebElement targetDiv = driver.findElement(By.xpath("//button[@id='saveBasicDetailsBtn']"));
			wait.until(ExpectedConditions.elementToBeClickable(EditProfile));
			targetDiv.click();
			Thread.sleep(2000);
			
			driver.findElement(By.xpath("//div[@class='nI-gNb-drawer__icon']")).click();
			driver.findElement(By.xpath("//a[@title='Logout']")).click();
			
			Thread.sleep(2000);
			driver.close();
			driver.quit();

		} catch (Exception e) {
			e.printStackTrace();//
		}
	}
}
