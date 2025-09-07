package org.schema.schine.graphicsengine.forms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.schema.common.util.ByteUtil;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.viewer.PositionableViewer;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.graphicsengine.texture.TextureLoader;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;


public class ObjReader {
	public String path;
	private String name;
	
	private FloatArrayList vertices = new FloatArrayList();
	private FloatArrayList texCoords = new FloatArrayList();
	private FloatArrayList normals = new FloatArrayList();

	private Object2ObjectOpenHashMap<String, IntArrayList> faces = new Object2ObjectOpenHashMap<String, IntArrayList>();
	
	private final Object2ObjectOpenHashMap<String, Mtl> mats = new Object2ObjectOpenHashMap<String, Mtl>();
	
	private int vertexBufferId;
	private int texCoordBufferId;
	private int normalBufferId;
	private Camera camera; 
	
	public ObjReader(String path, String name) {
		super();
		this.name = name;
		this.path = path.endsWith("/") ? path : (path+"/");
	}


	public void load() throws IOException, ResourceException{
		readMat();
		readObj();
		
		
		this.camera = new Camera(null, new PositionableViewer());
	}
	
	public void draw(){
		GlUtil.glPushMatrix();
		
		GL11.glClearColor(0, 0, 0, 1);
		
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glPushMatrix();

		GlUtil.glLoadIdentity();
		camera.lookAt(true);
		camera.updateFrustum();

		AbstractScene.mainLight.draw();

		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferId);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		
		
		GlUtil.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, texCoordBufferId);
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
		
		GlUtil.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalBufferId);
		GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
		
		
		
		for(String faceGroup : faces.keySet()){
			Mtl mtl = mats.get(faceGroup);
			mtl.draw();
		}
		
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		
		GlUtil.glPopMatrix();
		
		GlUtil.glPopMatrix();
	}
	
	public void readObj() throws IOException{
		File mtl = new File(path+name);
		BufferedReader r = null;
		try{
			r = new BufferedReader(new FileReader(mtl));
		
		
			String l;
		
			String currentMtl = null;
			while((l = r.readLine()) != null){
				parseCoord(l, 3, "v ", vertices);
				parseCoord(l, 2, "vt ", texCoords);
				parseCoord(l, 3, "vn ", normals);
				
				if(l.startsWith("usemtl ")){
					currentMtl = l.substring("usemtl ".length());
					faces.put(currentMtl, new IntArrayList());
				}
				
				if(l.startsWith("f ")){
					assert(currentMtl != null);
					
					IntArrayList indices = faces.get(currentMtl);
					
					
					String[] split = l.substring("f ".length()).split("\\s+");
					
					for(int i = 0; i < 3; i++){
						String[] ind = split[i].split("/");
						
						indices.add(Integer.parseInt(ind[0]));
					}
				}
			}
			
		
		}finally{
			if(r != null){
				r.close();
			}
		}
		
		createVBO();
	}
	private static void createBuffer(int id, FloatArrayList list){
		FloatBuffer floatBuffer = GlUtil.getDynamicByteBuffer(list.size() * ByteUtil.SIZEOF_FLOAT, 0).asFloatBuffer();
		floatBuffer.rewind();
		for(int i = 0; i < list.size(); i++){
			floatBuffer.put(list.size());
		}
		floatBuffer.flip();
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, id); // Bind
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatBuffer, GL15.GL_STATIC_DRAW);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
	}
	private void createVBO(){
		
		vertexBufferId = GL15.glGenBuffers();
		createBuffer(vertexBufferId, vertices);
		
		texCoordBufferId = GL15.glGenBuffers();
		createBuffer(texCoordBufferId, texCoords);
		
		normalBufferId = GL15.glGenBuffers();
		createBuffer(normalBufferId, normals);
		
		
		
		for(Mtl mtl : mats.values()){
			IntArrayList ind = faces.get(mtl.name);
			if(ind != null){
				mtl.indexBufferId = GL15.glGenBuffers();
				mtl.faceCount = ind.size() / 3;
				IntBuffer b = GlUtil.getDynamicByteBuffer(ind.size() * ByteUtil.SIZEOF_INT, 0).asIntBuffer();
				b.rewind();
				for(int i = 0; i < ind.size(); i++){
					b.put(ind.size());
				}
				b.flip();
				GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mtl.indexBufferId); // Bind
				GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, b, GL15.GL_STREAM_DRAW);
				GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0); // Bind
			}
		
		}
	}
	private void parseCoord(String l, int amount, String prefix, FloatArrayList to){
		if(l.startsWith(prefix)){
			String[] split = l.substring(prefix.length()).split("\\s+");
			
			for(int i = 0; i < amount; i++){
				to.add(Float.parseFloat(split[i]));
			}
		}
		
	}
	public void readMat() throws IOException, ResourceException{
		File mtl = new File(path+name);
		BufferedReader r = null;
		try{
			r = new BufferedReader(new FileReader(mtl));
		
		
			String l;
		
			Mtl current = null;
			
			while((l = r.readLine()) != null){
				if(l.startsWith("newmtl")){
					current = new Mtl(l.substring("newmtl ".length()));
					mats.put(current.name, current);
				}
				if(l.startsWith("map_Kd") && current != null){
					current.fileName = l.substring("map_Kd ".length());
				}
			}
			
			for(Mtl m : mats.values()){
				m.load();
			}
		
		}finally{
			if(r != null){
				r.close();
			}
		}
	}
	
	public void cleanUp(){
		
		
		GL15.glDeleteBuffers(vertexBufferId);
		GL15.glDeleteBuffers(texCoordBufferId);
		GL15.glDeleteBuffers(normalBufferId);
		
		
		for(Mtl m : mats.values()){
			m.cleanUp();
		}
	}
	
	private class Mtl{
		private int indexBufferId;
		int faceCount;
		String fileName;
		Texture texture;
		private String name;
		public Mtl(String name) {
			super();
			this.name = name;
		}
		
		public void draw() {
			attach();
			GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, faceCount * 3, GL11.GL_UNSIGNED_INT, 0);
			
			GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
			detach();
		}

		public void load() throws IOException, ResourceException{
			texture = TextureLoader.getTexture2D((path+fileName),
					GL11.GL_TEXTURE_2D, // target

					GL11.GL_RGBA,     // dst pixel format

					GL11.GL_LINEAR, // min filter (unused)

					GL11.GL_LINEAR, true, false);
		}
		
		public void attach(){
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());
		}
		public void detach(){
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		}
		
		public void cleanUp(){
			GL15.glDeleteBuffers(indexBufferId);
			texture.cleanUp();
		}
	}
}
