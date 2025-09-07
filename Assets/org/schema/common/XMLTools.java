package org.schema.common;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;
import java.util.Locale;

public class XMLTools {
	public static String printParentPath(Node node) {
		if (node.getParentNode() != null) {
			return printParentPath(node.getParentNode()) + " -> " + node.getNodeName();
		} else {
			return node.getNodeName();
		}
	}

	public static Document loadXML(File f) throws IOException{
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;

				dBuilder = dbFactory.newDocumentBuilder();

			BufferedInputStream b = new BufferedInputStream(new FileInputStream(f), 4096);
			Document doc = dBuilder.parse(b);
			b.close();

			return doc;
		} catch (ParserConfigurationException | SAXException e) {
			throw new IOException(e);
		}
	}
	private static boolean hasNormalChilds(Node a){
			NodeList childNodes = a.getChildNodes();
			for(int i = 0; i < childNodes.getLength(); i++){
				Node item = childNodes.item(i);
				if(item.getNodeType() == Node.ELEMENT_NODE ){
					return true;
				}
			}
		return false;
	}
	private static void repRecId(Node a, Node b, final String onAttrib, Document root){
		
		NodeList childNodes = a.getChildNodes();
		NodeList childNodesOther = b.getChildNodes();
		List<Node> replaceWhat = new ObjectArrayList<Node>();
		List<Node> replaceWith = new ObjectArrayList<Node>();
		for(int i = 0; i < childNodes.getLength(); i++){
			Node item = childNodes.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE){
				
				
				for(int j = 0; j < childNodesOther.getLength(); j++){
					Node otherItem = childNodesOther.item(j);
					if(otherItem.getNodeType() == Node.ELEMENT_NODE){
						
						
//						System.err.println("#+#+#+CHECKING WITH "+item.getNodeName()+"; "+item.getAttributes().getNamedItem(onAttrib).getNodeValue());
						if(item.hasAttributes() && otherItem.hasAttributes() && 
								item.getAttributes().getNamedItem(onAttrib) != null && 
								otherItem.getAttributes().getNamedItem(onAttrib) != null && 
								item.getAttributes().getNamedItem(onAttrib).getNodeValue().equals(otherItem.getAttributes().getNamedItem(onAttrib).getNodeValue())) {
							replaceWhat.add(item);
							replaceWith.add(otherItem);
							
						}
					}
				}
			}
		}
		for(int i = 0; i < replaceWhat.size(); i++){
			System.err.println("[CUSTOMXML] replacing node "+printParentPath(replaceWhat.get(i)));
			Node toReplace = replaceWhat.get(i);
			Node replWith = replaceWith.get(i);
			a.removeChild(toReplace);
			Node replWithImportNode = root.importNode(replWith, true);
			a.appendChild(replWithImportNode);
			
			b.removeChild(replWith); //remove so we dont add it double later
		}
		
		//add rest of nodes that aren't replacements
		for(int j = 0; j < childNodesOther.getLength(); j++){
			Node otherItem = childNodesOther.item(j);
			if(otherItem.getNodeType() == Node.ELEMENT_NODE){
				Node replWithImportNode = root.importNode(otherItem, true);
				a.appendChild(replWithImportNode);
			}
		}
	}
	private static void repRec(Node a, Node b, Document root){
		
		NodeList childNodes = a.getChildNodes();
		NodeList childNodesOther = b.getChildNodes();
		List<Node> replaceWhat = new ObjectArrayList<Node>();
		List<Node> replaceWith = new ObjectArrayList<Node>();
		for(int i = 0; i < childNodes.getLength(); i++){
			Node item = childNodes.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE){
				Node namedItem = item.getAttributes().getNamedItem("version");
				if(namedItem != null && namedItem.getNodeValue().toLowerCase(Locale.ENGLISH).equals("noreactor")) {
					System.err.println("IGNORED 'NOREACTOR' versioned NODE");
					continue;
				}

				for(int j = 0; j < childNodesOther.getLength(); j++){
					Node otherItem = childNodesOther.item(j);
					if(otherItem.getNodeType() == Node.ELEMENT_NODE){
						if(!hasNormalChilds(item) && !hasNormalChilds(otherItem) && otherItem.getNodeName().equals(item.getNodeName())){
							replaceWhat.add(item);
							replaceWith.add(otherItem);
							
						}else if(otherItem.getNodeName() != null && otherItem.getNodeName().equals(item.getNodeName())){
							repRec(item, otherItem, root);
						}
					}
				}
			}
		}
		for(int i = 0; i < replaceWhat.size(); i++){
			System.err.println("[CUSTOMXML] replacing node "+printParentPath(replaceWhat.get(i)));
			Node toReplace = replaceWhat.get(i);
			Node replWith = replaceWith.get(i);
			a.removeChild(toReplace);
			Node replWithImportNode = root.importNode(replWith, true);
			a.appendChild(replWithImportNode);
		}
	}

	public static void mergeDocumentOnAttrib(Document root, Document insertDoc, final String attrib) {
		
		if (root != null && insertDoc != null) {
			
			repRecId(root.getDocumentElement(), insertDoc.getDocumentElement(), attrib, root);
		}
		
	}
	public static void mergeDocument(Document root, Document insertDoc) {

	    if (root != null && insertDoc != null) {
	    	
	    	repRec(root.getDocumentElement(), insertDoc.getDocumentElement(), root);
	    }

	}
	public static File writeDocument(File file, Document doc) throws ParserConfigurationException, TransformerException {
			// ///////////////////////////
			// Creating an empty XML Document

			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();

			org.w3c.dom.Element root = doc.createElement("Config");

			doc.setXmlVersion("1.0");

			// create a comment and put it in the root element

			// ///////////////
			// Output the XML

			// set up a transformer
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			// create string from xml tree
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(file);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			// String xmlString = sw.toString();

			// print xml
			// System.out.println("Here's the xml:\n\n" + xmlString);
			return file;
		
	}
}
