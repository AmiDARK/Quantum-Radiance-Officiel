package com.alcahest.QRadianceClient;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class radDriver{
	private Socket newSocket = null;
	private PrintWriter output = null;
	private BufferedReader input = null;
	String lastResponse = "";
	String serverMessage = "";
	private RemoteWebDriver driver = null;
	boolean isAlive = false;
	private Thread drvThread = null;

	
	radDriver( URL driverURL, DesiredCapabilities driverCaps ){
		String urlHost = driverURL.getHost();
		int urlPort = driverURL.getPort();
		if ( urlPort < 1 ) { urlPort = 18000; }
		if ( urlHost != null & urlHost.length() > 8 ) {
			try {
				System.out.println( "Host : " + urlHost + " at " + String.valueOf( urlPort ) );
				System.out.println( "Sent Capabilities : " + driverCaps.toString() );
				newSocket = new Socket( urlHost, urlPort );
				if ( newSocket.isConnected() == true ) {
					this.output = new PrintWriter( newSocket.getOutputStream(), true );
					this.input = new BufferedReader( new InputStreamReader( newSocket.getInputStream() ) );
					this.output.println( driverCaps.toString() );
				}
				boolean setupFail = false;
				while( newSocket.isConnected() == true && this.driver == null && setupFail == false ) {
					if( this.isThereANewMessage() == true ) {
						System.out.println( this.serverMessage );
						// ********************* On peut lancer le driver pour utiliser le serveur Appium
						if ( this.serverMessage.startsWith( "APPIUM:" ) ) {
							String remoteURL = this.serverMessage.replace( "APPIUM:", "" );
							this.driver = new RemoteWebDriver( new URL( remoteURL ), driverCaps );
							if( this.driver != null && this.driver.getSessionId() != null ) {
								this.output.println( "JOBSTARTED" ); // Client send message to the server to say "Yes, we got it and started to work on".
								this.isAlive = true;
								this.drvThread = new Thread( new keepContact( this ) );
							}else {
								this.driver = null;
								setupFail = true;
							}
						// ******************** On peut envoyer le fichier .exe de l'application.
						}else if ( this.serverMessage.startsWith( "SENDAPP" ) ) {
							String appFile = (String)driverCaps.getCapability( "app" );
							FileReader apkToSend = new FileReader( System.getProperty( "user.dir" ) + "/" + appFile );
							long fileSize =  new File( System.getProperty( "user.dir" ) + "/" + appFile ).length();
							this.output.println( "FILESIZE=" + String.valueOf( fileSize ) );
							apkToSend.transferTo( this.output );
							apkToSend.close();
							if( this.isThereANewMessage() == true ) {
								if ( !this.serverMessage.equals( "MESSAGE:FILERECEIVEDSUCCESSFULLY" ) == true ){
									System.out.println( "Server Error : " + this.serverMessage );
									setupFail = false;
								}
							}
							
							
						// ******************** Last case, connexion is rejected by Server
						}else {
							if( this.serverMessage.startsWith( "REJECTED" ) || this.serverMessage.startsWith( "CLOSE" ) ) {
								setupFail = true;
							}
						}
					}
				}		
				// Une fois le socket initialisé et l'URL du driver Appium créé par le serveur distant, 
				// On utiliser la méthode Super pour initialiser la classe mère avec les paramètres correspondants.
			} catch (UnknownHostException e) {
				System.out.println( "ERROR : UnknownHostException reachet on opening driver : " + e.getLocalizedMessage() );
			} catch (IOException e) {
				System.out.println( "ERROR : IOException reachet on opening driver : " + e.getLocalizedMessage() );
			} 
		}
		
	}
	
	public RemoteWebDriver getDriver() {
		RemoteWebDriver zeDriver = this.driver;
		return zeDriver;
	}
	
	public boolean close() {
		if ( this.driver != null ) {
			this.isAlive = false;
			while( this.drvThread.isAlive() == true ) {
				try { Thread.sleep( 250 ); }catch( Exception e ) {}
			}
			this.driver.quit();
			this.driver = null;
			this.output.println( "JOBFINISHED" );
			this.output.println( "CLOSECONNEXION" );
			this.output.close();
			this.output = null;
			// Separated exception handling to prevent from input closing blocking Socket closing.
			try { this.input.close(); }catch( Exception e ) { System.out.println( e.getLocalizedMessage() ); }
			this.input = null;
			try { this.newSocket.close(); }catch( Exception e ) { System.out.println( e.getLocalizedMessage() ); }
			this.newSocket = null;
			return true;
		}else {
			return false;
		}
	}
	
	public String receiveMessage() {
		String receivedMessage = "";
		char charCur[] = new char[ 1 ];
		try {
			while( this.input.read( charCur, 0, 1 ) != -1 ) {
				if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r') {
					receivedMessage += charCur[0];
				}else if( !receivedMessage.equalsIgnoreCase( "" ) ){
					return receivedMessage;
				}
	        }
		}catch( Exception e ) {
			System.out.println( "radServerThread run Exception " + e.getCause() );
		}
		return receivedMessage;
	}
	
	public boolean isThereANewMessage(){
		this.serverMessage = "";
		char charCur[] = new char[ 1 ];
		boolean success = waitUntilMessageArrives( 10000 );
		if (success == true ) {
			System.out.println( "Read the message" );
			try {
				while( this.input.read( charCur, 0, 1 ) != -1 ) {
					if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r') {
						// System.out.println( "CHAR : " + charCur[ 0 ] );
						this.serverMessage += charCur[0];
					}else if( !this.serverMessage.equalsIgnoreCase( "" ) ){
						System.out.println( "Message = " + this.serverMessage );
						return true;
					}
				}
			}catch( Exception e ) {
				System.out.println( "radServerThread run Exception " + e.getCause() );
			}
		}else {
			System.out.println( "No message to read" );
		}
		return false;
	}

	public boolean waitUntilMessageArrives( int maxWaitDuration ) {
		boolean success = false;
		int messageWait = 0;
		try {
			while( this.input.ready() == false && messageWait < maxWaitDuration ) {
				Thread.sleep( 100 );
				messageWait += 100;
			}
			if ( this.input.ready() == true ) {
				success = true;
			}else {
				System.out.println( "radclientThread waited at least " + ( maxWaitDuration / 1000.0f ) +  " seconds without receivine any message from server." );
			}
		}catch( Exception e ) {
			System.out.println( "radclientThread exception reached during the wait for a new message from server" );
			return false;
		}
		return success;
	}

	
	
	public class keepContact implements Runnable {
		private radDriver driver = null;
		
		keepContact( radDriver currentDriver ){
			this.driver = currentDriver;
		}
		
		public void run() {
			while( this.driver.isAlive == true ) {
				try {
					Thread.sleep( 1000 );
					this.driver.output.println( "KEEPCONTACT" );
				}catch( Exception e ) {
					
				}
			}
		}
		
	}
}
