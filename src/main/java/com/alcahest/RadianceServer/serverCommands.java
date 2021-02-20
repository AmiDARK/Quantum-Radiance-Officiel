package com.alcahest.RadianceServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class serverCommands {

	private static String loadedAndroidHTML = "qrDatas/supported_devices.csv";
	private static String AndroidDevicesURL = "https://storage.googleapis.com/play_public/supported_devices.csv";

	
	public static void AddDevicesAuto() {
		// List<String> deviceList = adbSupport.execute( "devices" );
	}
	
	public static void loadAndroidDevicesList() {
		try {
			System.out.println( "Try to downloaded updated Android devices list from the page : " );
			System.out.println( AndroidDevicesURL );
			FileUtils.copyURLToFile( new URL( AndroidDevicesURL ), new File( loadedAndroidHTML ) );
			
		} catch (MalformedURLException e) {
			System.out.println( "The requested URL <<" + AndroidDevicesURL + ">> is malformed" );
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println( "IO Exception occured when tried to download <<" + AndroidDevicesURL + ">>" );
			e.printStackTrace();
		}
	}

	/**
	 * This method return an arraylist of string containing the UDID list of all connected devices
	 * @return List<String>DevicesUDIDsList
	 */
	public static List<String> scanDevicesFromAdbScan() {
		List<String> allUDID = new ArrayList<String>();
		String newDevice = null;
		try {
			List<String> Devices = cmd( "adb device" );
			if ( Devices.size() > 1 ) {
				for( int dLoop = 0; dLoop < Devices.size(); dLoop++ ) {
					String input = Devices.get( dLoop );
					// If the line to scan contains "device", this mean we have an UDID here
					if ( input.contains( "device") == true ) {
                        newDevice = input.substring( 0, input.indexOf( " " ) );
						allUDID.add( newDevice );
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allUDID;
		
	}
	
	public static String getDeviceModelFromUDID( String deviceUDID ) {
        String DeviceUDID = null;
		List<String> DeviceUDIDList;
		try {
			DeviceUDIDList = cmd( "adb -s " + deviceUDID + " shell getprop ro.product.model" );
			DeviceUDID = DeviceUDIDList.get( 0 );
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return DeviceUDID;
	}
	
	public static devicesDatas FindDeviceFromModel( String modelInput ) {
		devicesDatas newDeviceDatas = new devicesDatas();
		try {
			List<String> Device = cmd( "find \"" + modelInput + "\" supported_devices.csv" );
			if ( Device.size() > 1 ) {
				String Line = Device.get( 2 );
				if ( Line.length() > 20 ) {
					int idPos1 = Line.indexOf( ",", 0);
					int idPos2 = Line.indexOf( ",", ( idPos1 + 1 ) );
					int idPos3 = Line.indexOf( ",", ( idPos2 + 1 ) );
					if ( idPos1 > 0 && idPos2 > idPos1 && idPos3 > idPos2 ) {
                        newDeviceDatas.MarketingName = Line.substring( 0, idPos1 );
                        newDeviceDatas.MarketingName = Line.substring( idPos1 + 1, idPos2 );
                        newDeviceDatas.Name = Line.substring( ( idPos2 + 1 ), idPos3 );
                        newDeviceDatas.Model = modelInput;
					}else {
                        newDeviceDatas.MarketingName = "Not found in the Android supported_devices list";
                        newDeviceDatas.MarketingName = "Not found in the Android supported_devices list";
                        newDeviceDatas.Name = "Not found in the Android supported_devices list";
                        newDeviceDatas.Model = modelInput;

					}
				}
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newDeviceDatas;
	}

	  public static List<String> cmd( String FullCommandLine ) throws IOException, InterruptedException{
		  	final String[] ADB_COMMAND_LINE = { "cmd.exe", "/c", FullCommandLine };
		    ProcessBuilder pb = new ProcessBuilder( ADB_COMMAND_LINE );
		    pb.redirectErrorStream(true);
		    Process p = pb.start();
		    p.waitFor();
		    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    String _temp = null;
		    List<String> lines = new ArrayList<String>();
		    while ((_temp = in.readLine()) != null) { lines.add(_temp); } // Récupère toutes les lignes d'information
		    // System.out.println("Command line returned : " + lines);
		    return lines;   	
		   }

			
			
			
			
			

	
}
