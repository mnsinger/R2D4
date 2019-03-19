package com.dataline.dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import com.ibm.db2.jcc.DB2Driver;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

public class UserDaoImpl implements UserDao {

	DataSource dataSource ;

	public DataSource getDataSource()
	{
		return this.dataSource;
	}

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	@Override
	public boolean isValidUser(String username, String password) throws SQLException
	{
		System.out.println("IN isValidUser!!!");
		//select &quot;DEVELOPER PRIMARY&quot; from dbo.developer_names where &quot;DEVELOPER PRIMARY&quot;=?
		String query = "Select \"DEVELOPER PRIMARY\" from dbo.developer_names where \"DEVELOPER PRIMARY\" = ?";
		PreparedStatement pstmt = dataSource.getConnection().prepareStatement(query);
		pstmt.setString(1, username);
		// pstmt.setString(2, password);
		ResultSet resultSet = pstmt.executeQuery();
		System.out.println(pstmt.toString());
		if(resultSet.next()) {
			System.out.println("USER FOUND!!!");
		    //return (resultSet.getInt(1) > 0);
		    return true;
		}
        else {
			System.out.println("Couldn't find user in database!!!");
			return false;
        }
	}

	@Override
	public String getDeveloperName(String username) throws SQLException
	{
		System.out.println("IN isValidUser!!!");
		//select &quot;DEVELOPER PRIMARY&quot; from dbo.developer_names where &quot;DEVELOPER PRIMARY&quot;=?
		String query = "Select DisplayName from dbo.developer_names where UPPER(\"DEVELOPER PRIMARY\") = ?";
		PreparedStatement pstmt = dataSource.getConnection().prepareStatement(query);
		pstmt.setString(1, username.toUpperCase());
		// pstmt.setString(2, password);
		ResultSet resultSet = pstmt.executeQuery();
		System.out.println("IN getDeveloperName. query: " + query + ", username: " + username);
		if(resultSet.next()) {
			//System.out.println("USER FOUND!!!");
		    //return (resultSet.getInt(1) > 0);
		    return resultSet.getString("DisplayName");
		}
        else {
			System.out.println("Couldn't find user in database!!!");
			return username;
        }
	}

	@Override
	public String getUserFromAddress(String username) throws SQLException, IOException {

		String fromAddress = "";

		DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());

		System.out.println("About to load ibm driver");
		String url = "jdbc:db2://pidvudb1:51013/DVPDB01";
		// Set URL for data source
		String user = "singerm";

		//Path propFile = Paths.get("C:\\R2D4\\eclipse-workspace\\dataline.properties");

		//Files.write(file, lines, Charset.forName("UTF-8"));
		//List fileAsList = Files.readAllLines(propFile);

		//String fileStr = (String) fileAsList.get(0);

		//String password = fileStr;

    	HashMap<String, String> propMap = new HashMap<String, String>();
        FileReader reader = new FileReader("C:\\R2D4\\eclipse-workspace\\r2d4.properties");
        BufferedReader bufferedReader = new BufferedReader(reader);

        String line;

        while ((line = bufferedReader.readLine()) != null) {
        	propMap.put(line.split("=")[0], line.split("=")[1]);
        }
    	System.out.println("propMap[idb_pswd]=" + propMap.get("idb_pswd"));
    	System.out.println("propMap[darwin_apps_pswd]=" + propMap.get("darwin_apps_pswd"));
        reader.close();

		Connection con = DriverManager.getConnection(url, propMap.get("darwin_apps_user"), propMap.get("darwin_apps_pswd"));
		// Create connection

		con.setAutoCommit(true);

		Statement stmt;
		ResultSet rs;

		// Create the Statement
		stmt = con.createStatement();                                            

		String getNameSQL = "select dv.ldapquery('" + username + "', 'cn', 'displayName') from sysibm.sysdummy1";
		System.out.println(getNameSQL);

		rs = stmt.executeQuery(getNameSQL);

		if (rs.next()) {
			fromAddress = rs.getString(1);
		}
		rs.close();
		stmt.close();

		con.close();

		return fromAddress;

	}

	@Override
	public String getUsernameAd(String username) throws SQLException {

		String query = "select coalesce(username, \"developer primary\") as username from dbo.developer_names where UPPER(\"DEVELOPER PRIMARY\") = ?";
		PreparedStatement pstmt = dataSource.getConnection().prepareStatement(query);
		pstmt.setString(1, username.toUpperCase());
		// pstmt.setString(2, password);
		ResultSet resultSet = pstmt.executeQuery();
		System.out.println("IN getUsernameAd. query: " + query + ", username: " + username);
		if(resultSet.next()) {
			//System.out.println("USER FOUND!!!");
		    //return (resultSet.getInt(1) > 0);
		    return resultSet.getString("username");
		}

		return "";
		
	}

	@Override
	public String getUsernameR2d4(String usernameAd) throws SQLException {

		String query = "select \"developer primary\" as username from dbo.developer_names where UPPER(COALESCE(username, \"DEVELOPER PRIMARY\")) = ?";
		PreparedStatement pstmt = dataSource.getConnection().prepareStatement(query);
		pstmt.setString(1, usernameAd.toUpperCase());
		ResultSet resultSet = pstmt.executeQuery();
		System.out.println("IN getUsernameR2d4. query: " + query + ", usernameAd: " + usernameAd);
		if(resultSet.next()) {
			//System.out.println("USER FOUND!!!");
		    //return (resultSet.getInt(1) > 0);
		    return resultSet.getString("username");
		}
		
		return "Not valid user";
		
	}

}
