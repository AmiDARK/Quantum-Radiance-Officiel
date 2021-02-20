package com.alcahest.RadianceServer;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class QRadServerAdmin implements Runnable {

	QRadServer mainServer;
	BufferedReader input;
	String Command="";
	Thread theThread;
	mySqlConnect mySqlConnection = null;
	
	QRadServerAdmin( QRadServer theMainServer ){
		mainServer = theMainServer;
		input = new BufferedReader( new InputStreamReader( System.in ) );
		theThread = new Thread( this );
		theThread.start();		
	}

	public void run() {
		try {
			while( ( Command = input.readLine() ) != null ) {
				Command = Command.toLowerCase();
				if ( Command != null ) {
					// ************************************************************************************ ADB Commands
					if ( Command.length() > 4 && Command.startsWith( "adb" ) ) {
						switch( Command.toLowerCase() ) {
						case "adb.getproductmodel()":
							adbCommand( "adb getprop ro.product.model" );
							break;
						case "adb.getversion()":
							adbCommand( "adb shell getprop ro.build.version.release" );
							break;
						default:
							adbCommand( Command );
							break;
                        }
						Command = null;
					}
					// ************************************************************************************ Static commands
					if ( Command != null ) {
						switch( Command ) {
							case "quit" :
								System.exit( 0 );
								break;
							case "users" :
								SqlUsers.listSQLUsers();
								break;
							case "devices" :
								SqlDevices.listSQLDevices();
								break;
							case "startsql" :
								System.out.println( "Setup SQL Database" );
								try{
									this.mySqlConnection = new mySqlConnect( "127.0.0.1", "alcahest", "Alca29184", "qradiance" );
									this.mySqlConnection.connect();
								}catch( Exception e ) {
									System.out.println( "Cannot setup database : " );
									e.printStackTrace();
								}
								break;
							case "list" :
								System.out.println( "List of available commands :" );
								System.out.println( "============================" );
								System.out.println( "users : Will output the list of users currently registered to the server." );
								System.out.println( "devices : Will output the list of devices currently registered to the server with current connection state." );
								System.out.println( "quit : Will stop the server." );
								System.out.println( "list : Will output to the console, the list of all available commands" );
								System.out.println( "adb -args : Will execute the adb tool. Command line must be entered like in normal CMD window" );
								System.out.println( "users : Will output all the users available in the database" );
								break;
							case "update android list" :
								serverCommands.loadAndroidDevicesList();
								break;					
							default:
								System.out.println( "This command is not supported." );
								System.out.println( "Type 'list' for the list of all available commands" );
								break;
						}
					}
				}
				System.out.flush();
			}
		}catch( IOException e ) {
			System.out.println( "radServer.adminControl IOException reached : " + e.getCause() );
		}
	}
	
	
	public static void adbCommand( String adbRequest ){
		List<String> feedback = new ArrayList<String>();
		if ( adbRequest != null && adbRequest.length() > 3 ) {
			Process adbProcess;
			try {
				adbProcess = Runtime.getRuntime().exec( adbRequest );
				BufferedReader adbRequestInput = null;
				adbProcess.waitFor();
				if( adbProcess.exitValue() == 0 ) {
					adbRequestInput = new BufferedReader( new InputStreamReader( adbProcess.getInputStream() ) );
				}else {
					adbRequestInput = new BufferedReader( new InputStreamReader( adbProcess.getErrorStream() ) );
				}
				String directInput = null;
				while( ( directInput = adbRequestInput.readLine() ) != null ) {
					feedback.add( directInput );
					System.out.println( directInput );
				}
				adbProcess.waitFor();
			} catch (IOException e) {
				System.out.println( "radServer.adbCommand IOException reached : " + e.getLocalizedMessage() );
			} catch (InterruptedException e) {
				System.out.println( "radServer.adbCommand process InterruptedException reached : " + e.getLocalizedMessage() );
			}

		}
	}
}
