package org.schema.game.common.util.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.schema.schine.resource.FileExt;

public class CodeClass extends CodeNode {
	public ArrayList<CodeNode> nodes = new ArrayList<CodeNode>();
	private int endPoints;
	private int endPointsWritten;

	@Override
	public void create() {

		if (name.equals("Animation")) {
			createIndex();
		}

		File cDir = new FileExt("./generatedCode/");
		cDir.mkdir();
		File cFile = new FileExt("./generatedCode/" + name + ".java");

		StringBuffer sb = new StringBuffer();

		sb.append("package " + packageName + ";\n\n");
		for (String imp : importName) {
			sb.append("import " + imp + ";\n");
		}
		sb.append("\n");

		sb.append("public class " + name + " extends AnimationStructSet{\n\n");

		for (int i = 0; i < nodes.size(); i++) {
			CodeNode codeNode = nodes.get(i);
			sb.append("\t" + codeNode.createMember() + "\n");
		}
		getParser(sb);
		sb.append("\n\n");

		sb.append("}");

		System.err.println("WRITING: " + cFile.getAbsolutePath());
		FileWriter fw;
		try {
			fw = new FileWriter(cFile);
			fw.append(sb.toString());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < nodes.size(); i++) {
			CodeNode codeNode = nodes.get(i);
//			if(!name.equals("AnimationStructure") && !name.equals("Animation") ){
//				codeNode.name = name+codeNode.name;
//			}
			codeNode.create();
		}

	}

	private void createIndex() {

		File cFile = new FileExt("./generatedCode/AnimationIndex.java");

		StringBuffer sb = new StringBuffer();

		sb.append("package " + packageName + ";\n\n");
		for (String imp : importName) {
			sb.append("import " + imp + ";\n");
		}
		sb.append("\n");

		sb.append("public class AnimationIndex{\n\n");
		String memberName = "";
		for (int i = 0; i < nodes.size(); i++) {
			CodeNode codeNode = nodes.get(i);
			createIndexRec(memberName, codeNode, sb);
		}

		sb.append("public static AnimationIndexElement[] animations = new AnimationIndexElement[" + endPoints + "];\n\n");
		sb.append("\tstatic{\n");
		for (int i = 0; i < nodes.size(); i++) {
			CodeNode codeNode = nodes.get(i);
			createIndexRecColl(memberName, codeNode, sb);
		}
		sb.append("\t}\n");
		sb.append("	}\n\n");

		System.err.println("WRITING: " + cFile.getAbsolutePath());
		FileWriter fw;
		try {
			fw = new FileWriter(cFile);
			fw.append(sb.toString());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createIndexRecColl(String memberName, CodeNode codeNode, StringBuffer sb) {
		if (codeNode instanceof CodeClass) {
			for (int i = 0; i < ((CodeClass) codeNode).nodes.size(); i++) {
				CodeNode cNode = ((CodeClass) codeNode).nodes.get(i);
				createIndexRecColl(memberName + (memberName.isEmpty() ? codeNode.normalName.toUpperCase(Locale.ENGLISH) : ("_" + codeNode.normalName.toUpperCase(Locale.ENGLISH))), cNode, sb);
			}
		} else {
			createTravCol((CodeEndpoint) codeNode, memberName, sb);
		}
	}

	private void createIndexRec(String memberName, CodeNode codeNode, StringBuffer sb) {
		if (codeNode instanceof CodeClass) {
			for (int i = 0; i < ((CodeClass) codeNode).nodes.size(); i++) {
				CodeNode cNode = ((CodeClass) codeNode).nodes.get(i);
				createIndexRec(memberName + (memberName.isEmpty() ? codeNode.normalName.toUpperCase(Locale.ENGLISH) : ("_" + codeNode.normalName.toUpperCase(Locale.ENGLISH))), cNode, sb);
			}
		} else {
			createTrav((CodeEndpoint) codeNode, memberName, sb);
		}
	}

	private void createTrav(CodeEndpoint p, String memberName, StringBuffer sb) {
		p.travName = memberName + "_" + p.normalName.toUpperCase(Locale.ENGLISH);
		sb.append("public static final AnimationIndexElement " + p.travName + " = new AnimationIndexElement() {\n");
		sb.append("	\n");
		sb.append("	@Override\n");
		sb.append("	public AnimationStructEndPoint get(AnimationStructure root) {\n");
		sb.append("		return " + p.getRecLine("root") + ";\n");
		sb.append("	}\n");
		sb.append("	@Override\n");
		sb.append("	public boolean isType(Class<? extends AnimationStructSet> clazz) {\n");

		sb.append("		" + p.getTypeRec("clazz") + "\n");
		sb.append("		return false;\n");

		sb.append("	}\n");
		sb.append("	@Override\n");
		sb.append("	public String toString() {\n");

		sb.append("		return \"" + p.travName + "\";\n");

		sb.append("	}\n");
		sb.append("};\n");
		endPoints++;
	}

	private void createTravCol(CodeEndpoint p, String memberName, StringBuffer sb) {
		p.travName = memberName + "_" + p.normalName.toUpperCase(Locale.ENGLISH);
		sb.append("\tanimations[" + endPointsWritten + "] = " + p.travName + ";\n");
		endPointsWritten++;
	}

	private void getParser(StringBuffer sb) {
		//		sb.append("public void parse(Node node, String def){\n");
		//		sb.append(
		//			"NodeList topLevel = root.getChildNodes();\n"+
		//			"for(int i = 0; i < topLevel.getLength(); i++){\n"+
		//				"Node item = topLevel.item(i);\n"+
		//				"if(item.getNodeType() == Node.ELEMENT_NODE){\n"+
		//					"parseAnimation(item);\n"+
		//				"}\n"+
		//			"}\n"
		//			);
		//		sb.append("}\n\n");

		sb.append("\tpublic void parseAnimation(Node node, String def){\n");

		sb.append("\t\tif(node != null){\n\n");
		for (int i = 0; i < nodes.size(); i++) {
			sb.append("\t\t\tif(node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().toLowerCase(Locale.ENGLISH).equals(\"" + nodes.get(i).normalName.toLowerCase(Locale.ENGLISH) + "\")){\n" +
					"\t\t\t\t" + nodes.get(i).getMemberName() + ".parse(node, def, this);\n" +
					"\t\t\t}\n");
		}
		sb.append("\t\t}\n\n");

		sb.append("\t}\n\n");

		sb.append("\tpublic void checkAnimations(String def){\n");

		for (int i = 0; i < nodes.size(); i++) {
			sb.append("\t\tif(!" + nodes.get(i).getMemberName() + ".parsed){\n" +
					"\t\t\tif(AnimationStructSet.DEBUG){\nSystem.err.println(\"[PARSER] not parsed: " + nodes.get(i).getMemberName() + "\");\n}\n" +
					"\t\t\t" + nodes.get(i).getMemberName() + ".parse(null, def, this);\n" +
					"\t\t}\n");
			sb.append("\t\tchildren.add(" + nodes.get(i).getMemberName() + ");\n\n");
		}

		sb.append("\t}\n\n");
	}

}
