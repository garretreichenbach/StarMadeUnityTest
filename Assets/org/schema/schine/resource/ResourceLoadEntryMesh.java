package org.schema.schine.resource;

import org.schema.common.util.data.DataUtil;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationStructure;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

public class ResourceLoadEntryMesh extends ResourceLoadEntry {

	private final File file;
	public AnimationStructure animation;
	public CreatureStructure creature;
	public String physicsMesh;
	public TextureStructure texture;

	public ResourceLoadEntryMesh(String name, File f) {
		super(name);
		file = f;
	}

	@Override
	public LoadEntryType getType() {
		return LoadEntryType.MESH;
	}

	@Override
	protected void loadResource(ResourceLoader resourceLoader) throws IOException {
		resourceLoader.meshLoader.loadMesh(name, file, animation, texture, creature, physicsMesh);
	}

	@Override
	public String getFilePath() {
		return file.getAbsolutePath();
	}

	public static ResourceLoadEntryMesh parseFromXML(Node model, String rootPath, String meshType) throws IOException {
		NamedNodeMap modelAttributes = model.getAttributes();
		Node fileNameAttrib = modelAttributes.getNamedItem("filename");
		Node relPathAttrib = modelAttributes.getNamedItem("relpath");

		Node physicsMeshItem = modelAttributes.getNamedItem("physicsmesh");
		String physicsMesh = null;
		if(physicsMeshItem != null) physicsMesh = physicsMeshItem.getNodeValue();
		else if("collision".equals(meshType.toLowerCase(Locale.ENGLISH))) physicsMesh = "dedicated";
		String fileName = fileNameAttrib.getNodeValue();
		String completePath = rootPath;
		if(relPathAttrib != null) completePath += relPathAttrib.getNodeValue();
		if(!completePath.endsWith("/")) completePath += "/";

		File file = new File(DataUtil.dataPath + completePath + fileName + ".scene");
		if(!file.exists()) throw new FileNotFoundException("Can't find mesh scene file: " + file.getAbsolutePath());
		ResourceLoadEntryMesh entry = new ResourceLoadEntryMesh(model.getNodeName(), file);
		entry.physicsMesh = physicsMesh;

		NodeList modelSubNodes = model.getChildNodes();
		for(int x = 0; x < modelSubNodes.getLength(); x++) {
			Node modelSubItem = modelSubNodes.item(x);
			if(modelSubItem.getNodeType() == Node.ELEMENT_NODE) {
				//				System.err.println("[MODEL] MODEL HAS SUBNODES: "+model.getNodeName()+"->"+modelSubItem.getNodeName());
				switch(modelSubItem.getNodeName().toLowerCase(Locale.ENGLISH)) {
					case "animation" -> {
						System.err.println("[MODEL] CREATING ANIMATION STRUCTURE FOR " + model.getNodeName());
						NamedNodeMap aAttributes = modelSubNodes.item(x).getAttributes();
						Node afileNameItem = aAttributes.getNamedItem("default");
						String def = "STATIC_DEFAULT";
						if(afileNameItem != null) def = afileNameItem.getNodeValue();
						entry.animation = new AnimationStructure();
						entry.animation.parse(modelSubNodes.item(x), def, null);
					}
					case "texture" -> {
						System.err.println("[MODEL] CREATING TEXTURE STRUCTURE FOR " + model.getNodeName());
						entry.texture = TextureStructure.parse(modelSubNodes.item(x));
					}
					case "creature" -> {
						System.err.println("[MODEL] CREATING CREATURE STRUCTURE FOR " + model.getNodeName());
						entry.creature = CreatureStructure.parse(modelSubNodes.item(x));
					}
				}
			}
		}

		return entry;
	}
}
