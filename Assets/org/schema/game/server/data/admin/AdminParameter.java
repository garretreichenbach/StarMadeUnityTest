package org.schema.game.server.data.admin;

public class AdminParameter {
	public final Class<? extends Object> clazz;
	public final String name;
	public final String example;

	public AdminParameter(Class<? extends Object> clazz, String name,
	                      String example) {
		super();
		this.clazz = clazz;
		this.name = name;
		this.example = example;
	}

}
