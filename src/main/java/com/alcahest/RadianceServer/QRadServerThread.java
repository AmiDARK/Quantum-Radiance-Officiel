package com.alcahest.RadianceServer;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import org.json.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.alcahest.RadianceServer.SqlDevices.SqlDevicesDetails;
import com.alcahest.RadianceServer.errorHandler.errorType;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;

import java.io.*;

/**
 * This class handle client<->server connections.
 * 
 * @author Alcahest
 *
 */
@SuppressWarnings("unused")
public class QRadServerThread implements Runnable {
	private Thread clientThread;
	private Socket clientSocket;
	private BufferedReader input;
	private QRadServer mainServer;
	private boolean isClosed = false;
	private int clientNumber = 0;
	private String clientMessage = "";
	private String lastClientMessage = "";

	// Appium server datas
	private PrintWriter output;
	private JSONObject jSonAuth = null; // The authentication + capabilities request message.
	private String userKey = null;
	public AppiumServer appiumServer = null;
	public boolean appServerStarted = false;
	public DesiredCapabilities appCaps = null;
	public AppiumServiceBuilder builder = null;
	public AppiumDriverLocalService serviceToStart = null;
	public String AppiumIP = null;
	public int usePort = -1;
	private boolean runTests = false;
	private int noNewMessage = 0;
	private String userPath = null;
	private String appFile = null;
	// Device ID
	// private int deviceID = -1;
	public SqlDevicesDetails device = null;

	// Web connexion data
	private String clientIP = null;
	private int clientPort = -1;
	public DataOutputStream outputWeb = null;

	// File transfert data
	private FileInputStream fileLoader = null;
	
	// Types de connections entrantes autorisées
	private final int appiumConnexionType = 1;
	private final int webConnexionType = 2;

	/**
	 * This method will setup and start the new radServer thread
	 * 
	 * @param theClientSocket
	 * @param theMainServer
	 */
	QRadServerThread(Socket theClientSocket, QRadServer theMainServer) {
		this.mainServer = theMainServer;
		this.clientSocket = theClientSocket;
		try {
			// Create the input/output communication handlers
			// this.output = new PrintWriter( this.clientSocket.getOutputStream() );
			this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			// memorize the ID of the client
			this.clientNumber = theMainServer.addNewClient(this.output);
		} catch (IOException e) {
			System.out.println("radServerThread IOException " + e.getLocalizedMessage());
		}
		// Instanciate and start.
		clientThread = new Thread(this);
		clientThread.start();
	}

	/**
	 * This method is the server itself and handle everything concerning the
	 * connection with client.
	 */
	public void run() {
		this.clientMessage = "";
		// ******************************************************************** 1. Check
		// for client authentication
		System.out.println("A new client try to connect (ID=" + clientNumber + ")");
		if (this.isThereANewMessage() == true && this.isClosed == false) {
			switch (this.analyzeMessageType()) {
			case appiumConnexionType:
				this.getAppiumConnection();
				this.output.println("CLOSE");
				this.isClosed = true;
				break;
			case webConnexionType:
				this.webConnection();
				this.isClosed = true;
				break;
			default:
				System.out.println("Unknown connection type. Rejected");
				break;
			}
		}
		this.closeConnection();

	}

	public int analyzeMessageType() {
		if (this.clientMessage.toLowerCase().startsWith("capabilities")) {
			return appiumConnexionType;
		} else if (this.clientMessage.startsWith("GET")) {
			return webConnexionType;
		}
		return -1;
	}

	// ************************************************************************************************************************
	// Gestion connexion WEB
	/**
	 * 
	 */
	public void webConnection() {
		this.clientIP = this.clientSocket.getInetAddress().getHostAddress();
		this.clientPort = this.clientSocket.getPort();
		System.out.println("client ID=" + this.clientNumber + " : Catched a Web connexion from IP : " + this.clientIP
				+ " (port=" + this.clientPort + ").");
		try {
			this.outputWeb = new DataOutputStream(this.clientSocket.getOutputStream());
			String requestString = this.clientMessage;
			String headerLine = requestString;
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();
			System.out.println("client ID=" + this.clientNumber + " : httpMethod = " + httpMethod);
			System.out.println("client ID=" + this.clientNumber + " : httpQueryString = " + httpQueryString);
			if (httpQueryString.equals("/")) {
				webServer.SendPage(this, 200, "/index.html", true);
			} else {
				webServer.SendPage(this, 200, httpQueryString, true);
			}
		} catch (IOException e) {
			System.out.println("client ID=" + this.clientNumber + " : IO Exception on connection output stream setup : "
					+ e.getLocalizedMessage());
		}
	}

	// ************************************************************************************************************************
	// Gestion connexion APPIUM
	/**
	 * 
	 */
	public void getAppiumConnection() {
		try {
			this.output = new PrintWriter(this.clientSocket.getOutputStream());
			// ********************************************************************************************* Verify if userKey leads to an existing user account
			if (this.CheckAuthentication() == true) {
				this.userKey = this.jSonAuth.optString("userKey");
				// ***************************************************************************************** Verify is user account is valid or expired
				if (SqlUsers.isAccountExpired(this.userKey) == false) {
					this.userPath = System.getProperty( "user.dir" ) + "/" + this.userKey ;
					System.out.println("Client ID=" + clientNumber + " : Connexion accepted.");
					SqlUsers.connectSQLUser(this.userKey); //                                                  Set user as connected
					// ************************************************************************************ Send Server Welcome message to connected user
					System.out.println("Client ID=" + clientNumber + " : Send Welcome Message.");
					this.sendWelcomeMessage(); //                                                           Send Server Welcome message to client.
					// **************************************************************** 3. Check if
					// device exists and if it's free
					// ************************************************************************************ Search for an existing and available device
					System.out.println("Client ID=" + clientNumber + " : Search for requested device.");
					JSONObject jDat = this.jSonAuth;
					this.device = SqlDevices.findSQLDevice(jDat.optString("deviceName"), jDat.optString("platformName"),
							jDat.optString("platformVersion"), jDat.optString("udid"),
							jDat.optString("deviceManufacturer"), jDat.optString("deviceModel"), true); // true enable availability checking
					// ************************************************************************************ If an available device is found we allocate it to the current user
					if (this.device != null ) {
						System.out.println("Client ID=" + clientNumber + " : Device found.");
						SqlDevices.outputSQLDevice( this.device );
						// ******************************************************** 5. We reserve the
						// device for the current user.
						if ( SqlDevices.reserveSQLDevice( this.device, this.userKey ) == true) {
							// **************************************************** 6. Now we Create
							// capabilities from requested informations
							// Check json data received and build a correct capability from it.
							this.appCaps = new DesiredCapabilities();
							if ( Utilities.buildCapabilitiesFromJson(this.jSonAuth, this.appCaps ) == true) {
								// If the capabilities contain an App under an .apk file name, we request the user to send it.
								this.appFile = (String)this.appCaps.getCapability( "app" );
								System.out.println( "Client ID=" + this.clientNumber + " : Requested APK Link = " + this.appFile );
								boolean apkSuccess = true;
								if( this.appFile != null && this.appFile.toLowerCase().contains( ".apk" ) ) {
									apkSuccess = this.downloadApkFile();
								}
								
								if( apkSuccess == true ) {
									System.out.println("Client ID=" + clientNumber
											+ " : Build capabilities from received jSon datas.");
									// ************************************************ 7. Start appium server
									// After the welcome message, server will check if the requested device is
									// available of not
									System.out.println("Client ID=" + clientNumber
											+ " : Try to start Appium Driver for the requested device.");
									this.appiumServer = new AppiumServer(this.appCaps, this);
									// If the driver started correctly we now remain in standby waiting for the
									// client to finish it's automation testing.
									if (this.appServerStarted == true) {
										System.out.println("client ID=" + this.clientNumber
												+ " : Appium Server Started successfully");
										System.out.println("client ID=" + this.clientNumber + " : Appium Server ID = "
												+ this.AppiumIP);
										System.out.println("client ID=" + this.clientNumber + " : Appium used port = "
												+ this.usePort);
										// ************************************************************ 8. Send appium
										// IP + Port to client
										this.output.println("APPIUM:http://" + this.AppiumIP + ":"
												+ String.valueOf(this.usePort) + "/wd/hub");
										this.output.flush();
										// ************************************************************ 9. Wait for
										// client message
										if (isThereANewMessage() == true) {
											if (this.clientMessage.equals("JOBSTARTED")) {
												this.runTests = true;
												SqlDevices.setSQLDeviceAsRunningTests( this.device, this.userKey );
												while (this.runTests == true) {
													if (isThereANewMessage(10000) == true) {
														if (this.clientMessage.equals("JOBFINISHED")) {
															this.output.println(
																	"MESSAGE: Server received message for end of tests. Connection will be closed");
															this.runTests = false;
														} else if (this.clientMessage.equals("KEEPCONTACT")) {
															System.out.println("client ID=" + this.clientNumber
																	+ " keep contact.");
														}
													} else {
														this.noNewMessage++;
														if (this.noNewMessage > 3) {
															this.runTests = false;
															this.output.println(
																	" MESSAGE: Client did not keep normal contact. Connection will be closed.");
														}
													}
												}
												// When runTests finished of contact failed, we turn on appiumServer
												SqlDevices.reserveSQLDevice( this.device, this.userKey); // Puseh device from runningTest to Reserved
												this.appiumServer.stopAppiumServer();
												// Last step, wait from client sending "connexion closing".
												this.noNewMessage = 0;
												try {
													if (isThereANewMessage(10000) == false) {
														this.noNewMessage++;
													} else {
														if (this.clientMessage.equals("CLOSECONNEXION")) {
															System.out.println("Client ID " + this.clientNumber
																	+ " closed connexion normally.");
														}
													}
												} catch (Exception e) {
													System.out.println("Client ID " + this.clientNumber
															+ " didn't close connexion normally.");
												}
											} else {
												this.output.println(
														"MESSAGE: The client did not validate the use of the AppiumServer. Connection will be closed.");
												this.appiumServer.stopAppiumServer();
											}
										} else {
											this.output.println(
													"MESSAGE: The client did not validate the use of the AppiumServer. Connection will be closed.");
											this.appiumServer.stopAppiumServer();
										}
									} else {
										this.output.println(
												"MESSAGE: The appium server did not start correctly. Please retry in a few moment.");
									}
								
								}else {
									this.output.println(
											"MESSAGE: There was an error when handling the application APK File" );
									
								}
								
								
							} else {
								this.output.println("MESSAGE: Cannot build capabilities from the received data.");
							}
						} else {
							this.output.println(
									"MESSAGE: Cannot reserve the chosen device. Please retry in a few moment");
						}
					} else {
						this.output.println("MESSAGE: " + SqlDevices.getLastSQLErrorMessage() );
					}
				} else {
					this.output.println( "MESSAGE: User account expired on " + SqlUsers.getSQLUserExpirationDate( this.userKey ) );
				}
				SqlDevices.releaseSQLDevice( this.device, this.userKey );
				SqlUsers.disconnectSQLUser(this.userKey);
			} // No need for else in authentication as failure message is already coded in the
				// authentication method.
		} catch ( Exception e1 ) {
			System.out.println("Client ID=" + clientNumber + " : IOException reached during Appium Activity setup/run : "
					+ e1.getLocalizedMessage());
		}
	}

	/**
	 * 
	 */
	public void closeConnection() {
		try {
			Thread.sleep(2000);
			System.out.println("Client ID" + this.clientNumber + " disconnected from server");
			mainServer.removeClient(this.clientNumber);
			if( this.output != null ) {
				this.output.close();
				this.output = null;
			}
			if( this.outputWeb != null ) {
				this.outputWeb.close();
				this.outputWeb = null;
			}
			if( this.input != null ) {
				this.input.close();
				this.input = null;
			}
			if( this.clientSocket != null ) {
				this.clientSocket.close();
				this.clientSocket = null;
			}
		} catch (Exception e) {
			System.out.println(
					"radServerThread Error occured during connection closing with client #" + this.clientNumber);
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @return
	 */
	public boolean CheckAuthentication() {
		this.output.flush();
		if (this.clientMessage != null && this.clientMessage.length() > 10) {
			this.jSonAuth = new JSONObject(this.clientMessage.substring(clientMessage.indexOf("{")));
			String userKey = jSonAuth.optString("userKey");
			if (userKey != null && userKey.length() > 0) {
				System.out.println("UserKey = " + userKey);
				if (SqlUsers.isSQLUserExists(userKey) == true) {
					SqlUsers.connectSQLUser(userKey);
					this.output.println("MESSAGE:Connection successfully etablished.");
					this.output.flush();
					return true;
				}
			} else {
				System.out.println("No User key received");
			}
		}
		this.output.println("MESSAGE:radServerThread Cannot identify any user identification. Connection is rejected");
		this.output.println("REJECTED");
		this.output.flush();
		this.lastClientMessage = this.clientMessage;
		this.clientMessage = "";
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isThereANewMessage() {
		return isThereANewMessage(10000);
	}

	public boolean isThereANewMessage(int maxDelay) {
		this.clientMessage = "";
		char charCur[] = new char[1];
		boolean success = waitUntilMessageArrives(maxDelay);
		if (success == true) {
			System.out.println("client ID = " + this.clientNumber + " : Read the message");
			try {
				while (this.input.read(charCur, 0, 1) != -1 ) {
					if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r') {
						// System.out.println( "CHAR : " + charCur[ 0 ] );
						this.clientMessage += charCur[0];
					} else if (!this.clientMessage.equalsIgnoreCase("")) {
						System.out.println("client ID = " + this.clientNumber + " : Message = " + this.clientMessage);
						return true;
					}
				}
			} catch (Exception e) {
				System.out.println(
						"client ID = " + this.clientNumber + " : radServerThread run Exception " + e.getCause());
			}
		} else {
			System.out.println("client ID = " + this.clientNumber + " : No message to read");
		}
		return false;
	}

	public boolean waitUntilMessageArrives(int maxWaitDuration) {
		int messageWait = 0;
		try {

			while ( this.input.ready() == false && messageWait < maxWaitDuration ) {
				Thread.sleep(1000);
				messageWait += 1000;
				System.out.println( messageWait );
			}
			if (this.input.ready() == true ) {
				return true;
			} else {
				
				System.out.println("radServerThread waited at least " + (maxWaitDuration / 1000.0f)
						+ " seconds without receivine any message from client.");
				return false;
			}
		} catch (Exception e) {
			System.out.println("radServerThread exception reached during the wait for a new message from client");
			return false;
		}
	}

	public void sendWelcomeMessage() {
		this.output.println("MESSAGE:Frederic Cordier's radServer Ver 0.1");
		this.output.println("MESSAGE:Authentication successful");
		this.output.println("MESSAGE: ");
		this.output.flush();
	}

	
	// https://stackoverflow.com/questions/4687615/how-to-achieve-transfer-file-between-client-and-server-using-java-socket
	
	public boolean downloadApkFile() {
		System.out.println( "User requested an APK file : " + this.appFile );
		// If file exists() delete it, then create a new blank file
		try {
			Files.deleteIfExists( Paths.get( this.userPath + "\\" + this.appFile ) );
		} catch (IOException e) {
			System.out.println( "Server exception when tried to check for file exists : " + this.userPath + "\\" + this.appFile );
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new File( this.userPath ).mkdir();
		this.output.println( "REQUEST:SENDAPP" );
		this.output.flush();
		System.out.println( "REQUEST:SENDAPP sent " );
		if ( this.isThereANewMessage( 10000 ) == true ) {
			if ( this.clientMessage.startsWith( "FILESIZE:" ) == true ) {
				// On récupère l'information de dimension du fichier .APK à récupérer
				String fileSizeSTR = this.clientMessage.replace( "FILESIZE:", "" );
				int fileSize = Integer.parseInt( fileSizeSTR ); // Récupération de la dimension du fichier à recevoir.
				byte[] bytesToRead = new byte[ fileSize ];
				// On lit le contenu du fichier APK depuis les données transférées par le client
				try {
					InputStream fromClient = this.clientSocket.getInputStream();
					int bytesRead = fromClient.read( bytesToRead, 0, fileSize );
					fromClient.close();
					FileOutputStream ouputApk = new FileOutputStream( this.userPath + "\\" + this.appFile );
					BufferedOutputStream outputApkBuf = new BufferedOutputStream( ouputApk );
					outputApkBuf.write( bytesToRead, 0, fileSize );
					outputApkBuf.close();
					ouputApk.close();
					if( bytesRead == fileSize ) {
						this.output.println( "MESSAGE:FILERECEIVEDSUCCESSFULLY" );
						this.output.flush();
						return true;
					}else {
						this.output.println( "MESSAGE:INCORRECTFILESIZE" );
						this.output.println( "MESSAGE:Server Received " + String.valueOf( bytesRead ) + "bytes out of " + String.valueOf( fileSize ) + "bytes planed." );
						this.output.flush();
						return false;
					}
				}catch( Exception e) {
					e.printStackTrace();
					return false;
				}
			}else {
				this.output.println( "MESSAGE:INCORRECTMESAGERECEIVED" );
				this.output.println( "MESSAGE: Awaited message was 'FILESIZE:' followed with APK File size in bytes" );
				this.output.flush();
				return false;
			}
		}else {
			this.output.println( "MESSAGE:NOMESSAGERECEIVEDFROMCLIENT" );
			this.output.flush();
			return false;
		}
	}
}
