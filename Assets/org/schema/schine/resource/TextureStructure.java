package org.schema.schine.resource;

import java.io.IOException;
import java.util.Locale;

import org.schema.common.util.data.DataUtil;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.texture.Texture;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class TextureStructure {

	private final ObjectArrayList<String> diffuseMapNames = new ObjectArrayList<String>();
	private final ObjectArrayList<String> normalMapNames = new ObjectArrayList<String>();
	private final ObjectArrayList<String> colorMapNames = new ObjectArrayList<String>();
	private final ObjectArrayList<String> specularMapNames = new ObjectArrayList<String>();
	private final ObjectArrayList<String> emissionMapNames = new ObjectArrayList<String>();

	private final ObjectArrayList<Texture> diffuseMaps = new ObjectArrayList<Texture>();
	private final ObjectArrayList<Texture> normalMaps = new ObjectArrayList<Texture>();
	private final ObjectArrayList<Texture> colorMaps = new ObjectArrayList<Texture>();
	private final ObjectArrayList<Texture> specularMaps = new ObjectArrayList<Texture>();
	private final ObjectArrayList<Texture> emissionMaps = new ObjectArrayList<Texture>();
	public static TextureStructure parse(Node root) {
		TextureStructure t = new TextureStructure();
		NodeList topLevel = root.getChildNodes();
		for (int i = 0; i < topLevel.getLength(); i++) {
			Node item = topLevel.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
//				System.err.println("[ANIMATION] PARSING TEX: "+root.getNodeName()+"->"+item.getNodeName());
				t.parseTexture(item);
//				parseTopLevel(item, s);
			}
		}

		return t;
	}

	protected void parseTextureItems(Node root, ObjectArrayList<String> toList) {

		NodeList topLevel = root.getChildNodes();
		for (int i = 0; i < topLevel.getLength(); i++) {
			Node item = topLevel.item(i);
//			System.err.println("[Texture] ITEM "+root.getNodeName()+"->"+item.getNodeName());
			if (item.getNodeType() == Node.ELEMENT_NODE) {

				String name = item.getTextContent();
				toList.add(name);
			}
		}
	}

	private void parseTexture(Node item) {
		String t = item.getNodeName().toLowerCase(Locale.ENGLISH);
		if (t.equals("diffuse")) {
			parseTextureItems(item, diffuseMapNames);
		} else if (t.equals("normalmap")) {
			parseTextureItems(item, normalMapNames);
		} else if (t.equals("specular")) {
			parseTextureItems(item, specularMapNames);
		} else if (t.equals("colormap")) {
			parseTextureItems(item, colorMapNames);
		} else if (t.equals("emission")) {
			parseTextureItems(item, emissionMapNames);
		}
	}

	private void load(ObjectArrayList<String> p, ObjectArrayList<Texture> to, String path) throws IOException {
		for (int i = 0; i < p.size(); i++) {
			to.add(Controller.getTexLoader().getTexture2D(DataUtil.dataPath + path
					+ p.get(i), true));
		}
	}

	public void load(String path) throws IOException {
		if(ResourceLoader.dedicatedServer) {
			return;
		}
		load(diffuseMapNames, diffuseMaps, path);
		load(normalMapNames, normalMaps, path);
		load(colorMapNames, colorMaps, path);
		load(specularMapNames, specularMaps, path);
		load(emissionMapNames, emissionMaps, path);
	}

	/**
	 * @return the diffuseMaps
	 */
	public ObjectArrayList<Texture> getDiffuseMaps() {
		return diffuseMaps;
	}

	/**
	 * @return the diffuseMapNames
	 */
	public ObjectArrayList<String> getDiffuseMapNames() {
		return diffuseMapNames;
	}
}
