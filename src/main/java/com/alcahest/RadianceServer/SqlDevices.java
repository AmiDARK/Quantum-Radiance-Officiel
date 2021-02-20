package com.alcahest.RadianceServer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.alcahest.RadianceServer.errorHandler.errorType;

public class SqlDevices {

	public static SqlDevicesDetails lastFoundDevice = null;
	/**
	 * ***********************************************************************************************************************
	 * /** This sub-class defined the data required to handle devices in a device
	 * list
	 */
	public static class SqlDevicesDetails {
		String deviceName;
		String deviceOS;
		String deviceOSVersion;
		String deviceUDID;
		String deviceManufacturer;
		String deviceModel;
		int deviceState; // ( -1 = not connected, 0 = available, 1 = busy )
		Date expirationDate; // Expiration date for current busy state
		String reservedToUser; // userKey of the user that did reserve the device
		String onServerName; // Name of the server on which the device is connected

		// ********************************************************************************************************************
		/**
		 * This is the constructor for an deviceDetails object to handle 1 device
		 * 
		 * @param deviceName         = The subjective name given to the device (ex:
		 *                           Huawei P8 Lite 2017, Samsung Galaxy S7, etc.)
		 * @param deviceOS           = Name of the OS installed in the device
		 * @param deviceOSVersion    = Version of the OS installed in the device
		 * @param deviceUDID         = The device Unique Device IDentifier
		 * @param deviceManufacturer = The device manufacturer (ex. Huawei, Samsung,
		 *                           Apple, etc.)
		 * @param deviceModel        = The device model (ex : PRA-LX1, etc. )
		 * @param deviceState        = the current state of the device
		 * @param expirationDate     = In case the device is reserved or used, the
		 *                           expiration date of the reservation. Otherwise =
		 *                           NULL
		 * @param onServerName       = the name of the server on which the device is
		 *                           connected (ex: local, WindowsServer1, MacServer,
		 *                           etc.)
		 */
		SqlDevicesDetails(String deviceName, String deviceOS, String deviceOSVersion, String deviceUDID,
				String deviceManufacturer, String deviceModel, int deviceState, Date expirationDate,
				String reservedToUser, String onServerName) {
			this.deviceName = deviceName;
			this.deviceOS = deviceOS;
			this.deviceOSVersion = deviceOSVersion;
			this.deviceUDID = deviceUDID;
			this.deviceManufacturer = deviceManufacturer;
			this.deviceModel = deviceModel;
			if (deviceState != 256) {
				this.deviceState = deviceState;
			} else {
				this.deviceState = -1;
			}
			this.expirationDate = expirationDate;
			this.reservedToUser = onServerName;
		}
	}

	// ********************************************************************************************************************
	@SuppressWarnings("unused") // lastErrorMessage is used but Eclipse does not identify occurencies
	private static String lastSQLDeviceErrorMessage = null;

	// ************************************************************************************************************************
	/**
	 * This method will return the current device state of a specific device
	 * 
	 * @param deviceToCheck
	 * 
	 * @return deviceState
	 */
	public static int getSQLDeviceState(SqlDevicesDetails deviceToCheck) {
		if (deviceToCheck.deviceState < 1) {
			boolean isConnected = adbSupport.isDeviceConnected(deviceToCheck.deviceUDID);
			if (isConnected == true) {
				deviceToCheck.deviceState = 0;
			} else {
				deviceToCheck.deviceState = -1;
			}
			updateSQLDeviceState(deviceToCheck, deviceToCheck.deviceState);
		}
		return deviceToCheck.deviceState;
	}

	// ***********************************************************************************************************************
	/**
	 * /** This method will update a device state directly in the SQL Database
	 * 
	 * @param deviceToCheck
	 * @param currentSQLDeviceState
	 */
	public static void updateSQLDeviceState(SqlDevicesDetails deviceToCheck, int currentSQLDeviceState) {
		if (isSQLDeviceExists(deviceToCheck.deviceUDID) == true) {
			try {
				QRadServer.mySqlConnection
						.set("UPDATE devices SET deviceStatus='" + String.valueOf(currentSQLDeviceState)
								+ "' WHERE deviceUDID='" + deviceToCheck.deviceUDID + "'");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ***********************************************************************************************************************
	/**
	 * /** This method will return true if a device exists
	 * 
	 * @param deviceUDID
	 * 
	 * @return true if the devices exist in the SQL Database
	 */
	public static boolean isSQLDeviceExists(String deviceUDID) {
		return ( findSQLDevice(null, null, null, deviceUDID, null, null, false) != null );
	}

	// ***********************************************************************************************************************
	/**
	 * This method will create a readable SqlDeviceDetails object using a ResultSet from a SQL get Query
	 * @param thisDevice is the ResultSet of a SQL query to read a registered device content
	 * @return currentDevice is the SqlDeviceDetails object representing the device from the ResultSet datas
	 */
	public static SqlDevicesDetails createSQLDeviceData(ResultSet thisDevice) {
		try {
			SqlDevicesDetails currentDevice = new SqlDevicesDetails(thisDevice.getNString("deviceName"),
					thisDevice.getNString("deviceOS"), thisDevice.getNString("deviceOSVersion"),
					thisDevice.getNString("deviceUDID"), thisDevice.getNString("deviceManufacturer"),
					thisDevice.getNString("deviceModel"), thisDevice.getInt("deviceStatus"),
					thisDevice.getDate( "expirationDate"), thisDevice.getNString("reservedByUserID"),
					thisDevice.getNString("onServerName"));
			return currentDevice;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	// ************************************************************************************************************************
	/**
	 * This method will return the device index of the available device found from
	 * requested informations If this method return a negative value, the value will
	 * represent the errorID : errorHandler.getErrorName( ID ) you can then also
	 * check the lastErrorMessage to get information about failure
	 * 
	 * @param deviceName
	 * @param deviceOS
	 * @param deviceOSVersion
	 * @param deviceUDID
	 * @param deviceManufacturer
	 * @param deviceModel
	 * 
	 * @return if positive, the index number of the found device in the
	 *         DevicesDetails list if negative, the errorID
	 */
	public static SqlDevicesDetails findSQLDevice(String deviceName, String deviceOS, String deviceOSVersion,
			String deviceUDID, String deviceManufacturer, String deviceModel, boolean checkForAvailability) {
		SqlDevicesDetails currentDevice = null;
		int searchCount = 0;
		String searchMethod = null;
		// *****************************************************************************************
		// Main loop to look for the devices depending on provided data and using datas priorities.
		while( currentDevice == null && searchCount < 5 ) {
			switch( searchCount ){
				// ******************************************************************** Search priority (#0) : unique deviceUDID
				case 0:
					searchMethod = "SELECT * FROM devices WHERE deviceUDID='" + deviceUDID + "'";
					break;
				// ******************************************************************** Search priority (#1) : deviceName	
				case 1:
					searchMethod = "SELECT * FROM devices WHERE deviceName='" + deviceName + "'";
					break;
				// ******************************************************************** Search priority (#2) : deviceModel (Optional deviceOSVersion)	
				case 2:
					if ( deviceOSVersion == null ) {
						searchMethod = "SELECT * FROM devices WHERE deviceModel='" + deviceName+ "'";					
					}else {
						searchMethod = "SELECT * FROM devices WHERE deviceModel='" + deviceName
								+ "' AND deviceOSVersion='" + deviceOSVersion + "'";
					}
					break;
				// ******************************************************************** Search priority (#3) : deviceManufacturer (optional : deviceOSVersion)
				case 3:
					if ( deviceOSVersion == null ) {
						searchMethod = "SELECT * FROM devices WHERE deviceManufacturer='" + deviceManufacturer + "'";
					}else {
						searchMethod = "SELECT * FROM devices WHERE deviceManufacturer='" + deviceManufacturer
								+ "' AND deviceOSVersion='" + deviceOSVersion + "'";
					}
					break;
				// ******************************************************************** Search Priority (#4) : deviceOS (optional : deviceOSVersion)
				case 4:
					searchMethod = "SELECT * FROM devices WHERE deviceOS='" + deviceOS + "'";
					break;
				// ******************************************************************** Search Priority (#X) : All other cases .. Nothing to do.
				default:
					searchMethod = null;
					break;
			}
			// Search the device in the DataBase using current searchMethod
			try {
				if( searchMethod != null ) {
					ResultSet thisDevice = QRadServer.mySqlConnection.get( searchMethod );
					if (QRadServer.mySqlConnection.getSqlResultSize() > 0) {
						thisDevice.next();
						currentDevice = createSQLDeviceData(thisDevice);
					}
				}else {
				}
			} catch (Exception e) {
				e.printStackTrace();
				lastSQLDeviceErrorMessage = errorHandler.getErrorName( errorType.ExceptionError );
				return null;
			}
			searchCount++; // Increment search Counter to use next search method if no device was found.
		}
		// *****************************************************************************************
		// If a device was found, we check its availability before continuing.
		if( currentDevice != null ) {
			lastFoundDevice = currentDevice; // Update lastFoundDevice memory.
			if (checkForAvailability == true) {
				switch (getSQLDeviceState(currentDevice)) {
				case -1:
					lastSQLDeviceErrorMessage = errorHandler.getErrorName(errorType.deviceIsOffline);
					return null; // Device is currently OFFLINE
				case 0:
					return currentDevice; // Device is available
				case 1:
					lastSQLDeviceErrorMessage = errorHandler.getErrorName(errorType.deviceNotAvailable);
					return null; // Device is currently used/unavailable
				}
			}else {
				return currentDevice; // No availability checking, return device DATAS
			}
		}		
		lastSQLDeviceErrorMessage = errorHandler.getErrorName(errorType.noDeviceFound);
		return null;
	}

	// ***********************************************************************************************************************
	/**
	 * This method will output a chosen user in the console.
	 * @param userToOutput
	 * @param userID
	 */
	public static void outputSQLDevice( SqlDevicesDetails deviceToOutput ) {
		if( deviceToOutput != null ) {
			deviceToOutput.deviceState = getSQLDeviceState( deviceToOutput );
			switch( deviceToOutput.deviceState ) {
				case -1 : System.out.println( "  deviceState = Offline/Not Connected" ); break;
				case  0 : System.out.println( "  deviceState = Available" ); break;
				case  1 : System.out.println( "  deviceState = used/busy" ); break;
				default : System.out.println( "  deviceState = Unavailable" ); break;
			}
			System.out.println( "  deviceName = '" + deviceToOutput.deviceName + "'"  );
			System.out.println( "  deviceOS = '" + deviceToOutput.deviceOS + "'" );
			System.out.println( "  deviceOSVersion = '" + deviceToOutput.deviceOSVersion + "'"  );
			System.out.println( "  deviceUDID = '****************'" );
			System.out.println( "  deviceManufacturer = '" + deviceToOutput.deviceManufacturer + "'"  );
			System.out.println( "  deviceModel = '" + deviceToOutput.deviceModel + "'" );
		}
	}

	// ************************************************************************************************************************
	/**
	 * This method will try to reserve a device if exists and is available.
	 * if the method return false, you can check the lastErrorMessage to get information about failure
	 * 
	 * @param deviceToReserve
	 * @param userKey
	 * 
	 * @return true if device was correctly reserved
	 */
	public static boolean reserveSQLDevice( SqlDevicesDetails deviceToReserve, String userKey ) {
		String updateToPush = null;
		switch( getSQLDeviceState( deviceToReserve ) ) {
			// Device is OFFLINE, cannot be reserved
			case -1 :
				lastSQLDeviceErrorMessage = errorHandler.getErrorName( errorType.deviceIsOffline ); 
				updateToPush = "UPDATE devices SET deviceStatus='" + deviceToReserve.deviceState + "'"
						+ ", reservedByUserID=''"
			//			+ ", expirationDate='" + deviceToReserve.expirationDate + "'"
						+ " WHERE deviceUDID='" + deviceToReserve.deviceUDID + "'";
				updateSQLDevice( updateToPush );
				return false;
			// Device is AVAILABLE : Makes reservation during 30 minutes
			case 0 :
				deviceToReserve.expirationDate = Utilities.dateSupport.getShiftedDate( 30 );
				deviceToReserve.reservedToUser = userKey;
				deviceToReserve.deviceState = 1;
				updateToPush = "UPDATE devices SET deviceStatus='" + deviceToReserve.deviceState + "'"
										+ ", reservedByUserID='" + userKey + "'"
										+ ", expirationDate='" + Utilities.dateSupport.getStringFromDate( deviceToReserve.expirationDate ) + "'"
										+ " WHERE deviceUDID='" + deviceToReserve.deviceUDID + "'";
				updateSQLDevice( updateToPush );
				return true;
			// Device is USED : Check if it's the current user that did already reserve it
			case 1 :
				// If the device is already reserved to the user that request it.
				if ( deviceToReserve.reservedToUser.equals( userKey ) ) {
					deviceToReserve.expirationDate = Utilities.dateSupport.getShiftedDate( 30 );
					updateToPush = "UPDATE devices SET deviceStatus='" + deviceToReserve.deviceState + "'"
							+ ", reservedByUserID='" + userKey + "'"
							+ ", expirationDate='" + Utilities.dateSupport.getStringFromDate( deviceToReserve.expirationDate ) + "'"
							+ " WHERE deviceUDID='" + deviceToReserve.deviceUDID + "'";
					updateSQLDevice( updateToPush );
					return true;
				// If the device was reserved by another user, we cannot reserve it until the other user release it.
				}else {
					lastSQLDeviceErrorMessage = errorHandler.getErrorName( errorType.deviceIsReserved );
					return false;
				}
			default:
				return false;
		}
	}
	// ***********************************************************************************************************************
	/**
	 * 
	 * @param deviceToRelease
	 * @param userKey
	 * @return
	 */
	public static boolean releaseSQLDevice( SqlDevicesDetails deviceToRelease, String userKey ) {
		String updateToPush = null;
		switch( getSQLDeviceState( deviceToRelease ) ) {
			case 2:
				deviceToRelease.deviceState = 0;
				updateToPush = "UPDATE devices SET deviceStatus='" + deviceToRelease.deviceState + "'"
						+ ", reservedByUserID=''"
						+ ", expirationDate='" + Utilities.dateSupport.getStringFromDate( new Date() ) + "'"
						+ " WHERE deviceUDID='" + deviceToRelease.deviceUDID + "'";
				updateSQLDevice( updateToPush );
				return true;
			default:
				lastSQLDeviceErrorMessage = errorHandler.getErrorName( errorType.deviceDidNotRanTests );
				return false;
		}
	}
	
	// ***********************************************************************************************************************
	/**
	 * This method will use current mySqlConnection to update device data in the DataBase
	 * 
	 * @param updateToPush
	 * @return
	 */
	public static boolean updateSQLDevice( String updateToPush ) {
		try {
			 QRadServer.mySqlConnection.set( updateToPush );
			 return true;
		}catch( Exception e ) {
			e.printStackTrace();
			return false;
		}

	}
	
	// ***********************************************************************************************************************
	/**
	 *  Thie method will return the latest errorMessage obtaines
	 * @return
	 */
	public static String getLastSQLErrorMessage() {
		return lastSQLDeviceErrorMessage;
	}
	
	// ***********************************************************************************************************************
	/**
	 * This method will update a device to put it in "running test" state 
	 * @param deviceToReserve
	 * @param userKey
	 * @return
	 */
	public static boolean setSQLDeviceAsRunningTests( SqlDevicesDetails deviceToReserve, String userKey ) {
		String updateToPush = null;
		switch( getSQLDeviceState( deviceToReserve ) ) {
			// Device is OFFLINE, cannot be reserved
			case -1 :
				lastSQLDeviceErrorMessage = errorHandler.getErrorName( errorType.deviceIsOffline ); 
				return false;
			// Device is AVAILABLE : Makes reservation during 30 minutes
			case 0 :
				deviceToReserve.expirationDate = Utilities.dateSupport.getShiftedDate( 30 );
				deviceToReserve.reservedToUser = userKey;
				deviceToReserve.deviceState = 2;
				updateToPush = "UPDATE devices SET deviceStatus='" + deviceToReserve.deviceState + "'"
						+ ", reservedByUserID='" + userKey + "'"
						+ ", expirationDate='" + Utilities.dateSupport.getStringFromDate( deviceToReserve.expirationDate ) + "'"
						+ " WHERE deviceUDID='" + deviceToReserve.deviceUDID + "'";
				updateSQLDevice( updateToPush );
				return true;
			// Device is USED : Check if it's the current user that did already reserve it
			case 1 :
				// If the device is already reserved to the user that request it.
				if ( deviceToReserve.reservedToUser.equals( userKey ) ) {
					deviceToReserve.expirationDate = Utilities.dateSupport.getShiftedDate( 30 );
					deviceToReserve.deviceState = 2;
					updateToPush = "UPDATE devices SET deviceStatus='" + deviceToReserve.deviceState + "'"
							+ ", reservedByUserID='" + userKey + "'"
							+ ", expirationDate='" + Utilities.dateSupport.getStringFromDate( deviceToReserve.expirationDate ) + "'"
							+ " WHERE deviceUDID='" + deviceToReserve.deviceUDID + "'";
					updateSQLDevice( updateToPush );
					return true;
					// If the device was reserved by another user, we cannot reserve it until the other user release it.
				}else {
					lastSQLDeviceErrorMessage = errorHandler.getErrorName( errorType.deviceIsReserved );
					return false;
				}
			default:
				return false;
		}
	}

	
	/** ***********************************************************************************************************************
	/**
	 * This method will output all the devices in the console
	 */
	public static void listSQLDevices() {
		try {
			ResultSet devicesList = QRadServer.mySqlConnection.get("SELECT * FROM devices");
			int devicesCount = QRadServer.mySqlConnection.getSqlResultSize();
			if ( devicesCount > 0 ) {
				System.out.println("There is " + devicesCount + " devices registered in the dataBase ");
				while ( devicesList.next()) {
					System.out.println( "***********************************************************");
					System.out.println( "Device # " + devicesList.getRow() + " : " );
					System.out.println( "deviceName = " + devicesList.getNString( "deviceName" ) );
					System.out.println( "deviceOS = " + devicesList.getNString( "deviceOS" ) );
					System.out.println( "deviceOSVersion = " + devicesList.getNString( "deviceOSVersion" ) );
					System.out.println( "deviceUDID = " + devicesList.getNString( "deviceUDID" ) );
					System.out.println( "deviceManufacturer = " + devicesList.getNString( "deviceManufacturer" ) );
					System.out.println( "deviceModel = " + devicesList.getNString( "deviceModel" ) );
					System.out.println( "deviceStatus = " + devicesList.getInt( "deviceStatus" ) );
					System.out.println( "expirationDate = " + devicesList.getDate( "expirationDate" ).toString() );
					System.out.println( "reservedByUserID = " + devicesList.getNString( "reservedByUserID" ) );
				}
			}else {
				System.out.println( "The Sql DataBase contains no registered devices(s)." );
			}
			QRadServer.mySqlConnection.release(); // Release used statement
		} catch (Exception e) {
			System.out.println( "Exception reached when trying to read devices Sql database datas." );
			e.printStackTrace();
			QRadServer.mySqlConnection.release(); // Release used statement
		}

	
	}


}
