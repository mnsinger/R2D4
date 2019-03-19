package com.dataline.dao;

import java.io.IOException;
import java.sql.SQLException;

public interface UserDao {
	public boolean isValidUser(String username, String password) throws SQLException;

	public String getUserFromAddress(String username) throws SQLException, IOException;

	String getDeveloperName(String username) throws SQLException;

	String getUsernameAd(String username) throws SQLException;

	String getUsernameR2d4(String usernameAd) throws SQLException;
}
