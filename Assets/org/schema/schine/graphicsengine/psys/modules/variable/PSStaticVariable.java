package org.schema.schine.graphicsengine.psys.modules.variable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class PSStaticVariable implements PSVariable<Float> {

	@Override
	public Float get(float lifetime) {
		return 0.0f;
	}

	@Override
	public void set(PSVariable<Float> var) {
	}

	@Override
	public void appendXML(Object m, Element element) {
		
	}

	@Override
	public void parse(Node item) {
		
	}

}
