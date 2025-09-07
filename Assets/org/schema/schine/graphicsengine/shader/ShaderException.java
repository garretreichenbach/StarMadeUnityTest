package org.schema.schine.graphicsengine.shader;

public class ShaderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String path;
	public String info;
	public String source;

	public ShaderException(String path, String glGetShaderInfoLog, String source) {
		super("\n" + path + " \n\n" + glGetShaderInfoLog + "\n");
		this.source = source;
		this.path = path;
		this.info = glGetShaderInfoLog;
	}

}
