package com.alcahest.QRadianceClient;


import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Unit test for simple App.
 */
public class radClient{
	public static radDriver myDriver = null;
	public static RemoteWebDriver driver = null;
	

	static String message;
	public static void main( String args[] ) {
	
		DesiredCapabilities aCaps = new DesiredCapabilities();
//		aCaps.setCapability( "userKey", "9F7458650340F0A0DB0C758" ); // Expired account
		aCaps.setCapability( "userKey", "F8546934ED89AB89ED09435" ); // Valid account
		aCaps.setCapability( "udid", "UpdateDeviceUDIDHere" );
		aCaps.setCapability( "platformName", "Android" );
		aCaps.setCapability( "deviceName", "Huawei P8 Lite 2017" );
		aCaps.setCapability( "automationName", "UiAutomator2" );
		aCaps.setCapability( "app", "test.apk" );
		// aCaps.setCapability( "appPackage", "calculator.innovit.com.calculatrice" );
		try {
			myDriver = new radDriver( new URL( "http://127.0.0.1:17000" ), aCaps );
			if ( ( driver = myDriver.getDriver() ) != null ) {
				System.out.println( "RemoteWebDriver successfully started" );
			}else {
				System.exit( 0 );
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.findElement( By.xpath( "//*[@text='4']" ) ).click();
		driver.findElement( By.xpath( "//*[@text='7']" ) ).click();
		driver.findElement( By.xpath( "//*[@text='+']" ) ).click();
		driver.findElement( By.xpath( "//*[@text='2']" ) ).click();
		driver.findElement( By.xpath( "//*[@text='3']" ) ).click();
		driver.findElement( By.xpath( "//*[@text='=']" ) ).click();
		// Add a pause
		try {
			Thread.sleep( 4000 );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myDriver.close();
	}
}
