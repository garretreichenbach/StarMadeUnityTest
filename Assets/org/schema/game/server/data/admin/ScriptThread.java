package org.schema.game.server.data.admin;

import org.schema.game.server.data.GameServerState;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.BufferedReader;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class ScriptThread extends Thread {

	private final ScriptEngine engine;
	private final BufferedReader reader;
	private final String function;
	private long startTime;

	public ScriptThread(ScriptEngine engine, BufferedReader reader, String function, String name) {
		this.engine = engine;
		this.reader = reader;
		this.function = function;
		setName(name + ":" + function + "()");
	}

	@Override
	public void start() {
		startTime = System.currentTimeMillis();
		super.start();
	}

	@Override
	public void run() {
		try {
			engine.eval(reader);
			engine.eval(function + "()");
			GameServerState.scriptThreads.remove(this);
		} catch(ScriptException exception) {
			System.err.println("Error while executing script: " + exception.getMessage());
		}
	}

	public long getStartTime() {
		return startTime;
	}
	
	public long getRunTime() {
		return System.currentTimeMillis() - startTime;
	}
}
