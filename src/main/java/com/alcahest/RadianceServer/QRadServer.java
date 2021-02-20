package com.alcahest.RadianceServer;
import java.net.*;
import java.io.*;
import java.util.*;
import org.json.JSONObject;

/**
 * This class concern the main server.
 * It startup admininstration interface (useable from cli)
 * It receive connection requests from external users and send them the separate threads.
 * 
 * @author Alcahest
 *
 */
@SuppressWarnings("rawtypes")
public class QRadServer 
{
	private int usersConnected = 0;            // Contient la quantité d'users connectés au serveur
	private Vector usersTabs = new Vector();   // Contiendra les flux de sortie vers les clients
	public static mySqlConnect mySqlConnection = null;  // Used to connect main server to the mySQL DataBase
	public static String mySQL_URL  = "127.0.0.1";
	public static String mySQL_User = "alcahest";
	public static String mySQL_password = "alca29184";
	public static String mySQL_database = "qradiance";
	
	/**
	 * Here is the main server startup method.
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main( String[] args )
    {
		System.out.println( "Arguments = " + args );
		
		// Connect to the SQL DataBase
		mySqlConnection = new mySqlConnect( mySQL_URL, mySQL_User, mySQL_password, mySQL_database );
		mySqlConnection.connect();

		// Instanciate the main thread
		QRadServer mainServer  = new QRadServer();

		// Get arguments to define default port, etc.
		int sPort = 17000;
		if ( args.length > 0 ) {
			try {
				sPort = Integer.parseInt( args[ 0 ] ); // Sinon on utiliser l'argument 0
			}catch( Exception e ) {
				System.out.println( "radServer startup : Argument error" );
			}
		}

		try {
			new QRadServerAdmin( mainServer );
			ServerSocket mainServerSocket = new ServerSocket( sPort, 10 ); // Ouverture du socket sur le port du serveur
			outputWelcomeMessage( sPort );
			// When a connection request is received, a new RadServer thread is started to handle the connection.
			while( true ) {
				new QRadServerThread( mainServerSocket.accept(), mainServer );
			}
		}catch( Exception e) {
			System.out.println( "radServer Exception " + e.getCause() );
		}
    
    
    
    }
	
	
	static private void outputWelcomeMessage( Integer serverPort ) {
		System.out.println( "********************************" );
		System.out.println( "Radiance Server Ver 0.1.3 opened on port " + serverPort.toString() );
		System.out.println( "By : Frederic Cordier" );
		System.out.println( "Build : 2019.09.291707" );
		System.out.println( "********************************" );
		System.out.println( "Enter \"quit\" to stop server." );
		System.out.println( "Enter \"list\" for a guide to the current supported admin commands." );
		System.out.println( " " );
	}
	
	synchronized public int getClientAmount() {
		return usersConnected;
	}
	
	@SuppressWarnings("unchecked")
	synchronized public int addNewClient( PrintWriter clientOutput ) {
		usersConnected++;
		usersTabs.addElement( clientOutput );
		return usersTabs.size() -1;
	}
	
	synchronized public void removeClient( int clientNumber ) {
		usersConnected--;
		if ( usersTabs.elementAt( clientNumber ) != null ) {
			usersTabs.removeElementAt( clientNumber );
		}
	}
	
	synchronized public boolean getAuthentication( String clientMessage ) {
		if ( clientMessage != null && clientMessage.length() > 10 ) {
			JSONObject jSonAuth = new JSONObject( clientMessage.substring( clientMessage.indexOf( "{" ) ) );
			String userKey = jSonAuth.optString( "userKey" );
			if ( userKey != null && userKey.length() > 0 ) {
				if( SqlUsers.isSQLUserExists( userKey ) == true ) {
					SqlUsers.connectSQLUser( userKey );
				}
			}else {
				System.out.println( "Cannot recognize any user identification. Connection is rejected" );
				return false;
			}
			
			System.out.println( "User Key entered : " + userKey );
			String deviceUDID = jSonAuth.optString( "UDID" );
			System.out.println( "Device UDID = " + deviceUDID );
		}
		return false;
	}




}




