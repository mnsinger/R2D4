package com.dataline.delegate;

import java.io.IOException;
import java.sql.SQLException;

import com.dataline.service.UserService;

public class LoginDelegate
{
	private UserService userService;

	public UserService getUserService()
	{
		return this.userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public boolean isValidUser(String username, String password) throws SQLException
	{
	    return userService.isValidUser(username, password);
	}
	
	public String getUserFromAddress(String username) throws SQLException, IOException
	{
	    return userService.getUserFromAddress(username);
	}

}