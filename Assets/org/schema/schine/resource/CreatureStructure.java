package org.schema.schine.resource;

import org.schema.common.ParseException;
import org.schema.common.util.linAlg.Vector3i;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.vecmath.Vector3f;
import java.util.HashSet;
import java.util.Locale;

public class CreatureStructure {

	/*
	 * <Creature>
				<Type>aracnic</Type>
				<PartType>bottom</PartType>
				<AttachmentBones>
					<Main>PelvisRoot</Main>
					<Weapon>none</Weapon>
				</AttachmentBones>
				<MaxScale><X>10</X><Y>10</Y><Z>10</Z></MaxScale>
				<BoxDim>
					<X>1</X><Y>1</Y><Z>1</Z>
				</BoxDim>
			</Creature> 
	 * 
	 * 
	 * 
	 */

	/*
	 *  <Main>PelvisRoot</Main>
		<Head>none</Head>
		<RightHand>none</RightHand>
		<LeftHand>none</LeftHand>
		<RightHolster>none</RightHolster>
		<LeftHolster>none</LeftHolster>
		<RightBackHolster>none</RightBackHolster>
		<LeftBackHolster>none</LeftBackHolster>
		<Held>none</Held>
		<Neck>PlSkelNeck2</Neck>
		<RightCollarBone>PlSkelRArmCollarbone</RightCollarBone>
		<LeftCollarBone>PlSkelLArmCollarbone</LeftCollarBone>
	 */
	public String type;
	public HashSet<PartType> partType = new HashSet<PartType>();
	public String mainBone;
	public String headBone;
	public String heldBone;
	public String rightHand;
	public String leftHand;
	public String rightForeArm;
	public String leftForeArm;
	public String rightHolster;
	public String leftHolster;
	public String rightBackHolster;
	public String leftBackHolster;
	public String upperBody;
	public String neck;
	public String rightCollarBone;
	public String leftCollarBone;
	public Vector3i dim;
	public Vector3f maxScale;

	public static CreatureStructure parse(Node root) throws ParseException {
		CreatureStructure t = new CreatureStructure();
		NodeList topLevel = root.getChildNodes();
		for(int i = 0; i < topLevel.getLength(); i++) {
			Node item = topLevel.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				System.err.println("[ANIMATION] PARSING CREATURE: " + root.getNodeName() + "->" + item.getNodeName());
				t.parseCreature(item);
			}
		}
		StringBuilder s = new StringBuilder();
		if(t.type == null) s.append("type, ");
		if(t.partType == null) s.append("partType, ");
		if(t.mainBone == null) s.append("mainBone, ");
		if(t.heldBone == null) s.append("heldBone, ");
		if(t.rightHand == null) s.append("rightHand, ");
		if(t.leftHand == null) s.append("leftHand, ");
		if(t.rightHolster == null) s.append("rightHolster, ");
		if(t.leftHolster == null) s.append("leftHolster, ");
		if(t.rightBackHolster == null) s.append("rightBackHolster, ");
		if(t.leftBackHolster == null) s.append("leftBackHolster, ");
		if(t.upperBody == null) s.append("upperBody, ");
		if(t.headBone == null) s.append("headBone, ");
		if(t.neck == null) s.append("neck, ");
		if(t.rightCollarBone == null) s.append("rightCollarBone, ");
		if(t.leftCollarBone == null) s.append("leftCollarBone, ");
		if(t.rightForeArm == null) s.append("rightForeArm, ");
		if(t.leftForeArm == null) s.append("leftForeArm, ");
		if(t.dim == null) s.append("boxDim, ");
		if(t.maxScale == null) s.append("maxScale, ");
		if(s.length() > 0) {
			s.delete(s.length() - 2, s.length());
			throw new ParseException("Error parsing creature! missing: \"" + s + "\"; on " + root.getParentNode().getNodeName() + "->" + root.getNodeName());
		}
		return t;
	}

	private void parseCreature(Node item) throws ParseException {
		String t = item.getNodeName().toLowerCase(Locale.ENGLISH);
		System.err.println("[CREATURE] NOW PARSING: " + t);
		switch(t) {
			case "type" -> type = item.getTextContent();
			case "parttype" -> {
				String ln = item.getTextContent().toLowerCase(Locale.ENGLISH);
				String[] split = ln.split(",");
				for(String l : split) {
					switch(l) {
						case "top" -> partType.add(PartType.TOP);
						case "middle" -> partType.add(PartType.MIDDLE);
						case "bottom" -> partType.add(PartType.BOTTOM);
						default -> throw new ParseException("PartType invalid: " + l + " in node: " + t);
					}
				}
			}
			case "attachmentbones" -> parseAttachment(item.getChildNodes());
			case "boxdim" -> parseBoxDim(item.getChildNodes());
			case "maxscale" -> parseMaxScale(item.getChildNodes());
		}
	}

	private void parseMaxScale(NodeList topLevel) throws ParseException {
		Vector3f dim = new Vector3f(-1, -1, -1);
		try {
			for(int i = 0; i < topLevel.getLength(); i++) {
				Node item = topLevel.item(i);
				if(item.getNodeType() == Node.ELEMENT_NODE) {
					String t = item.getNodeName().toLowerCase(Locale.ENGLISH);
					switch(t) {
						case "x" -> dim.x = Float.parseFloat(item.getTextContent());
						case "y" -> dim.y = Float.parseFloat(item.getTextContent());
						case "z" -> dim.z = Float.parseFloat(item.getTextContent());
					}
				}
			}
		} catch(NumberFormatException e) {
			e.printStackTrace();
			throw new ParseException("Cannot read maxScale: Not a float");
		}
		if(dim.x > 0 && dim.y > 0 && dim.z > 0) maxScale = dim;
		else throw new ParseException("Cannot read maxScale: either x, y or z is invalid or missing");
	}

	private void parseBoxDim(NodeList topLevel) throws ParseException {
		Vector3i dim = new Vector3i(-1, -1, -1);
		try {
			for(int i = 0; i < topLevel.getLength(); i++) {
				Node item = topLevel.item(i);
				if(item.getNodeType() == Node.ELEMENT_NODE) {
					String t = item.getNodeName().toLowerCase(Locale.ENGLISH);
					switch(t) {
						case "x" -> dim.x = Integer.parseInt(item.getTextContent());
						case "y" -> dim.y = Integer.parseInt(item.getTextContent());
						case "z" -> dim.z = Integer.parseInt(item.getTextContent());
					}
				}
			}
		} catch(NumberFormatException e) {
			e.printStackTrace();
			throw new ParseException("Cannot read boxDim: Not an integer");
		}
		if(dim.x > 0 && dim.y > 0 && dim.z > 0) {
			this.dim = dim;
		} else {
			throw new ParseException("Cannot read boxDim: either x, y or z is invalid or missing");
		}
	}

	private void parseAttachment(NodeList topLevel) throws ParseException {
		System.err.println("[CREATURE] PARSING: " + topLevel);
		for(int i = 0; i < topLevel.getLength(); i++) {
			Node item = topLevel.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				String t = item.getNodeName().toLowerCase(Locale.ENGLISH);
				switch(t) {
					case "main" -> mainBone = item.getTextContent();
					case "righthand" -> rightHand = item.getTextContent();
					case "lefthand" -> leftHand = item.getTextContent();
					case "rightholster" -> rightHolster = item.getTextContent();
					case "leftholster" -> leftHolster = item.getTextContent();
					case "rightbackholster" -> rightBackHolster = item.getTextContent();
					case "leftbackholster" -> leftBackHolster = item.getTextContent();
					case "held" -> heldBone = item.getTextContent();
					case "head" -> headBone = item.getTextContent();
					case "upperbody" -> upperBody = item.getTextContent();
					case "neck" -> neck = item.getTextContent();
					case "rightcollarbone" -> rightCollarBone = item.getTextContent();
					case "leftcollarbone" -> leftCollarBone = item.getTextContent();
					case "rightforearm" -> rightForeArm = item.getTextContent();
					case "leftforearm" -> leftForeArm = item.getTextContent();
					default -> throw new ParseException("AttachmentBone invalid: " + t);
				}
			}
		}
	}

	public enum PartType {
		BOTTOM, MIDDLE, TOP
	}
}
