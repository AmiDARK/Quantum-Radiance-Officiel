package com.alcahest.RadianceServer;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;

public class Utilities {

	public static class dateSupport{
	
		// ************************************************************************************************************************
		/**
		 * This method will return the current date in a JSON compatible format : yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
		 * @return theCurrentDate
		 */
		public static Date getShiftedDate( int minutesShift ) {
			Date currentDate = new Date();                       // 1. We get the current date
			Calendar finalDate = Calendar.getInstance();         // 2. Get a calendar to locale date
			finalDate.setTime( currentDate );                    // 3. The calendar will be dated to the currentDate
			finalDate.add( Calendar.MINUTE, minutesShift );      // 4. We add the Day shift to the calendar to reflect the wanted date (minutes shift)
			currentDate.setTime( finalDate.getTimeInMillis() );
			return currentDate;
			// DateFormat jDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" ); // 5. We setup the date output formatter
			// return jDateFormat.format( finalDate.getTime() ); // 6. We return the new date in a JSON Compatible formatted String 
		}

		// ************************************************************************************************************************
		/**
		 * This method will convert a DATE to a STRING.
		 * @param dateToOutput
		 * @return StringVersionOfTheDate
		 */
		public static String getStringFromDate( Date dateToOutput ) {
			DateFormat outputDate = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
			return outputDate.format( dateToOutput );
		}
	
		// ************************************************************************************************************************
		/**
		 * This method will convert a String date to a java date object.
		 * @param DateToOutput is a DATE data in STRING format
		 * @return Date data
		 */
		public static Date getDateFromString( String DateToOutput ) {
			try {
				Date feedbackDate = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).parse( DateToOutput );
				return feedbackDate;
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static boolean buildCapabilitiesFromJson( JSONObject source, DesiredCapabilities target ) {
		if ( source != null || target != null ) {
			JSONArray sourceNames = source.names();
			if ( sourceNames.length() > 0 ) {
				for( int sLoop = 0; sLoop < sourceNames.length(); sLoop++ ) {
					String capItem = sourceNames.getString( sLoop );
					String capValue = source.optString( capItem );
					if( capValue != null ) {
						if( !capItem.toLowerCase().equals("userkey" ) ){
							target.setCapability( capItem,  capValue );
						}
					}else {
						boolean capValueB = source.optBoolean( capItem );
						target.setCapability(capItem,  capValueB );
					}
				}
			}else {
				return false;
			}
		}else {
			return false;
		}	
		return true;
	}
	
	
	public static boolean getFileExists( String fileName ) {
		boolean exists = new File( fileName ).exists();
		return exists;
	}
	
	public static void waitDelay( int Duration ) {
		try {
			Thread.sleep( Duration );
		}catch( Exception e ) {
			// #To Do
		}
	}
	
}
