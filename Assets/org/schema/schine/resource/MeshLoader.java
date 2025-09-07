package org.schema.schine.resource;

import api.listener.events.register.MeshLoadEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.mod.annotations.DoesNotWork;
import org.apache.commons.io.IOUtils;
import org.schema.common.util.data.ResourceUtil;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationStructure;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.MeshGroup;
import org.schema.schine.graphicsengine.meshimporter.XMLOgreParser;
import org.schema.schine.graphicsengine.texture.Texture;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MeshLoader {

	/**
	 * The with vbo.
	 */
	public static boolean loadVertexBufferObject = true;
	/**
	 * The mesh map.
	 */
	private final Map<String, Mesh> meshMap = new HashMap<String, Mesh>();
	private final Map<String, Texture> textureMap = new HashMap<String, Texture>();
	public MeshLoader(ResourceUtil resourceUtil) {
	}

	/**
	 * Add3ds mesh.
	 *
	 * @param creature
	 * @param texture
	 * @return true, if successful
	 * @throws IOException
	 * @
	 */
	public Mesh loadMesh(String name, File file, AnimationStructure anim, TextureStructure texture, CreatureStructure creature, String physicsMesh) throws IOException {
		return addMesh(name, file, anim, texture, creature, physicsMesh, false);
	}
	//INSERTED CODE
	public Mesh getModMesh(StarMod mod, String name){
		return meshMap.get(getModNamespace(mod, name));
	}
	public boolean loadModMesh(StarMod mod, String name, InputStream zippedFile, String physicsMesh) throws ResourceException, IOException {
		System.err.println("Loading Mod mesh...");
		ZipInputStream zip = new ZipInputStream(zippedFile);

		String dir = "models/modmodels/"+mod.getName() + "/" + name + "/"; //second / has to be here for materials to work
		String outputDir = "data/" + dir;
		new File(outputDir).mkdirs();
		while (true) {
			ZipEntry entry = zip.getNextEntry();
			if (entry == null) {
				break;
			}
			File entryDestination = new File(outputDir, entry.getName());
			if (entry.isDirectory()) {
				entryDestination.mkdirs();
			} else {
				entryDestination.getParentFile().mkdirs();
				OutputStream out = new FileOutputStream(entryDestination);
				//Apparently getNextEntry will position the start at the next entry, then handle EOF by itself,
				// So we will see if this works
				IOUtils.copy(zip, out);
			}
		}
		zip.close();
		System.err.println("Mod mesh copied");

		File meshFile = new File(dir);
		addMesh(getModNamespace(mod, name), meshFile, null, null, null, physicsMesh, true); //TODO: Ithirahad: @JakeV this may be borken; I just made it not throw an error for now.
		System.err.println("Added mod mesh");
		return true;
	}
	private static String getModNamespace(StarMod mod, String name){
		return mod.getName() + "~" + name;
	}

	@DoesNotWork
	public void loadModObjMesh(StarMod mod, String name){
		try {
			Mesh mesh = Mesh.loadObj("display_screen.obj");
			this.meshMap.put(getModNamespace(mod, name), mesh);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	///
	/**
	 * Add3ds mesh.
	 *
	 * @param creature
	 * @param texture
	 * @return true, if successful
	 * @throws IOException
	 * @
	 */
	private Mesh addMesh(String name, File file, AnimationStructure animation, TextureStructure texture, CreatureStructure creature, String physicsMesh, boolean overwrite) throws IOException {

		if (!overwrite && meshMap.containsKey(name)) {
			return (MeshGroup) meshMap.get(name);
		}
		XMLOgreParser p = new XMLOgreParser();
		String path = file.getParentFile().getCanonicalPath()+File.separator;
		MeshGroup m = p.parseScene(path, file.getName());

		m.setName(name);
		if (loadVertexBufferObject) {
			for (AbstractSceneNode sm : m.getChilds()) {
				if (sm instanceof Mesh) {
					try {
						while (!sm.isLoaded()) {

							Mesh.buildVBOs((Mesh) sm);

							if (physicsMesh != null && physicsMesh.equals("convexhull")) {
								((Mesh) sm).loadPhysicsMeeshConvexHull();
							}
							if (physicsMesh != null && physicsMesh.equals("dedicated")) {
								((Mesh) sm).retainVertices();
							}
						}
					} catch (Exception e) {
						System.err.println("error in " + name);
						e.printStackTrace();
					}
				}
			}
		}else {
			//retain vertices otherwise
			for (AbstractSceneNode sm : m.getChilds()) {
				if (sm instanceof Mesh) {
					if (physicsMesh != null && physicsMesh.equals("dedicated")) {
						((Mesh) sm).retainVertices();
					}
				}
			}
		}

		GlUtil.LOCKDYN = false;
		GlUtil.locked.clear();
		if(!ResourceLoader.dedicatedServer) {
			assignMaterials(m, path);
		}

		meshMap.put(name, m);
		//INSERTED CODE
			MeshLoadEvent meshLoadEvent = new MeshLoadEvent(name, file.getName(), path, physicsMesh, m, this);
			StarLoader.fireEvent(meshLoadEvent, false);
			///
			return m;
	}

	/**
	 * assigns the materials that are in material.texturefile to a OGRE mesh and
	 * recursivly calls the method on its own childs
	 *
	 * @param m    the m
	 * @param path the path
	 * @throws ResourceException the resource exception
	 */
	private void assignMaterials(Mesh m, String path)
			throws ResourceException {
		if (m.getMaterial().isMaterialTextured()) {

			if (m.getMaterial().getTextureFile() != null) {// if object has a
				//				File f = new FileExt(DataUtil.dataPath + path
				//				+ m.getMaterial().getTextureFile());
				//				if(f.exists()){
				try {
					m.getMaterial().texturePathFull = path
							+ m.getMaterial().getTextureFile();
					
//					System.err.println("PARSING "+m.getMaterial().getTextureFile()+"; in path "+path+" for "+m.getName());
					
					
					Texture texture = textureMap.get(m.getMaterial().texturePathFull);
					if(texture == null){
						texture = Controller.getTexLoader().getTexture2D(m.getMaterial().texturePathFull, true);
						textureMap.put(m.getMaterial().texturePathFull, texture);
					}else{
					}
					
					m.getMaterial().setTexture(texture);
					//				}else{
					//					throw new ResourceException("Error assigning TextureNew to "+m+". The Image File "+f.getAbsolutePath()+" does not exist!");
					//				}
					String normalFile = m.getMaterial().getTextureFile().replace(".", "_normal.");
					String specularFile = m.getMaterial().getTextureFile().replace(".", "_specular.");

					
					if((new File(path + normalFile)).exists()){
						m.getMaterial().normalTexturePathFull = path + normalFile;
						Texture nTexture = textureMap.get(m.getMaterial().normalTexturePathFull);
						if(nTexture == null){
							nTexture = Controller.getTexLoader().getTexture2D(m.getMaterial().normalTexturePathFull, true);
							textureMap.put(m.getMaterial().normalTexturePathFull, nTexture);
						}
						m.getMaterial().setNormalMap(nTexture);
						m.getMaterial().setMaterialBumpMapped(true);
					}
					
					if((new File(path + specularFile)).exists()){
						m.getMaterial().specularTexturePathFull = path + specularFile;
						Texture nTexture = textureMap.get(m.getMaterial().specularTexturePathFull);
						if(nTexture == null){
							nTexture = Controller.getTexLoader().getTexture2D(m.getMaterial().specularTexturePathFull, true);
							textureMap.put(m.getMaterial().specularTexturePathFull, nTexture);
						}
					
					
						m.getMaterial().setSpecularMap(nTexture);
						m.getMaterial().setSpecularMapped(true);
					}
				} catch (IOException e) {
					System.err.println("ERROR LOADING: " + path
							+ m.getMaterial().getTextureFile());
					e.printStackTrace();
				}
			}
			if (m.getMaterial().getEmissiveTextureFile() != null) {
				try {

					m.getMaterial().setEmissiveTexture(Controller.getTexLoader().getTexture2D(path
							+ m.getMaterial().getEmissiveTextureFile(), true));

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (!m.getMaterial().isMaterialBumpMapped() && m.getMaterial().getNormalTextureFile() != null) {
				try {
					String normalPath = path + m.getMaterial().getNormalTextureFile();
					
					Texture normal = Controller.getTexLoader().getTexture2D(normalPath, true);
					assert(normal != null);
					m.getMaterial().setNormalMap(normal);
					m.getMaterial().setMaterialBumpMapped(true);
					
					assert(m.getMaterial().getNormalMap() != null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		for (AbstractSceneNode kid : m.getChilds()) {
			if (kid instanceof Mesh) {
				Mesh mKid = (Mesh) kid;
				assignMaterials(mKid, path);
			}
		}

	}

	/**
	 * @return the meshMap
	 */
	public Map<String, Mesh> getMeshMap() {
		return meshMap;
	}

}
