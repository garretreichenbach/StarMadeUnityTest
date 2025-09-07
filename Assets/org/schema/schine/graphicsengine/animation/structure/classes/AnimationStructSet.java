package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.ArrayList;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AnimationStructSet {
	public static final boolean DEBUG = false;
	public AnimationStructSet parent;
	protected boolean parsed;
	protected ArrayList<AnimationStructSet> children = new ArrayList<AnimationStructSet>();

	public static String printParentPath(AnimationStructSet node) {
		if (node.parent != null) {
			return printParentPath(node.parent) + " -> " + node.getClass().getSimpleName();
		} else {
			return node.getClass().getSimpleName();
		}
	}

	public void parse(Node node, String def, AnimationStructSet parent) {
		this.parent = parent;
		if (node != null) {
			NodeList topLevel = node.getChildNodes();
			for (int i = 0; i < topLevel.getLength(); i++) {
				Node item = topLevel.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE) {
					String dd = def;

					NamedNodeMap aAttributes = topLevel.item(i).getAttributes();
					Node afileNameItem = aAttributes.getNamedItem("default");
					if (afileNameItem != null) {
						def = afileNameItem.getNodeValue();
					}
					parseAnimation(item, def);
//					System.err.println("ANIMATION PARSED FOR "+printParentPath(this)+" :: "+item.getNodeName()+" "+(item.getNodeName().equals("Item") ? " = "+item.getTextContent() : ""));
				}
			}
			checkAnimations(def);
//			System.err.println("TOTAL PARSED: ");
//			for(int i = 0; i < children.size(); i++){
//				System.err.println("----> "+printParentPath(children.get(i))+": "+((children.get(i) instanceof AnimationStructEndPoint) ? Arrays.toString(((AnimationStructEndPoint)children.get(i)).animations) : ""));
//			}
		} else {
			parseAnimation(null, def);
			checkAnimations(def);
		}
		parsed = true;
	}

	public abstract void checkAnimations(String def);

	public abstract void parseAnimation(Node item, String def);

	public boolean isType(Class<? extends AnimationStructSet> clazz) {
		if (this.getClass().isAssignableFrom(clazz)) {
			return true;
		}
		if (parent != null) {
			return parent.isType(clazz);

		}
		return false;
	}
}
