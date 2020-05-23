package com.alcahest.RadianceServer;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.remote.DesiredCapabilities;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;


public class AppiumServer {
	QRadServerThread backup;
	
	AppiumServer( DesiredCapabilities appiumCaps, QRadServerThread backup ) {
		if( appiumCaps != null && backup != null ) {
			this.backup = backup;
			try {
				this.backup.AppiumIP = InetAddress.getLocalHost().getHostAddress();
				this.backup.usePort = this.nextFreePort();
				this.startAppiumServer( this.backup.AppiumIP, this.backup.usePort, appiumCaps );
			} catch (UnknownHostException e) {
				System.out.println( "radAppiumServer : Cannot get current server ip address : " + e.getLocalizedMessage() );
			}
		}
	}

	
	public void startAppiumServer( String useIPAddress, int usePort, DesiredCapabilities appCaps ) {
		// System.out.println( "Detected nodemodule path = " + nodeModule );
		String nodeJS = findNodeModulePath();
		this.backup.builder = new AppiumServiceBuilder().withAppiumJS( new File( nodeJS ) );
		this.backup.builder.withCapabilities( appCaps );                        // Use client capabilities
		this.backup.builder.usingPort( usePort );                               // Use pre-defined port
		this.backup.builder.withIPAddress( useIPAddress );                      // Set the appium IP address
		this.backup.builder.withArgument(GeneralServerFlag.SESSION_OVERRIDE);   // Force restart
		this.backup.builder.withArgument(GeneralServerFlag.LOG_LEVEL, "error"); // Log all errors
		// Start the server with the builder
		this.backup.serviceToStart = AppiumDriverLocalService.buildService( this.backup.builder );
		this.backup.serviceToStart.start();
		this.backup.usePort = usePort;
		this.backup.appServerStarted = true;
	}
	
	public void stopAppiumServer() {
		if ( backup != null ) {
			if ( this.backup.serviceToStart != null ) {
				this.backup.serviceToStart.stop();
				this.backup.serviceToStart = null;
				this.backup.builder = null;
				this.backup.appServerStarted = false;
			}
		}
	}
	
	public int nextFreePort() {
		int usePort = 4723; // 1st appium default port
		boolean freePortFound = false;
		do {
			try {
				ServerSocket cServer = new ServerSocket( usePort );
				cServer.close();
				freePortFound = true;
			}catch( Exception e ) {
				usePort++;
			}
		}while( freePortFound == false );
		return usePort;
	}

	public static String findNodeModulePath() {
		String feedback = null;
		if (SystemUtils.IS_OS_WINDOWS) {
			if (Utilities.getFileExists(System.getProperty("user.home") + "/node_modules/appium/build/lib/main.js") == true) {
				feedback = System.getProperty("user.home") + "/node_modules/appium/build/lib/main.js";
			} else if (Utilities.getFileExists(System.getProperty("user.home")
					+ "/AppData/Roaming/npm/node_modules/appium/build/lib/main.js") == true) {
				feedback = System.getProperty("user.home")
						+ "/AppData/Roaming/npm/node_modules/appium/build/lib/main.js";
			}
		} else {
			if (SystemUtils.IS_OS_MAC_OSX) {
				feedback = "/usr/local/lib/node_modules/appium/build/lib/main.js"; // MAC OS
			}
		}

		return feedback;
	}


}
