package org.schema.game.common.facedit.model;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.schema.common.util.data.DataUtil;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class Model{
	
	public SceneFile scene;
	private String fullPath;
	public File sceneFile;
	public boolean duplicated;
	public ModelCategory cat;
	public final String name;
	public String nameNew;
	public String relpath = ".";
	public String filename;
	public String physicsmesh;
	public Model(String name) {
		this.name = name;
		
	}
	public String calcPath() {
		return "."+File.separator+DataUtil.dataPath+cat.path+File.separator+relpath+File.separator+filename;
	}
	public void initialize(String optionalMaterialFileName) throws SAXException, IOException, ParserConfigurationException, ResourceException {
		scene = new SceneFile();
		this.fullPath = calcPath();
		this.sceneFile = new File(fullPath);
		scene.parse(this.sceneFile, optionalMaterialFileName != null ? optionalMaterialFileName : this.sceneFile.getName().substring(0, this.sceneFile.getName().lastIndexOf(".")));		
	}
	public Model(Model dub, String newName) {
		cat = dub.cat;
		name = newName;
		relpath = dub.relpath;
		filename = dub.filename;
		
		this.duplicated = true;
	}
	
	public void save() throws IOException, ResourceException, ParserConfigurationException, TransformerException {
		System.err.println("Saving Model: "+name+"; "+(duplicated ? "DUPLICATE" : ""));
		if(duplicated) {
			//copy file
			
			String fileNameOld = filename;
			filename = name.toLowerCase(Locale.ENGLISH)+".scene";
			
		}
		File oldSceneFile = this.sceneFile;
		this.fullPath = calcPath();
		this.sceneFile = new File(fullPath);
		if(!this.sceneFile.exists()) {
			System.err.println("scene "+this.sceneFile.getAbsolutePath()+" didn't exist. Creating from copy "+oldSceneFile.getAbsolutePath());
			FileUtil.copyFile(oldSceneFile, this.sceneFile);
		}
		scene.sceneFile = sceneFile;
		scene.save(duplicated);
		
		duplicated = false;
	
	}
	
	
	public int getIndexOf(DefaultMutableTreeNode parent) {
		for(int i = 0; i < parent.getChildCount(); i++) {
			TreeNode childAt = parent.getChildAt(i);
			if(((DefaultMutableTreeNode)childAt).getUserObject() == this) {
				return i;
			}
		}
		System.err.println("NO ORDER "+name);
		throw new RuntimeException("ORDER" +name);
//		return -1;
	}
	public String toString(){
		return (duplicated ? "*" : "")+name;
	}
	
	public class Scene{
		public String texture;
	}

	public Node createNode(Document doc) {
		Element node = doc.createElement(name);
		node.setAttribute("relpath", (relpath == null || relpath.trim().length() == 0) ? "." : relpath);
		node.setAttribute("filename", filename.toLowerCase(Locale.ENGLISH).endsWith(".scene") ? filename.substring(0, filename.lastIndexOf(".")) : filename);
		if(physicsmesh != null) {
			node.setAttribute("physicsmesh", physicsmesh.trim());
		}
		return node;
	}
	
	
}
