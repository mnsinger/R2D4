package com.dataline.service;

import java.io.IOException;
import java.sql.SQLException;

import com.dataline.dao.UserDao;
import com.dataline.service.UserService;

public class UserServiceImpl implements UserService {

	private UserDao userDao;

	public UserDao getUserDao()
	{
		return this.userDao;
	}

	public void setUserDao(UserDao userDao)
	{
		this.userDao = userDao;
	}

	@Override
	public boolean isValidUser(String username, String password) throws SQLException {
		return userDao.isValidUser(username, password);
	}

	@Override
	public String getUserFromAddress(String username) throws SQLException, IOException {
		return userDao.getUserFromAddress(username);
	}

	@Override
	public String getDeveloperName(String username) throws SQLException {
		return userDao.getDeveloperName(username);
	}

	@Override
	public String getUsernameAd(String username) throws SQLException {
		return userDao.getUsernameAd(username);
	}

	@Override
	public String getUsernameR2d4(String username) throws SQLException {
		return userDao.getUsernameR2d4(username);
	}

}
