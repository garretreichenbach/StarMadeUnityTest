package org.schema.game.server.data.admin;

import java.util.Arrays;

public class AdminCommandIllegalArgument extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private AdminCommands command;
	private String[] parameters;
	private String msg;

	public AdminCommandIllegalArgument(AdminCommands c, String[] parameters) {
		super("[ERROR] " + c.name() + ". parameters " + Arrays.toString(parameters) + ". Usage: " + c.getDescription());
		this.command = c;
		this.parameters = parameters;
	}

	public AdminCommandIllegalArgument(AdminCommands c, String[] parameters, String msg) {
		super("[ERROR] " + c.name() + ". parameters " + Arrays.toString(parameters) + ". Message: " + msg + ". Usage: " + c.getDescription());
		this.command = c;
		this.parameters = parameters;
		this.msg = msg;
	}

	/**
	 * @return the command
	 */
	public AdminCommands getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(AdminCommands command) {
		this.command = command;
	}

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * @return the parameters
	 */
	public String[] getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}

}
