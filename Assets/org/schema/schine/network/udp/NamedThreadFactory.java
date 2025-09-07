package org.schema.schine.network.udp;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {
	private String name;
	private boolean daemon;
	private ThreadFactory delegate;

	public NamedThreadFactory(String name) {
		this(name, Executors.defaultThreadFactory());
	}

	public NamedThreadFactory(String name, boolean daemon) {
		this(name, daemon, Executors.defaultThreadFactory());
	}

	public NamedThreadFactory(String name, boolean daemon, ThreadFactory delegate) {
		this.name = name;
		this.daemon = daemon;
		this.delegate = delegate;
	}

	public NamedThreadFactory(String name, ThreadFactory delegate) {
		this(name, false, delegate);
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread result = delegate.newThread(r);
		String s = result.getName();
		result.setName(name + "[" + s + "]");
		result.setDaemon(daemon);
		return result;
	}
}
