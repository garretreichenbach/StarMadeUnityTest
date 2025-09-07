package org.schema.game.common.data.element.annotation;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementParserException;
import org.w3c.dom.Node;

public interface NodeSetting {
	public void parse(Node node, ElementInformation info) throws ElementParserException;
}
