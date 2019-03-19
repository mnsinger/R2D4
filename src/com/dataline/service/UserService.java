package com.dataline.service;

import java.io.IOException;
import java.sql.SQLException;

public interface UserService {
	public boolean isValidUser(String username, String password) throws SQLException;

	String getUserFromAddress(String username) throws SQLException, IOException;

	public String getDeveloperName(String username) throws SQLException;

	public String getUsernameAd(String username) throws SQLException;

	public String getUsernameR2d4(String username) throws SQLException;

}
