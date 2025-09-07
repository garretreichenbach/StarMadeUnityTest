package org.schema.common.util.settings;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.XMLTools;
import org.schema.common.util.settings.SettingState.SettingStateValueFac;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public interface Settings {
	
	public static final List<Settings> settingsList = new ObjectArrayList<>();
	
	public static Object2ObjectOpenHashMap<Settings, SettingStateValueFac> stateFac = new Object2ObjectOpenHashMap<Settings, SettingState.SettingStateValueFac>();
	public boolean isOn() ;
	public int getInt() ;
	public float getFloat() ;
	public void setOn(boolean on) ;
	public void setInt(int v) ;
	public void setFloat(float v);
	public String getString() ;
	public void setString(String v) ;
	public Object getObject();
	public void setObject(Object o);

	public String name();
	
	
	public static void read(String path, SettingState[] v, String[] names) throws IOException {
		File f = new File(path);
		if(!f.exists()) {
			throw new FileNotFoundException(path);
		}
		
		Document doc = XMLTools.loadXML(f);
		
		Element root = doc.getDocumentElement();
		
		
		NodeList cn = root.getChildNodes();
		for(int n = 0; n < cn.getLength(); n++) {
			Node item = cn.item(n);
			
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals("setting")) {
					boolean found = false;
					NamedNodeMap attributes = item.getAttributes();
					
					Node namedItem = attributes.getNamedItem("name");
					Node typeItem = attributes.getNamedItem("type");
					
					if(namedItem == null) {
						throw new SettingsParseException(item.getNodeName()+" has no 'name' attribute");
					}
					if(typeItem == null) {
						throw new SettingsParseException(item.getNodeName()+" has no 'type' attribute");
					}
					
					String attName = namedItem.getNodeValue().toLowerCase(Locale.ENGLISH);
					
					for(int i = 0; i < names.length; i++) {
						if(names[i].toLowerCase(Locale.ENGLISH).equals(attName)) {
							found = true;
							v[i].parseXML(item);
							break;
						}
					}
					
					if(!found) {
						System.err.println("[SETTINGS][WARNING] no coresponding config entry found for '"+attName+"'");
					}
				}else {
					throw new SettingsParseException("Invalid node name: "+item.getNodeName());
				}
			}
		}
		
	}
	
	public static void write(String path, SettingState[] v, String[] names, boolean overwrite) throws IOException {
//		System.err.println("WRITING TO PATH "+path);
		
		DocumentBuilderFactory fac = DocumentBuilderFactory.newDefaultInstance();
		
		Document doc;
		try {
			doc = fac.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e1) {
			throw new IOException(e1);
		} 
		
		Element e = doc.createElement("Settings");
		
		doc.appendChild(e);
		
		for(int i = 0; i < v.length; i++) {
			SettingState s = v[i];
			String name = names[i];
			s.addToNode(name, e);
		}
		File f = new File(path);
		f.getParentFile().mkdirs();
		if(overwrite && f.exists()) {
			f.delete();
		}
		if(!f.exists()) {
			try {
				XMLTools.writeDocument(f, doc);
			} catch(ParserConfigurationException ex) {
				throw new RuntimeException(ex);
			} catch(TransformerException ex) {
				throw new RuntimeException(ex);
			}
		}
		
	}
	public static String getSettingsPath() {
		return "."+File.separator+"settings"+File.separator;
	}
}
