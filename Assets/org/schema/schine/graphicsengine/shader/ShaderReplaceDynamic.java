package org.schema.schine.graphicsengine.shader;

public class ShaderReplaceDynamic implements ShaderModifyInterface {
	public final String from;
	public final String to;

	public ShaderReplaceDynamic(String from, String to) {
		super();
		this.from = from;
		this.to = to;
	}

	@Override
	public String handle(String vsrc) {

		return vsrc.replaceAll(from, to);
	}

}
