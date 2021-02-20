package com.alcahest.QRadianceClient;
import com.alcahest.RadianceServer.SqlDevices;
import com.alcahest.RadianceServer.SqlUsers;
import com.alcahest.RadianceServer.mySqlConnect;
import com.alcahest.RadianceServer.QRadServer;

public class testMySqlConnect {
	
	// private static mySqlConnect mySqlConnection = null;

	public static void main( String[] args ) throws Exception
    {
		try {
			QRadServer.mySqlConnection = new mySqlConnect( "127.0.0.1", "alcahest", "alca29184", "qradiance"  );
			QRadServer.mySqlConnection.connect();
			SqlUsers.listSQLUsers();
			SqlDevices.listSQLDevices();
		}catch( Exception e) {
			e.printStackTrace();
		}
		
		
    }
}
