package com.alcahest.RadianceServer;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class webServer {
	static final String HTML_START = "<html>" + "<title>Quantum Radiance Server ver 0.1</title>" + "<body>";
	static final String HTML_END = "</body>" + "</html>";

	@SuppressWarnings("unused")
	public static void SendPage( QRadServerThread currentServer, int statusCode, String responseURL, boolean isFile ) {
		String StatusString = null;
		if ( statusCode == 200 ) {
			StatusString = "HTTP/1.1 200 OK" + "\r\n";
		}else {
			StatusString = "HTTP/1.1 404 Not Found" + "\r\n";
		}
		if ( isFile == true ) {
			try {
				FileInputStream outputFile = new FileInputStream( "web" + responseURL );
				String responseString = HTML_START + responseURL + HTML_END;
				String contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
				currentServer.outputWeb.writeBytes( StatusString );
				currentServer.outputWeb.writeBytes( "Server Java HTTPServer" );
				currentServer.outputWeb.writeBytes( "Content-Type: text/html" + "\r\n" );
				currentServer.outputWeb.writeBytes( "Connection: close\r\n" );
				currentServer.outputWeb.writeBytes( "\r\n" );
				sendFile( outputFile, currentServer.outputWeb );
			}catch( FileNotFoundException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void sendFile(FileInputStream fin, DataOutputStream out){
		byte[] buffer = new byte[1024];
		int bytesRead;

		try {
			while ((bytesRead = fin.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			fin.close();
		}catch( Exception e) {
			
		}
	}

}
