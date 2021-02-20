package com.alcahest.RadianceServer;

import java.sql.ResultSet;
import java.util.Date;

public class SqlUsers {

	// Here is the sub-class that will contain the user structure details read from
	// SQL database
	public static class userDetails {
		String userName;
		String userKey;
		String expirationDate;
		Boolean isConnected;

		// Constructeur de l'objet
		userDetails(String userName, String userKey, String expirationDate) {
			this.userName = userName;
			this.userKey = userKey;
			this.expirationDate = expirationDate;
			this.isConnected = false;
		}
	}

	/**
	 * This method will output all the users in the console
	 */
	public static void listSQLUsers() {
		try {
			ResultSet userLists = QRadServer.mySqlConnection.get("SELECT * FROM users");
			int usersCount = QRadServer.mySqlConnection.getSqlResultSize();
			if ( usersCount > 0 ) {
				System.out.println("There is " + usersCount + " users in the dataBase ");
				while (userLists.next()) {
					System.out.println( "***********************************************************");
					System.out.println( "User # " + userLists.getRow() + " : " );
					System.out.println("userName = " + userLists.getNString("userName"));
					System.out.println("userKey = " + userLists.getNString("userKey"));
					System.out.println("expirationDate = " + userLists.getDate("expirationDate"));
					System.out.println("isConnected = " + userLists.getBoolean("isConnected"));
					System.out.println("userID = " + userLists.getInt("userID"));
				}
			}else {
				System.out.println( "The Sql DataBase contains no user(s)." );
			}
			QRadServer.mySqlConnection.release(); // Release used statement
		} catch (Exception e) {
			System.out.println( "Exception reached when trying to read users Sql database datas." );
			e.printStackTrace();
			QRadServer.mySqlConnection.release(); // Release used statement
		}
	}

	public static boolean isSQLUserExists(String userKey) {
		System.out.println( "SQLQuery for user exists from userKey=" + userKey );
		try {
			ResultSet userLists = QRadServer.mySqlConnection.get( "SELECT * FROM users WHERE userKey='" + userKey + "'" );
			int usersCount = QRadServer.mySqlConnection.getSqlResultSize();
			if ( usersCount > 0) {
				System.out.println( "User(s) found with the provided userKey :" );
				while (userLists.next()) {
					System.out.println( "***********************************************************");
					System.out.println( "User # " + userLists.getRow() + " : " );
					System.out.println("userName = " + userLists.getNString("userName"));
					System.out.println("userKey = " + userLists.getNString("userKey"));
					System.out.println("expirationDate = " + userLists.getDate("expirationDate"));
					System.out.println("isConnected = " + userLists.getBoolean("isConnected"));
					System.out.println("userID = " + userLists.getInt("userID"));
				}
				QRadServer.mySqlConnection.release(); // Release used statement
				return true;
			}else {
				System.out.println( "No user found with the provided userKey" );
				QRadServer.mySqlConnection.release(); // Release used statement
				return true;
				
			}
		}catch( Exception e) {
			System.out.println( "Error when trying to find user in the DataBase, with the provided userKey" );
			e.printStackTrace();
			QRadServer.mySqlConnection.release();
			return false;
		}
	}

	public static int connectSQLUser( String userKey ) {
		if ( isSQLUserExists( userKey ) == true ) {
			try {
				 QRadServer.mySqlConnection.set( "UPDATE users SET isConnected='1' WHERE userKey='" + userKey + "'" );
				 ResultSet thisUser = QRadServer.mySqlConnection.get( "SELECT * FROM users WHERE userKey='" + userKey + "'" );
				 thisUser.next();
				 return thisUser.getInt("userID");
			}catch( Exception e ) {
				e.printStackTrace();
				return -1;
			}
		}else {
			return -1;
		}
	}

	public static int disconnectSQLUser( String userKey ) {
		if ( isSQLUserExists( userKey ) == true ) {
			try {
				 QRadServer.mySqlConnection.set( "UPDATE users SET isConnected='0' WHERE userKey='" + userKey + "'" );
				 ResultSet thisUser = QRadServer.mySqlConnection.get( "SELECT * FROM users WHERE userKey='" + userKey + "'" );
				 thisUser.next();
				 return thisUser.getInt("userID");
			}catch( Exception e ) {
				e.printStackTrace();
				return -1;
			}
		}else {
			return -1;
		}
	}

	public static Date getSQLUserExpirationDate( String userKey ) {
		try {
			ResultSet thisUser = QRadServer.mySqlConnection.get( "SELECT * FROM users WHERE userKey='" + userKey + "'" );
			if ( QRadServer.mySqlConnection.getSqlResultSize() > 0) {
				thisUser.next();
				return thisUser.getDate( "expirationDate" );
			}else {
				return null;
			}
		}catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean isAccountExpired( String userKey ) {
		Date userExpirationDate = SqlUsers.getSQLUserExpirationDate( userKey );
		if( userExpirationDate != null ) {
			// Get user Expiration date in java DATE format
			try {
				// SimpleDateFormat userDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
				// Date userDate = userDateFormat.parse( userExpirationDate );
				// Get the current date in java DATE format
				Date currentDate = new Date();
				if ( currentDate.after( userExpirationDate ) ) {
					return true;
				}else {
					return false;
				}
			}catch( Exception e ) {
				e.printStackTrace();
				return true;
			}
		}else {
			// If no user expiration date found, consider it as expired.
			return true;
		}
	
	}
}