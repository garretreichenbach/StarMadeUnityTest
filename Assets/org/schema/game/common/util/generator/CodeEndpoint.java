package org.schema.game.common.util.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.schema.schine.resource.FileExt;

public class CodeEndpoint extends CodeNode {

	public String travName;

	@Override
	public void create() {
		File cDir = new FileExt("./generatedCode/");
		cDir.mkdir();
		File cFile = new FileExt("./generatedCode/" + name + ".java");

		StringBuffer sb = new StringBuffer();

		sb.append("package " + packageName + ";\n\n");
		for (String imp : importName) {
			sb.append("import " + imp + ";\n");
		}
		sb.append("\n");

		sb.append("public class " + name + " extends AnimationStructEndPoint{\n\n");

		//			sb.append("public final ArrayList<String> animations = new ArrayList<String>();\n");
		sb.append("\n\n");

		sb.append("\tpublic AnimationIndexElement getIndex(){\n");
		sb.append("\t\treturn AnimationIndex." + travName + ";\n");

		sb.append("\t}\n");
		sb.append("}");

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

	public String getRecLine(String top) {
		return getExecRec(top, "");
	}

}
