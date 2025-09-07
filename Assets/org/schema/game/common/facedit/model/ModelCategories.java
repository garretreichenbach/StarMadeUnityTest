package org.schema.game.common.facedit.model;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.schema.common.XMLTools;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.w3c.dom.Document;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ModelCategories{
	public File file;
	public Document doc;
	public Object2ObjectOpenHashMap<String, ModelCategory> models = new Object2ObjectOpenHashMap<String, ModelCategory>();
	public void clear() {
		models.clear();
	}
	
	public void save() throws IOException, ResourceException, ParserConfigurationException, TransformerException {
		for(ModelCategory e : this.models.values()) {
			e.save();
		}
		XMLTools.writeDocument(file, doc);
	}
}
