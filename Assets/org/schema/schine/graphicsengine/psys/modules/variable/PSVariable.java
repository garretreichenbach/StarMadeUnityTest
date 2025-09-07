package org.schema.schine.graphicsengine.psys.modules.variable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface PSVariable<E> {

	public E get(float lifetime);

	public void set(PSVariable<E> var);

	public void appendXML(Object m, Element element);

	public void parse(Node item);
}
