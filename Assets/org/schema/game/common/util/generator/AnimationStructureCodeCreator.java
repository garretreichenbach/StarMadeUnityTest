package org.schema.game.common.util.generator;

import java.util.Locale;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AnimationStructureCodeCreator {

	/*
	 * 
	 * 
	 * <Animation>
				<Idle default="default">
					<Floating>
						<Item>FreeFlyIdle</Item>
					</Floating>

					<Gravity>
						<Item>StandIdle</Item>
					</Gravity>

				</Idle>

				<Moving default="default">

					<Jumping>
						<JumpUp>
							<Item>StandJump</Item>
						</JumpUp>
						<JumpDown>
							<Item>StandJump</Item>
						</JumpDown>
					</Jumping>
	 */
	public static CodeClass parse(Node root) {
		CodeClass rootClass = new CodeClass();
		rootClass.name = "AnimationStructure";
		NodeList topLevel = root.getChildNodes();
		for (int i = 0; i < topLevel.getLength(); i++) {
			Node item = topLevel.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				rootClass.nodes.add(parseRec(rootClass, root, "-"));
			}
		}

		return rootClass;
	}

	public static CodeNode parseRec(CodeNode parent, Node root, String pre) {

		CodeClass rootClass = new CodeClass();
		if (!parent.name.equals("AnimationStructure") && !parent.name.equals("Animation")) {
			rootClass.name = parent.name + root.getNodeName();
		} else {
			rootClass.name = root.getNodeName();
		}
		rootClass.normalName = root.getNodeName();
		rootClass.parent = parent;
		System.err.println(pre + "> " + rootClass.name);
		NodeList topLevel = root.getChildNodes();
		boolean end = false;
		for (int i = 0; i < topLevel.getLength(); i++) {
			Node item = topLevel.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("item")) {
					end = true;
					break;
				}
				rootClass.nodes.add(parseRec(rootClass, item, "-" + pre));
			}
		}
		if (end) {
			CodeEndpoint codeEndpoint = new CodeEndpoint();
			if (!parent.name.equals("AnimationStructure") && !parent.name.equals("Animation")) {
				codeEndpoint.name = parent.name + root.getNodeName();
			} else {
				codeEndpoint.name = root.getNodeName();
			}
			codeEndpoint.normalName = root.getNodeName();
			codeEndpoint.parent = parent;
			System.err.println(pre + "-> ENDPOINT");
			return codeEndpoint;
		}
		return rootClass;

	}
}
