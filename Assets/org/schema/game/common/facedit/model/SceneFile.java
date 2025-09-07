package org.schema.game.common.facedit.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.schema.common.XMLTools;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SceneFile {
	public File sceneFile;
	public List<SceneNode> sceneNodes = new ObjectArrayList<SceneNode>();
	public Document doc;
	
	
	public SceneFile(File f, String materialFileName) throws SAXException, IOException, ParserConfigurationException, ResourceException {
		parse(f, materialFileName);
	}
	public SceneFile() {
		
	}
	public String getSceneName() {
		return sceneFile.getName().substring(0, sceneFile.getName().lastIndexOf("."));
	}
	
	public void parse(File f, String materialFileName) throws SAXException, IOException, ParserConfigurationException, ResourceException {
		this.sceneFile = f;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(sceneFile));
			Document root = db.parse(bufferedInputStream);
			this.doc = root;
			bufferedInputStream.close();
			parseRec(root.getDocumentElement(), materialFileName);
		}catch(SAXException r) {
			System.err.println("ERROR IN FILE: "+f.getAbsolutePath());
			throw r;
		}catch(IOException r) {
			System.err.println("ERROR IN FILE: "+f.getAbsolutePath());
			throw r;
		}
		
		
		
	}
	private void parseRec(Node r, String materialFileName) throws ResourceException, IOException {
		if(r.getNodeType() != Node.ELEMENT_NODE) {
			return;
		}
		if(r.getNodeName().toLowerCase(Locale.ENGLISH).equals("node")) {
			SceneNode node = new SceneNode(this);
			node.parse(r, materialFileName);
			sceneNodes.add(node);
			return;
		}
		
		NodeList childNodes = r.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			parseRec(childNodes.item(i), materialFileName);
		}
	}
	public void save(boolean duplicated) throws IOException, ResourceException, ParserConfigurationException, TransformerException {
		for(SceneNode n : sceneNodes) {
			n.save(duplicated);
		}
		
		XMLTools.writeDocument(sceneFile, doc);
	}
}
