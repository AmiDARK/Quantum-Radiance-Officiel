package com.alcahest.RadianceServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.mysql.cj.jdbc.*;

@SuppressWarnings("unused")
public class mySqlConnect {

	public Connection sqlConnect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	private String host = null;
	private String user = null;
	private String password = null;
	private String dataBase = null;

	public mySqlConnect( String host, String user, String password, String dataBase  ) {
		this.host = host;
		this.user = user;
		this.password = password;
		this.dataBase = dataBase;
	}

	public boolean connect(){
		String timezoneFix = "&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
		try {
			Class.forName( "com.mysql.cj.jdbc.Driver" );
			this.sqlConnect = DriverManager
					.getConnection("jdbc:mysql://" + this.host + "/" + this.dataBase + "?" + "user=" + this.user + "&password=" + this.password + timezoneFix );
			System.out.println( "Connection Successfull" );
			return true;
		}catch( Exception e ){
			e.printStackTrace();
			// throw e;
			return false;
		}
	}

	public ResultSet get( String query ) {
		if ( this.statement != null ) { this.release(); }
		try {
			this.statement = this.sqlConnect.createStatement();
			this.resultSet = this.statement.executeQuery( query );
			return this.resultSet;
		}catch( Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void set( String query ) {
		if ( this.statement != null ) { this.release(); }
		try {
			this.statement = this.sqlConnect.createStatement();
			int queryExecution = this.statement.executeUpdate( query );
		}catch( Exception e) {
			e.printStackTrace();
		}
	}

	public void release() {
		try {
			if (this.resultSet != null) {
				this.resultSet.close();
				this.resultSet = null;
			}
			if (this.statement != null) {
				this.statement.close();
				this.statement = null;
			}
		}catch( Exception e) {
			e.printStackTrace();
		}		
	}
	
	// You need to close the resultSet
	private void close() {
		try {
			this.release();
			if (this.sqlConnect != null) {
				this.sqlConnect.close();
				this.sqlConnect = null;
			}
		}catch( Exception e) {
			e.printStackTrace();
		}
	}

	public int getSqlResultSize() {
		if ( this.resultSet != null ) {
			try {
				this.resultSet.last();
				int resultSize = this.resultSet.getRow();
				this.resultSet.beforeFirst();
				return resultSize;
			}catch( Exception e) {
				System.out.println( "Exception reached when tried to get Sql query results size" );
				e.printStackTrace();
				return -1;
			}
		}else {
			return -1;
		}
	}

/*
	private void useless() throws Exception {
		// Statements allow to issue SQL queries to the database
		this.statement = this.sqlConnect.createStatement();
		// Result set get the result of the SQL query
		resultSet = this.statement.executeQuery("select * from feedback.comments");
		writeResultSet(resultSet);

		// PreparedStatements can use variables and are more efficient
		this.preparedStatement = this.sqlConnect
				.prepareStatement("insert into  feedback.comments values (default, ?, ?, ?, ? , ?, ?)");
		// "myuser, webpage, datum, summary, COMMENTS from feedback.comments");
		// Parameters start with 1
		this.preparedStatement.setString(1, "Test");
		this.preparedStatement.setString(2, "TestEmail");
		this.preparedStatement.setString(3, "TestWebpage");
		// preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
		this.preparedStatement.setString(5, "TestSummary");
		this.preparedStatement.setString(6, "TestComment");
		this.preparedStatement.executeUpdate();

		this.preparedStatement = this.sqlConnect
				.prepareStatement("SELECT myuser, webpage, datum, summary, COMMENTS from feedback.comments");
		resultSet = preparedStatement.executeQuery();
		writeResultSet(resultSet);

		// Remove again the insert comment
		this.preparedStatement = this.sqlConnect.prepareStatement("delete from feedback.comments where myuser= ? ; ");
		this.preparedStatement.setString(1, "Test");
		this.preparedStatement.executeUpdate();

		resultSet = this.statement.executeQuery("select * from feedback.comments");
		writeMetaData(resultSet);

	}

	private void writeMetaData(ResultSet resultSet) throws SQLException {
		// Now get some metadata from the database
		// Result set get the result of the SQL query

		System.out.println("The columns in the table are: ");

		System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
		for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
			System.out.println("Column " + i + " " + resultSet.getMetaData().getColumnName(i));
		}
	}

	private void writeResultSet(ResultSet resultSet) throws SQLException {
		// ResultSet is initially before the first data set
		while (resultSet.next()) {
			// It is possible to get the columns via name
			// also possible to get the columns via the column number
			// which starts at 1
			// e.g. resultSet.getSTring(2);
			String user = resultSet.getString("myuser");
			String website = resultSet.getString("webpage");
			String summary = resultSet.getString("summary");
			Date date = resultSet.getDate("datum");
			String comment = resultSet.getString("comments");
			System.out.println("User: " + user);
			System.out.println("Website: " + website);
			System.out.println("Summary: " + summary);
			System.out.println("Date: " + date);
			System.out.println("Comment: " + comment);
		}
	}
*/
}
