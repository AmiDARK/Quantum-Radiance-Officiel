package com.alcahest.RadianceServer;

// ****************************************************************************************************************************
/**
 * This class handle the error system
 * @author Alcahest
 *
 */
public class errorHandler {

	// ************************************************************************************************************************
	/**
	 * This sub-class handle the error index that correspond to the main class method errorHandler.getErrorName( errorType )
	 * @author Alcahest
	 */
	public static class errorType{
		public final static int noDeviceFound       = -1;
		public final static int deviceNotAvailable  = -2;
		public final static int deviceIsOffline     = -3;
		public final static int deviceIsReserved    = -4;
		public final static int deviceIsNotReserved = -5;
		public final static int deviceDidNotRanTests = -6;
		public final static int ExceptionError      = -666;
		public final static int unknownError        =  0;
	}
	
	// ************************************************************************************************************************
	/**
	 * This method will return a string that represent the error description from the errorNumber
	 * @param errorNumber
	 * 
	 * @return ErrorDescription
	 */
	public static String getErrorName( int errorNumber ) {
		switch( errorNumber ) {
		case  0 : return "Unknown error number.";
		case -1 : return "No device was found using the requested informations.";
		case -2 : return "The requested device is currently not available.";
		case -3 : return "The requested device is currently Offline.";
		case -4 : return "The requested device is already reserved/used by another user.";
		case -5 : return "The requested device is already available.";
		case -6 : return "The device was not under the state of running tests.";
		case 666 : return "An exception occured. Please refer to the exception message on the Server side.";
		default : return "Unknown error number.";
		}
	}
	
}
