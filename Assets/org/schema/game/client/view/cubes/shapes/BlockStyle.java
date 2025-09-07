package org.schema.game.client.view.cubes.shapes;

import java.util.Locale;

import org.schema.game.common.data.element.ElementParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public enum BlockStyle {
		NORMAL(0, true, false, false, 6, "normal", "A normal 6 sided block", "block"),
		WEDGE(1, false, true, false, 12, "wedge", "A wedged block", "wedge"),
		CORNER(2, false, true, false, 24, "corner", "A corner blcok with a square base", "corner"),
		SPRITE(3, false, false, true, 6, "sprite", "An X-shaped spritelike block", "sprite"),
		TETRA(4, false, true, false, 8, "tetra", "A corner angeled block with a triangle base", "tetra"),
		HEPTA(5, false, true, false, 8, "penta", "A block with a tetra cut off", "hepta"),
		NORMAL24(6, true, false, false, 24, "normal24", "A block with 24 orientation like rails", "24normal"), 
	;
	public final int id;
	public final String[] oldNames;
	public final String realName;
	public String desc;

	public int slab;
	
	public final boolean solidBlockStyle;
	public final boolean blendedBlockStyle;
	public final boolean cube;
	public final int orientations;
	
	public static final String[] ids = getAsStringId();
	public static final String[] names = getAsStringName();
	
	public static final String[] getAsStringId(){
		String[] s = new String[values().length];
		for(int i = 0; i < values().length; i++){
			s[i] = String.valueOf(values()[i].id);
		}
		return s;
	}
	public static final String[] getAsStringName(){
		String[] s = new String[values().length];
		for(int i = 0; i < values().length; i++){
			s[i] = String.valueOf(values()[i].realName);
		}
		return s;
	}
	
	private BlockStyle(int id, boolean normal, boolean solidBlockStyle, boolean blendedBlockStyle, int orientations, String realName, String desc, String ... oldName){
		this.id = id;
		this.oldNames = oldName;
		this.realName = realName;
		this.desc = desc;
		this.cube = normal;
		this.solidBlockStyle = solidBlockStyle;
		this.blendedBlockStyle = blendedBlockStyle;
		this.orientations = orientations;
	}
	public static String getDescs() {
		StringBuffer f = new StringBuffer();
		for(int i = 0; i < values().length; i++){
			f.append(values()[i].realName+": "+values()[i].desc+";\n");
		}
		
		return f.toString();
	}
	public static BlockStyle getById(int blockStyle) throws ElementParserException {
		for(int i = 0; i < values().length; i++){
			if(values()[i].id == blockStyle){
				return values()[i];
			}
		}
		throw new ElementParserException("Block Style not found by id: "+blockStyle);
	}
	public static BlockStyle parse(Element node) {
		NodeList cn = node.getChildNodes();
		for(int i = 0; i < cn.getLength(); i++) {
			Node n = cn.item(i);
			if(n.getNodeType() == Element.ELEMENT_NODE) {
				if(n.getNodeName().toLowerCase(Locale.ENGLISH).equals("styleid")) {
//					Node namedItem = n.getAttributes().getNamedItem("slab");
//					if(namedItem != null) {
//						slab = Integer.parseInt(namedItem.getNodeValue());
//					}
					int id = Integer.parseInt(n.getTextContent());
					for(int c = 0; c < values().length; c++) {
						if(values()[c].id == id) {
							return values()[c];
						}
					}
				}
			}
		}
		throw new RuntimeException("NO STYLE");
	}
	public void write(Element node, Document doc) {
		Element c = doc.createElement("StyleId");
//		Attr a = doc.createAttribute("slab");
//		a.setValue(String.valueOf(slab));
//		c.appendChild(a);
		c.setTextContent(String.valueOf(id));
		node.appendChild(c);
	}
}
