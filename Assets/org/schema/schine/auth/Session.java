package org.schema.schine.auth;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JDialog;

import org.schema.schine.auth.exceptions.WrongUserNameOrPasswordException;

public interface Session {

	public void login(String username, String passwd) throws IOException, WrongUserNameOrPasswordException;

	public boolean isValid();

	public void upload(File f, String bbName, int bbType, String description, String licence, JDialog jFrame) throws IOException;

	public ArrayList<String> retrieveNews(int max) throws IOException;

	public String getUniqueSessionId();

	public String getAuthTokenCode();

	public void afterLogin();

	public void setServerName(String string);

}
