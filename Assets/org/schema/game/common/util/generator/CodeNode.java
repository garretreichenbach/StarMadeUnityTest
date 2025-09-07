package org.schema.game.common.util.generator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.schema.common.util.data.DataUtil;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.resource.FileExt;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class CodeNode {
	public static final String packageName = "org.schema.schine.graphicsengine.animation.structure.classes";
	public static final ArrayList<String> importName = new ArrayList<String>();
	public static int idGen = 0;

	static {
		importName.add("org.w3c.dom.Node");
		importName.add("java.util.Locale");
	}

	public String normalName;
	public CodeNode parent;
	public String name;

	public static void main(String[] asfd) throws ParserConfigurationException, SAXException, IOException {

		FileUtil.deleteRecursive(new FileExt("./generatedCode/"));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(DataUtil.dataPath + File.separator + DataUtil.configPath));
		Document root = db.parse(bufferedInputStream);
		bufferedInputStream.close();

		NodeList a = root.getChildNodes();

		for (int i = 0; i < a.getLength(); i++) {
			NodeList childNodes = a.item(i).getChildNodes();
			System.err.println(a.item(i).getNodeName());

			for (int j = 0; j < childNodes.getLength(); j++) {
				Node item = childNodes.item(j);

				//				System.err.println(item.getNodeName());
				if (item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals("Character")) {

					System.err.println("------------>");
					NodeList charNodes = item.getChildNodes();
					for (int c = 0; c < charNodes.getLength(); c++) {
						Node subNode = charNodes.item(c);
						System.err.println(subNode.getNodeName());
						if (subNode.getNodeType() == Node.ELEMENT_NODE && subNode.getNodeName().equals("PlayerMdl")) {
							NodeList ccd = subNode.getChildNodes();
							for (int v = 0; v < ccd.getLength(); v++) {
								Node subNode2 = ccd.item(v);
								if (subNode2.getNodeType() == Node.ELEMENT_NODE && subNode2.getNodeName().equals("Animation")) {
									CodeClass parse = AnimationStructureCodeCreator.parse(subNode2);
									parse.create();
								}
							}
						}
					}
					System.err.println("<------------");
				}
			}
		}
		(new FileExt("./generatedCode/AnimationStructure.java")).delete();

		FileUtil.copyFile(new FileExt("./generatedCode/Animation.java"), new FileExt("./generatedCode/AnimationStructure.java"));

		(new FileExt("./generatedCode/Animation.java")).delete();
		//		a.getChildNodes();
		//
		//		AnimationStructureCodeCreator.parse(a);
		//		for(int i = 0; i < childNodes.getLength(); i++){
		//			Node item = childNodes.item(i);
		//			if(item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().toLowerCase(Locale.ENGLISH).equals("animation")){
		//
		//			}
		//		}
	}

	public String getExecRec(String top, String prog) {
		assert (!(this instanceof CodeEndpoint) || parent != null) : this + "; " + this.parent;
		if (parent == null || parent.name.equals("AnimationStructure")) {
			return top + prog;
		} else {
			assert (!getMemberName().equals("animation")) : this.name + "; " + this.parent.name;
			return parent.getExecRec(top, "." + getMemberName() + prog);
		}
	}

	public String getTypeRec(String paramName) {
		if (parent == null || parent.name.equals("AnimationStructure")) {
			return "";
		} else {
			return "if(" + paramName + ".equals(" + name + ".class)){ return true; }\n\t\t" + parent.getTypeRec(paramName);
		}

	}

	public abstract void create();

	public String createMember() {
		return "public final " + name + " " + getMemberName() + " = new " + name + "();";
	}

	public String getMemberName() {
		return name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
	}
}
