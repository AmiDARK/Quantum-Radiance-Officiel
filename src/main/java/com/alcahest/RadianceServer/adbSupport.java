package com.alcahest.RadianceServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// ****************************************************************************************************************************
/**
 * This class is dedicaced to the Android ADB tool support.
 * @author Alcahest
 */
public class adbSupport {

	// Wil return a line containing the UDID of the device if connected.
	private static String adbGetDeviceConnected = "adb devices -l"; // Will return at least one line containing the UDID in it
	

	// ************************************************************************************************************************
	/**
	 * This method will return TRUE if the device defined by its UDID is connected to the server.
	 * @param deviceUDID The device UDID
	 * 
	 * @return TRUE if device is connected, otherwise FALSE
	 */
	public static boolean isDeviceConnected( String deviceUDID ) {
		boolean isConnected = false;
		String getDetails = adbGetDeviceConnected;
		List<String> feedback = execute( getDetails );
		if( feedback != null ) {
			if ( feedback.size() > 0 ) {
				for( int dLoop = 0; dLoop < feedback.size(); dLoop++ ) {
					if ( feedback.get( dLoop ).contains( deviceUDID ) == true ) {
						isConnected = true;
						break;
					}
				}
			}
		}
		return isConnected;
	}

	
	// ************************************************************************************************************************
	/**
	 * This method will execute a command line (consider that it start with 'adb ' and contains parameter
	 * @param adbCommandToExecute
	 * 
	 * @return all the output get from the command line execution.
	 */
	public static List<String> execute(String adbCommandToExecute ){
		List<String> feedback = new ArrayList<String>();
		// System.out.println( ADB_COMMAND_LINE );
		try {
			Process process = Runtime.getRuntime().exec( adbCommandToExecute );
			// Get input streams
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			process.waitFor();
			String inText = null;
			// Read command errors
			while ( ( inText = stdError.readLine() ) != null ) {
				System.out.println( inText );
			}
			// Read command standard output
			while ( ( inText = stdInput.readLine() ) != null ) {
				feedback.add( inText );
			}
		} catch (Exception e) {
			System.out.println( "Adb command line returned an error : " + e.getLocalizedMessage() );
		}
		return feedback;
	}

	
	}
