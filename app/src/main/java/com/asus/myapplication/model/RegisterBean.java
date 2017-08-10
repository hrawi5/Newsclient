package com.asus.myapplication.model;

import com.asus.myapplication.util.BaseResp;

public class RegisterBean extends BaseResp{
	private int errorCode;
	private int userId;
	private String userName;

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
