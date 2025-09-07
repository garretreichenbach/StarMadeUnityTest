package org.schema.schine.graphicsengine.meshimporter;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.stax2.XMLInputFactory2;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.resource.ResourceLoader;
import org.schema.schine.xmlparser.XMLAttribute;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class StAXDocument {
	/**
	 * Parses the xml.
	 *
	 * @param path the path
	 * @throws ResourceException the resource exception
	 */
	public void parseXML(String path) throws ResourceException {
		parseXML(path, false);
	}
	public void parseXML(String path, boolean zipped) throws ResourceException {
		try {
			
			XMLInputFactory2 factory = (XMLInputFactory2)XMLInputFactory2.newInstance();
			factory.setProperty(
					XMLInputFactory.
				IS_REPLACING_ENTITY_REFERENCES,
					Boolean.FALSE);
			factory.setProperty(
					XMLInputFactory.
			IS_SUPPORTING_EXTERNAL_ENTITIES,
					Boolean.FALSE);
			factory.setProperty(
					XMLInputFactory.
					IS_COALESCING,
					Boolean.FALSE);
			factory.configureForSpeed();
			XMLStreamReader parser;
			if(zipped){
				parser = factory.createXMLStreamReader(new GZIPInputStream( ResourceLoader.resourceUtil
					.getResourceAsInputStream(path)));
			}else{
				parser = factory.createXMLStreamReader(ResourceLoader.resourceUtil
						.getResourceAsInputStream(path));
			}
			
			while (parser.hasNext()) {
				
				int currentEvent = parser.next();
				
				switch (currentEvent) {
					case XMLStreamReader.CHARACTERS:
						current.text = parser.getText();
						break;
					case XMLStreamReader.START_ELEMENT:
						if (root == null) {
							root = new XMLOgreContainer();
							root.name = parser.getLocalName();
							current = root;
						}else{
							
							XMLOgreContainer c = new XMLOgreContainer();
							c.name = parser.getLocalName();
							c.attribs = new ObjectArrayList(parser.getAttributeCount());
							for (int i = 0; i <parser.getAttributeCount(); i++) {
								String aName = parser.getAttributeLocalName(i); // Attr description
								if ("".equals(aName)) {
									aName = parser.getAttributeName(i).getLocalPart();
								}
								c.attribs.add(new XMLAttribute(aName, parser.getAttributeValue(i)));
							}
							current.childs.add(c);
							c.parent = current;
							if (!current.ended) {
								current = c;
							} 
						}
						break;
					case XMLStreamReader.END_ELEMENT:
						if(current.name.equals(parser.getLocalName())){
							current.ended = true;
							if (current.parent != null) {
								current = current.parent;
							}
						}
						break;
				}
			}
			parser.close();
		}  catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new ResourceException(path, e);
		}
	}
	/**
	 * The current.
	 */
	XMLOgreContainer current;
	/**
	 * The parent.
	 */
	XMLOgreContainer parent;

	// ---- SAX DefaultHandler methods ----
	/**
	 * The root.
	 */
	private XMLOgreContainer root;
	
	/**
	 * Gets the root.
	 *
	 * @return the root
	 */
	public XMLOgreContainer getRoot() {
		return root;
	}

	/**
	 * Sets the root.
	 *
	 * @param root the new root
	 */
	public void setRoot(XMLOgreContainer root) {
		this.root = root;
	}
}
