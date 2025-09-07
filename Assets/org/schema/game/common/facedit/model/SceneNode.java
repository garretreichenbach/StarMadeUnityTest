package org.schema.game.common.facedit.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Locale;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.resource.ResourceLoader;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SceneNode {

	private SceneFile sceneFile;

	public SceneNode(SceneFile sceneFile) {
		this.sceneFile = sceneFile;
	}

	public String name;
	public String nameNew;
	
	public Vector3f position = new Vector3f();
	public Vector3f scale = new Vector3f(1,1,1);
	public Vector4f rotation = new Vector4f();

	public String positionNewX;
	public String positionNewY;
	public String positionNewZ;
	public String scaleNewX;
	public String scaleNewY;
	public String scaleNewZ;
	public String rotationNewX;
	public String rotationNewY;
	public String rotationNewZ;
	public String rotationNewW;
	
	public MeshEntity entity;

	Node posNode;

	Node scaleNode;

	Node rotNode;

	Node entityNode;
	private Node nameAtt;
	
	
	public static class MeshEntity{
		
		public SceneNode scene;
		public String name;
		public String meshFile;
		public MaterialEntity material;
		private Node nameAtt;
		private Node meshFileAtt;

		public void parse(Node r, String materialFileName) throws ResourceException, IOException {
			this.nameAtt = r.getAttributes().getNamedItem("name");
			this.meshFileAtt = r.getAttributes().getNamedItem("meshFile");
			name = nameAtt.getNodeValue();			
			meshFile = meshFileAtt.getNodeValue();		
			
			
			NodeList childNodes = r.getChildNodes();
			for(int i = 0; i < childNodes.getLength(); i++) {
				Node item = childNodes.item(i);
				parseRec(item, materialFileName);
			}
		}
		private void parseRec(Node r, String materialFileName) throws ResourceException, IOException {
			if(r.getNodeType() != Node.ELEMENT_NODE) {
				return;
			}
			String nn = r.getNodeName().toLowerCase(Locale.ENGLISH);
			NamedNodeMap at = r.getAttributes();
			
			if(nn.equals("subentity") && at.getNamedItem("materialName") != null) {
				this.material = new MaterialEntity(at.getNamedItem("materialName").getNodeValue(), scene);
				this.material.parse(materialFileName);
			}
			NodeList childNodes = r.getChildNodes();
			for(int i = 0; i < childNodes.getLength(); i++) {
				Node item = childNodes.item(i);
				parseRec(item, materialFileName);
			}
		}
		public void save(boolean duplicated) throws IOException, ResourceException {
			if(material != null) {
				material.save(duplicated);
			}
		}
	}
	
	public static class MaterialEntity{
		public SceneNode scene;
		public Vector4f ambient;
		public Vector4f diffuse;
		public Vector4f specular;
		public MaterialEntity(String materialName, SceneNode scene) {
			this.materialName = materialName;
			this.scene = scene;
		}
		
		public void parse(String materialFileName) throws ResourceException, IOException {
			fileName = scene.sceneFile.sceneFile.getParentFile().getAbsolutePath()+File.separator+materialFileName+".material";
			parse();
		}
		private void parse() throws ResourceException, IOException {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					ResourceLoader.resourceUtil.getResourceAsInputStream(fileName)));
			
			String l;
			while((l = br.readLine()) != null) {
				l = l.trim();
				if(l.startsWith("ambient")) {
					String[] split = l.substring("ambient ".length(), l.length()).trim().split(" ");
					ambient = new Vector4f(Float.parseFloat(split[0]) , Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3]));
				}else if(l.startsWith("diffuse")) {
					String[] split = l.substring("diffuse ".length(), l.length()).trim().split(" ");
					diffuse = new Vector4f(Float.parseFloat(split[0]) , Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3]));
				}else if(l.startsWith("specular")) {
					String[] split = l.substring("specular ".length(), l.length()).trim().split(" ");
					specular = new Vector4f(Float.parseFloat(split[0]) , Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3]));
				}else if(l.startsWith("texture ")) {
					String texturePath = l.substring("texture ".length(), l.length());
					if(texturePath.toLowerCase(Locale.ENGLISH).contains("_em.")) {
						emissionTexture = texturePath;
					}else if(texturePath.toLowerCase(Locale.ENGLISH).contains("_nrm.")) {
						normalTexture = texturePath;
					}else {
						diffuseTexture = texturePath;
					}
				}
			}
			br.close();
		}
		public String fileName;
		public String materialName;
		
		public String diffuseTexture;
		public String normalTexture;
		public String emissionTexture;

		public String diffuseTextureNew;
		public String normalTextureNew;
		public String emissionTextureNew;
		
		
		public void save(boolean duplicated) throws IOException, ResourceException {
			String correctFileName = scene.sceneFile.sceneFile.getParentFile().getAbsolutePath()+File.separator+scene.sceneFile.getSceneName()+".material";
			if(!fileName.equals(correctFileName)) {
				//file was duplicated and needs to be copied
				
				File oldFile = new File(fileName);
				File newFile = new File(correctFileName);
				if(newFile.exists()) {
					newFile.delete();
				}
				FileUtil.copyFile(oldFile, newFile);
			}
			if(diffuseTextureNew == null && normalTextureNew == null && emissionTextureNew == null) {
				//no need to write
				return;
			}
			
			
			//update file name
			fileName = correctFileName;
			
			StringBuffer b = new StringBuffer();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
					ResourceLoader.resourceUtil.getResourceAsInputStream(fileName)));
			
			boolean newDiff = (diffuseTexture == null || diffuseTexture.trim().length() == 0) && diffuseTextureNew != null && diffuseTextureNew.length() > 0;
			boolean newNorm = (normalTexture == null || normalTexture.trim().length() == 0) && normalTextureNew != null && normalTextureNew.length() > 0;
			boolean newEmis = (emissionTexture == null || emissionTexture.trim().length() == 0) && emissionTextureNew != null && emissionTextureNew.length() > 0;
			
			String l;
			
			
			boolean passStarted = false;
			int passStart = 0;
			boolean passCompleted = false;
			while((l = br.readLine()) != null) {
				
				if(l.trim().equals("pass")) {
					passStarted = true; 
				}
				
				if(passStarted) {
					if(l.trim().equals("{")) {
						passStart++;
					}else if(l.trim().equals("}")) {
						passStart--;
						if(passStart == 0) {
							passCompleted = true;
						}
					}
				}
				
				if(l.trim().equals("texture_unit")) {
					StringBuffer unit = new StringBuffer();
					unit.append(l+"\n");
					
					boolean removeTextureBlock = false;
					while((l = br.readLine()) != null) {
						if(emissionTextureNew != null && l.contains(emissionTexture)) {
							if(emissionTextureNew.trim().length() == 0) {
								removeTextureBlock = true;
							}else {
								l = l.replace(emissionTexture, emissionTextureNew);
							}
							emissionTextureNew = null;
						}else if(normalTextureNew != null && l.contains(normalTexture)) {
							if(normalTextureNew.trim().length() == 0) {
								removeTextureBlock = true;
							}else {
								l = l.replace(normalTexture, normalTextureNew);
							}
							normalTextureNew = null;
						}else if(diffuseTextureNew != null && l.contains(diffuseTexture)) {
							if(diffuseTextureNew.trim().length() == 0) {
								removeTextureBlock = true;
							}else {
								l = l.replace(diffuseTexture, diffuseTextureNew);
							}
							diffuseTextureNew = null;
						}
						unit.append(l+"\n");
						if(l.contains("}")) {
							break;
						}
					}
					if(!removeTextureBlock) {
						b.append(unit.toString());
					}
				}else {
					if(passCompleted) {
						//append new textures here 
						if(newDiff) {
							b.append("\t\t\ttexture_unit\n");
							b.append("\t\t\t{\n");
							b.append("\t\t\t\ntexture "+diffuseTextureNew+"\n");
							b.append("\t\t\t}\n");
							b.append("\n");
						}
						if(newNorm) {
							b.append("\t\t\ttexture_unit\n");
							b.append("\t\t\t{\n");
							b.append("\t\t\t\ntexture "+normalTextureNew+"\n");
							b.append("\t\t\t}\n");
							b.append("\n");
						}
						if(newEmis) {
							b.append("\t\t\ttexture_unit\n");
							b.append("\t\t\t{\n");
							b.append("\t\t\t\ntexture "+emissionTextureNew+"\n");
							b.append("\t\t\t}\n");
							b.append("\n");
						}
						
						b.append(l+"\n");
						
					}else {
						b.append(l+"\n");
					}
				}
				
				
			}
			br.close();
			File old = new File(fileName);
			if(!old.delete()) {
				throw new IOException("File could not be deleted: "+old.getAbsolutePath());
			}
			
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			BufferedReader r = new BufferedReader(new StringReader(b.toString()));
			
			l = null;
			
			while((l = r.readLine()) != null) {
				out.write(l);
				out.newLine();
			}
			r.close();
			out.flush();
			out.close();
			
			//re-read all values
			parse(); 
		}
		
	}
	public void save(boolean duplicated) throws IOException, ResourceException {
		if(
				positionNewX == null && scaleNewX == null && rotationNewX == null &&
				positionNewY == null && scaleNewY == null && rotationNewY == null &&
				positionNewZ == null && scaleNewZ == null && rotationNewZ == null && 
				rotationNewW == null && nameNew == null
				
				) {
			//no need to write
		}else {
			if(nameNew != null && nameNew.trim().length() > 0) {
				name = nameNew;
				nameAtt.setNodeValue(name);
				nameNew = null;
			}
			
			if(positionNewX != null) {
				NamedNodeMap at = posNode.getAttributes();
				try {
					position.x = Float.parseFloat(positionNewX.trim());
					at.getNamedItem("x").setNodeValue(String.valueOf(position.x));
				}catch(Exception r) {
					r.printStackTrace();
				}
				positionNewX = null;
			}
			if(positionNewY != null) {
				NamedNodeMap at = posNode.getAttributes();
				try {
					position.y = Float.parseFloat(positionNewY.trim());
					at.getNamedItem("y").setNodeValue(String.valueOf(position.y));
				}catch(Exception r) {
					r.printStackTrace();
				}
				positionNewY = null;
			}
			if(positionNewZ != null) {
				NamedNodeMap at = posNode.getAttributes();
				try {
					position.z = Float.parseFloat(positionNewZ.trim());
					at.getNamedItem("z").setNodeValue(String.valueOf(position.z));
				}catch(Exception r) {
					r.printStackTrace();
				}
				positionNewZ = null;
			}
			
			
			if(scaleNewX != null) {
				NamedNodeMap at = scaleNode.getAttributes();
				try {
					scale.x = Float.parseFloat(scaleNewX.trim());
					at.getNamedItem("x").setNodeValue(String.valueOf(scale.x));
				}catch(Exception r) {
					r.printStackTrace();
				}
				scaleNewX = null;
			}
			if(scaleNewY != null) {
				NamedNodeMap at = scaleNode.getAttributes();
				try {
					scale.y = Float.parseFloat(scaleNewY.trim());
					at.getNamedItem("y").setNodeValue(String.valueOf(scale.y));
				}catch(Exception r) {
					r.printStackTrace();
				}
				scaleNewY = null;
			}
			if(scaleNewZ != null) {
				NamedNodeMap at = scaleNode.getAttributes();
				try {
					scale.z = Float.parseFloat(scaleNewZ.trim());
					at.getNamedItem("z").setNodeValue(String.valueOf(scale.z));
				}catch(Exception r) {
					r.printStackTrace();
				}
				scaleNewZ = null;
			}
			
			
			
			if(rotationNewX != null) {
				NamedNodeMap at = rotNode.getAttributes();
				try {
					rotation.x = Float.parseFloat(rotationNewX.trim());
					at.getNamedItem("qx").setNodeValue(String.valueOf(rotation.x));
				}catch(Exception r) {
					r.printStackTrace();
				}
				rotationNewX = null;
			}
			if(rotationNewY != null) {
				NamedNodeMap at = rotNode.getAttributes();
				try {
					rotation.y = Float.parseFloat(rotationNewY.trim());
					at.getNamedItem("qy").setNodeValue(String.valueOf(rotation.y));
				}catch(Exception r) {
					r.printStackTrace();
				}
				rotationNewY = null;
			}
			if(rotationNewZ != null) {
				NamedNodeMap at = rotNode.getAttributes();
				try {
					rotation.z = Float.parseFloat(rotationNewZ.trim());
					at.getNamedItem("qz").setNodeValue(String.valueOf(rotation.z));
				}catch(Exception r) {
					r.printStackTrace();
				}
				rotationNewZ = null;
			}
			if(rotationNewW != null) {
				NamedNodeMap at = rotNode.getAttributes();
				try {
					rotation.w = Float.parseFloat(rotationNewW.trim());
					at.getNamedItem("qw").setNodeValue(String.valueOf(rotation.w));
				}catch(Exception r) {
					r.printStackTrace();
				}
				rotationNewW = null;
			}
			
		}
		
		if(entity != null) {
			entity.save(duplicated);
		}
	}
	public void parse(Node r, String materialFileName) throws ResourceException, IOException {
		this.nameAtt = r.getAttributes().getNamedItem("name");
		name = nameAtt.getNodeValue();
		
		NodeList childNodes = r.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			parseRec(item, materialFileName);
		}
	}

	private void parseRec(Node r, String materialFileName) throws ResourceException, IOException {
		if(r.getNodeType() != Node.ELEMENT_NODE) {
			return;
		}
		String nn = r.getNodeName().toLowerCase(Locale.ENGLISH);
		NamedNodeMap at = r.getAttributes();
		if(nn.equals("position")) {
			this.posNode = r;
			float x = Float.parseFloat(at.getNamedItem("x").getNodeValue());
			float y = Float.parseFloat(at.getNamedItem("y").getNodeValue());
			float z = Float.parseFloat(at.getNamedItem("z").getNodeValue());
			position = new Vector3f(x, y, z);
		}else if(nn.equals("scale")) {
			this.scaleNode = r;
			float x = Float.parseFloat(at.getNamedItem("x").getNodeValue());
			float y = Float.parseFloat(at.getNamedItem("y").getNodeValue());
			float z = Float.parseFloat(at.getNamedItem("z").getNodeValue());
			scale = new Vector3f(x, y, z);
		}else if(nn.equals("rotation")) {
			this.rotNode = r;
			float x = Float.parseFloat(at.getNamedItem("qx").getNodeValue());
			float y = Float.parseFloat(at.getNamedItem("qy").getNodeValue());
			float z = Float.parseFloat(at.getNamedItem("qz").getNodeValue());
			float a = Float.parseFloat(at.getNamedItem("qw").getNodeValue());
			rotation = new Vector4f(x, y, z, a);
		}else if(nn.equals("entity")) {
			this.entityNode = r;
			entity = new MeshEntity();
			entity.scene = this;
			entity.parse(r, materialFileName);
			return; //dont further process children here since they will be in the class
		}
		
		NodeList childNodes = r.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			parseRec(item, materialFileName);
		}
	}
	
}
