package org.schema.game.client.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.CubeBufferFloat;
import org.schema.game.client.view.cubes.CubeBufferInt;
import org.schema.game.client.view.cubes.CubeData;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.CubeMeshBufferContainerPool;
import org.schema.game.client.view.cubes.lodshapes.LodDraw;
import org.schema.game.client.view.cubes.occlusion.Occlusion;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.shader.CubeMeshQuadsShader13;
import org.schema.game.common.controller.SegmentBufferIteratorInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentData4Byte;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.MeshGroup;
import org.schema.schine.graphicsengine.meshimporter.XMLOgreParser;
import org.schema.schine.graphicsengine.texture.Material;
import org.schema.schine.resource.FileExt;

import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.FloatArrayList;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Segment2ObjWriter implements Runnable, SegmentBufferIteratorInterface{
	
	private Occlusion occlusionData;
	private SegmentController root;
	
	private int objectIndex = -1;
	
	public static final String DEFAULT_PATH = "modelexport";
	
	private final List<FloatArrayList> vertices = new ObjectArrayList<FloatArrayList>();
	private final List<FloatArrayList> normals = new ObjectArrayList<FloatArrayList>();
	private final List<FloatArrayList> texcoords = new ObjectArrayList<FloatArrayList>();
	private final List<ByteArrayList> textureIndex = new ObjectArrayList<ByteArrayList>();

	
	private final ByteList layers = new ByteArrayList();
	private final File file;
	private final File mtlFile;
	
	private final Transform wtRoot;
	private final Transform wtRootInv;
	public String name;
	private static Vector3f[] preNormals = new Vector3f[]{
		new Vector3f(0,0,0),
		Element.DIRECTIONSf[0],
		Element.DIRECTIONSf[1],
		Element.DIRECTIONSf[2],
		Element.DIRECTIONSf[3],
		Element.DIRECTIONSf[4],
		Element.DIRECTIONSf[5],
	};
	private static boolean running;
	private Vector3f chunkPos = new Vector3f();
	private Vector3f cubePos = new Vector3f();
	private Vector3f mm = new Vector3f();
	private Vector3f mExtra = new Vector3f();
	private Vector3f shift = new Vector3f();
	private Vector3f vPos = new Vector3f();
	private Vector3f P = new Vector3f();
	private Vector3f vertexPos = new Vector3f();
	private Vector3f normal = new Vector3f();
	private Vector2f adip = new Vector2f();
	private Vector2f texCoords = new Vector2f();
	private Vector2f quad = new Vector2f();
	private Vector2f textureCoordinate = new Vector2f();
	private Transform currentWorldTransform = new Transform();
	public String path;
	public boolean done;
	private static final float tiling = 0.0625f;
	private static final float adi = 0.00485f;
	public Segment2ObjWriter(SegmentController c, String path, String name) {
		super();
		initializeOcclusion();
		this.root = c;
		
		wtRoot = new Transform(c.getWorldTransformOnClient());
		wtRootInv = new Transform(c.getWorldTransformOnClient());
		wtRootInv.inverse();
		if(name.endsWith(".obj")){
			name = name.substring(0, name.length()-4);
		}
		this.name = name;
		this.path = path;
		
		File f = new FileExt(path+"/"+name);
		if(f.exists()){
			FileUtil.deleteDir(f);
		}
		f.mkdirs();
		this.file = new FileExt(path+"/"+name+"/"+name+".obj");
		this.mtlFile = new FileExt(path+"/"+name+"/"+name+".mtl");
	}
	public void initializeOcclusion() {
		occlusionData = Occlusion.occluders[Occlusion.occluders.length-1];
	}
	
	@Override
	public void run(){
		running = true;
		System.err.println("################ STARTED WAVEFRONT WRITING #################");
		handleSegmentControllerRec(root);
		
		try {
			System.err.println("################ WRITING DATA ");
			writeData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			System.err.println("################ WRITING MATERIAL ");
			writeMaterial();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.err.println("################ WAVEFRONT WRITING FINISHED #################");
		running = false;
		
		this.done = true;
	}
	
	private void writeMaterial() throws IOException {
		
		
		File[] copyTextures = GameResourceLoader.copyTextures(path+"/"+name);
		
		File f = this.mtlFile;
		BufferedWriter w = null;
		try{
			int totalVertices = 0;
			w = new BufferedWriter(new FileWriter(f));
			w.write("# StarMade Wavefront Obj Export Material");
			w.newLine();
			w.newLine();
			for(int i = 0; i < copyTextures.length; i++){
				if(copyTextures[i] != null){
					File t = copyTextures[i];
					
					w.write("newmtl mtl_"+i);
					w.newLine();
					w.write("Ka 0.000 0.000 0.000");
					w.newLine();
					w.write("Kd 1.000 1.000 1.000");
					w.newLine();
					w.write("Ks 0.800 0.800 0.800");
					w.newLine();
					w.write("Ns 20.000");
					w.newLine();
					w.write("illum 2");
					w.newLine();
					w.write("Ni 1.000000");
					w.newLine();
					w.write("d 1.000000");
					w.newLine();
					w.write("map_Kd "+t.getName());
					w.newLine();
					w.newLine();
				}
			}
			
			for(Entry<String, Byte> e : textureAltMap.entrySet()){
				assert(e.getKey() != null);
				System.err.println("MATERIAL "+e.getKey());
				File t = new FileExt(e.getKey());
				String toName = "LOD_"+e.getValue()+t.getName().substring(t.getName().lastIndexOf("."), t.getName().length());
				File fileTo = new FileExt(path+"/"+name+"/"+toName);
				FileUtil.copyFile(t, fileTo);
				
				w.write("newmtl mtl_"+e.getValue());
				w.newLine();
				w.write("Ka 0.000 0.000 0.000");
				w.newLine();
				w.write("Kd 1.000 1.000 1.000");
				w.newLine();
				w.write("Ks 0.800 0.800 0.800");
				w.newLine();
				w.write("Ns 20.000");
				w.newLine();
				w.write("illum 2");
				w.newLine();
				w.write("Ni 1.000000");
				w.newLine();
				w.write("d 1.000000");
				w.newLine();
				w.write("map_Kd "+toName);
				w.newLine();
				w.newLine();
			}
		}finally{
			if(w != null){
				try{
					w.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	private void writeData() throws IOException {
		
		File f = this.file;
		BufferedWriter w = null;
		try{
			int totalVertices = 0;
			f.createNewFile();
			w = new BufferedWriter(new FileWriter(f));
			w.write("# StarMade Wavefront Obj Export");
			w.newLine();
			w.newLine();
			w.write("mtllib "+name+".mtl");
			w.newLine();
			w.write("o ");
			w.newLine();
			w.newLine();
			w.write("# Vertices");
			w.newLine();
			for(int objIndex = 0; objIndex <= objectIndex; objIndex++){
				FloatArrayList v = this.vertices.get(objIndex);
				ByteArrayList l = this.textureIndex.get(objIndex);
				for(int i = 0; i < v.size(); i+=3){
					w.write("v "+v.get(i)+" "+v.get(i+1)+" "+v.get(i+2)+" 1.0");
					w.newLine();
					totalVertices++;
				}
				for(int i = 0; i < l.size(); i+=3){
					//one per face
					layers.add(l.get(i));
				}
			}
			w.newLine();
			w.newLine();
			w.write("# Texture Coordinates");
			w.newLine();
			int totalTexCoords = 0;
			for(int objIndex = 0; objIndex <= objectIndex; objIndex++){
				FloatArrayList v = this.texcoords.get(objIndex);
				
				for(int i = 0; i < v.size(); i+=2){
					w.write("vt "+v.get(i)+" "+v.get(i+1));
					w.newLine();
					totalTexCoords++;
				}
			}
			w.newLine();
			w.newLine();
			w.write("# Normals");
			w.newLine();
			int totalNormals = 0;
			for(int objIndex = 0; objIndex <= objectIndex; objIndex++){
				FloatArrayList v = this.normals.get(objIndex);
				
				for(int i = 0; i < v.size(); i+=3){
					w.write("vn "+v.get(i)+" "+v.get(i+1)+" "+v.get(i+2));
					w.newLine();
					totalNormals++;
				}
			}
			
			w.newLine();
			w.newLine();
			w.write("# Faces");
			w.newLine();
			
			assert(totalVertices == totalNormals):"TT "+totalVertices+" / "+totalNormals;
			assert(totalVertices == totalTexCoords):"TT "+totalVertices+" / "+totalTexCoords;
			
			byte currentTexture = -1;
			for(int i = 0; i < totalVertices; i+=3){
				byte tex = layers.getByte(i/3);
				if(tex != currentTexture){
					w.write("usemtl mtl_"+tex);
					w.newLine();
					currentTexture = tex;
				}
				int a = (i+1);
				int b = (i+2);
				int c = (i+3);
//				//wavefront obj face indices start at 1 :(
				w.write("f "+a+"/"+a+"/"+a+" "+b+"/"+b+"/"+b+" "+c+"/"+c+"/"+c);
				w.newLine();
			}
			
			
			
		}finally{
			if(w != null){
				try{
					w.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	public void handleSegmentControllerRec(SegmentController c){
		handleSegmentController(c);
		for(RailRelation r : c.railController.next){
			handleSegmentControllerRec(r.docked.getSegmentController());
		}
	}
	public void handleSegmentController(SegmentController c){
		System.err.println("################ HANDLING "+c);
		synchronized(c.getState()){
			currentWorldTransform.set(c.getWorldTransform());
		}
		c.getSegmentBuffer().iterateOverNonEmptyElement(this, true);
	}
	@Override
	public boolean handle(Segment s, long lastChanged) {
		
		
		handleSeg((DrawableRemoteSegment) s);
		
		return true;
	}
	private void handleSeg(DrawableRemoteSegment nextUpdatedSegment){
		
		CubeData cubeOptOptMesh = null;
		CubeMeshBufferContainer containerFromPool = null;
		try{
			cubeOptOptMesh = SegmentDrawer.dataPool.getMesh(nextUpdatedSegment);
			containerFromPool = CubeMeshBufferContainerPool.get();
			compute(nextUpdatedSegment, containerFromPool, cubeOptOptMesh, 0);
			
			write(nextUpdatedSegment, containerFromPool, cubeOptOptMesh, currentWorldTransform);
			
		}finally{
			if(cubeOptOptMesh != null){
				SegmentDrawer.dataPool.release(cubeOptOptMesh);
			}
			if(containerFromPool != null){
				CubeMeshBufferContainerPool.release(containerFromPool);
			}
		}
	}
	
	private LodDraw[] lodDraws = new LodDraw[4096];
	{
		for(int i = 0; i < lodDraws.length; i++){
			lodDraws[i] = new LodDraw();
		}
	}
	private void write(DrawableRemoteSegment nextUpdatedSegment,
			CubeMeshBufferContainer containerFromPool,
			CubeData cubeOptOptMesh, Transform worldTransform) {
		
		
		objectIndex++;
		int end = containerFromPool.dataBuffer.totalPosition();
		
		containerFromPool.dataBuffer.make();
		containerFromPool.dataBuffer.getTotalBuffer().flip();
		
		final int valueCount = end/CubeMeshBufferContainer.vertexComponents;
		
		
		
		
		
		final int vertexCount = valueCount*3;
		final int normalCount = valueCount*3;
		final int texCoordCount = valueCount*2;
		
		setVertices(new FloatArrayList());
		setNormals(new FloatArrayList());
		setTexcoords(new FloatArrayList());
		setTextureIndex(new ByteArrayList());
		
		int index = 0;
		for(int i = 0; i < valueCount; i++){
			int x;
			int y;
			int z;
			int w;
			
			if(containerFromPool.dataBuffer instanceof CubeBufferInt){
				x = ((CubeBufferInt)containerFromPool.dataBuffer).totalBuffer.get();
				y = ((CubeBufferInt)containerFromPool.dataBuffer).totalBuffer.get();
				z = ((CubeBufferInt)containerFromPool.dataBuffer).totalBuffer.get();
				w = ((CubeBufferInt)containerFromPool.dataBuffer).totalBuffer.get();
			}else{
				x = (int)((CubeBufferFloat)containerFromPool.dataBuffer).totalBuffer.get();
				y = (int)((CubeBufferFloat)containerFromPool.dataBuffer).totalBuffer.get();
				z = (int)((CubeBufferFloat)containerFromPool.dataBuffer).totalBuffer.get();
				w = (int)((CubeBufferFloat)containerFromPool.dataBuffer).totalBuffer.get();
			}
			
//			System.err.println("INDEX ::: "+i+"; "+i*3+" / "+vertexCount+"; "+getVertices().length);
			
			if(softwareShader(x,y,z,w, i, nextUpdatedSegment, worldTransform)){
				index++;
			}
		}
		int lodPointer = 0;
		
		{
			SegmentData c = nextUpdatedSegment.getSegmentData();
			
			Transform tmpTrns = new Transform();
			Vector3i pos = new Vector3i(nextUpdatedSegment.pos);
			
			if(c != null && c.drawingLodShapes != null){
				final int sz = c.drawingLodShapes.size();
				for(int i = 0; i < sz; i++){
					
					int lodInfoIndex = c.drawingLodShapes.get(i);
					if(lodPointer == lodDraws.length){
						int oldLen = lodDraws.length;
						lodDraws = Arrays.copyOf(lodDraws, lodDraws.length*2);
						for(int h = oldLen; h < lodDraws.length; h++){
							lodDraws[h] = new LodDraw();
						}
					}
					LodDraw lodDraw = lodDraws[lodPointer];
					assert(lodDraw != null);
					lodDraw.lightingAndPos = c.getLodData();
					lodDraw.pointer = i * SegmentData.lodDataSize;
					lodDraw.type = c.getLodTypeAndOrientcubeIndex()[i*2];
					if(lodDraw.type == 0 || !ElementKeyMap.getInfoFast(lodDraw.type).hasLod()){
						lodDraw.faulty = true;
					}else{
						lodDraw.faulty = false;
						ElementInformation info = ElementKeyMap.getInfoFast(lodDraw.type);
						lodDraw.mesh = info.getModel(0, false);
						lodDraw.meshDetail = info.getModel(0, true);
						assert(lodDraw.mesh != null);
						
						
						short orientation = c.getLodTypeAndOrientcubeIndex()[i*2+1];
						Oriencube oc = (Oriencube) BlockShapeAlgorithm.algorithms[5][orientation];
						
						if(info.getId() == 104 ){
							int o = orientation%6;
							oc = Oriencube.getOrientcube(
									o, o > 1 ? Element.FRONT : Element.TOP);
						}
						
						
						
						lodDraw.transform.setIdentity();
						
						tmpTrns.set(oc.getBasicTransform());
						SegmentData.getPositionFromIndexWithShift(lodInfoIndex, pos, tmpTrns.origin);
						
						Matrix4fTools.transformMul(lodDraw.transform, tmpTrns);
					}
					lodPointer++;
				}
			}
		}
		try {
			if(lodPointer > 0){
				Arrays.sort(lodDraws, 0, lodPointer);
				
				
				Transform tt = new Transform();
				for(int i = 0; i < lodPointer; i++){
					LodDraw c = lodDraws[i];
					if(c.faulty){
						continue;
					}
					ElementInformation info = ElementKeyMap.getInfo(c.type);
					
					if(!recordedMeshes.containsKey(info.lodShapeString)){
						Mesh mesh = c.mesh;
						
						XMLOgreParser p = new XMLOgreParser();
						p.recordArrays = true;
						MeshGroup meshGroup;
							assert(mesh.scenePath != null);
							meshGroup = p.parseScene(mesh.scenePath, mesh.sceneFile);
						
						
						recordedMeshes.put(info.lodShapeString, meshGroup);
					}
					if(info.lodShapeStringActive != null && info.lodShapeStringActive.length() > 0 && !recordedMeshes.containsKey(info.lodShapeStringActive)){
						Mesh mesh = c.meshDetail;
						
						XMLOgreParser p = new XMLOgreParser();
						p.recordArrays = true;
						MeshGroup meshGroup;
						assert(mesh.scenePath != null);
						meshGroup = p.parseScene(mesh.scenePath, mesh.sceneFile);
						
						
						recordedMeshes.put(info.lodShapeStringActive, meshGroup);
					}
					Mesh rootMesh = recordedMeshes.get(info.lodShapeString);
					Mesh mesh = (Mesh) rootMesh.getChilds().get(0);
					
					tt.set(worldTransform);
					tt.mul(c.transform);
					
					for(int mIn : mesh.recordedIndices){
						Vector3f vert = new Vector3f(mesh.recordedVectices.get(mIn));
						Vector3f norm = new Vector3f(mesh.recordedNormals.get(mIn));
						Vector2f te = new Vector2f(mesh.recordedTextcoords.get(mIn));
						
						tt.transform(vert);
						tt.basis.transform(norm);
						
						wtRootInv.transform(vert);
						wtRootInv.basis.transform(norm);
						
						
						getVertices().add(vert.x);
						getVertices().add(vert.y);
						getVertices().add(vert.z);
						
						getNormals().add(norm.x);
						getNormals().add(norm.y);
						getNormals().add(norm.z);
						
						getTexcoords().add(te.x);
						getTexcoords().add(1.0f-te.y);
						Material mat = mesh.getMaterial();
						String path = mesh.scenePath+mat.getTextureFile();
						
						if(!textureAltMap.containsKey(path)){
							
							textureAltMap.put(path, altTextureIndex);
							altTextureIndex--;
							if(altTextureIndex > 0){
								throw new IllegalArgumentException("Byte overflow");
							}
						}
						byte tex = textureAltMap.getByte(path);
						getTextureIndex().add(tex);
						
					}
				}
				
			}
		} catch (ResourceException e) {
			e.printStackTrace();
		}
		
	}
	private Object2ByteOpenHashMap<String> textureAltMap = new Object2ByteOpenHashMap<String>();
	private byte altTextureIndex = -1;
	private Object2ObjectOpenHashMap<String, Mesh> recordedMeshes = new Object2ObjectOpenHashMap<String, Mesh>();
	private boolean softwareShader(int x, int y, int z, int w, final int index, Segment seg, Transform worldTransform) {
		int indexInfo = x;
		
		float vIndex = ((indexInfo ) & 32767);
		
		float red =  ((indexInfo >> 16) & 15);
		
		float green =  ((indexInfo >> 20) & 15);
		
		float blue =  ((indexInfo >> 24) & 15);
		
		
		int info = y;
		
		float vertNumCodeE = info & 3;
		
		int sideId =  (info >> 2) & 7; 
		
		float layerE =  (info >> 5) & 7;

		float xyScaleManip =  (info >> 8) & 1;
		
		float typeE =  (info >> 9) & 255 ;
		
		float hitPointsE =  (info >> 17) & 7 ;
		
		float animatedE = (info >> 20) & 1;
		
		float extFlag =  (info >> 21) & 3;

		boolean onlyInBuildMode =  ((info >> 23) & 1) > 0.0;
		
		
		int normalIndex = sideId;
		
		Vector3f qpm = CubeMeshQuadsShader13.quadPosMark[normalIndex];
		
		
		
		
		
		float oreOverlayE = 0.0f;
		float occE = 0.0f;
		float eleE = 0.0f;
		float eleEV = 0.0f;
		
		int pCode = (z);
		chunkPos.set(pCode & 255, (pCode >> 8) & 255, (pCode >> 16) & 255);
		
		int eInfoW = w;
		
		
		eleEV =  (eInfoW >> 22) & 3 ;
		
		eleE =  (eInfoW >> 20) & 3 ;
		
		occE = (eInfoW >> 16) & 15; 
		
		oreOverlayE =  (eInfoW >> 10) & 63;
		
		
		Vector3f normalPos = preNormals[normalIndex+1];
		
		if((eInfoW & 511) == 0){
			normal.set(normalPos);
		}else{
			normal.set( preNormals[(eInfoW >> 6) & 7]);
			normal.add( preNormals[(eInfoW >> 3) & 7]);
			normal.add( preNormals[eInfoW & 7]);
			normal.normalize();
		}
		float type = typeE;

		
		
		
		float mVertNumQuarters = (vertNumCodeE) * 0.25f;
		float mTex = (extFlag) * 0.25f;
		
		
		int layer = (int) layerE;		
		
//		type += animatedE * animationTime;
		
		int typeI = (int)(type); 
		float xIndex = typeI & 15;
		float yIndex = typeI >> 4; // / 16.0
		
		int vIndexI = (int)vIndex;
		cubePos.set(
				((vIndexI     ) & 31), 
				((vIndexI >> 5) & 31),
				((vIndexI >> 10) & 31));
		
		vertexPos.set(cubePos.x - SegmentData.SEG_HALF, cubePos.y - SegmentData.SEG_HALF, cubePos.z - SegmentData.SEG_HALF);
		
		quad.set(
				(((int)(mTex * 2.0f)) & 1), 
				(((int)(mTex * 4.0f)) & 1)
				);
		//either 0,0; 0,1; 1,1; 1,0
		float eleVertEdge = (eleEV*0.25f);
		
		mm.set((((int)(mVertNumQuarters * qpm.x)) & 1),
				(((int)(mVertNumQuarters * qpm.y)) & 1),
				(((int)(mVertNumQuarters * qpm.z)) & 1));
		
		mExtra.set(
				(((qpm.x == 4.0f && xyScaleManip > 0.5f) || (qpm.x == 2.0 && xyScaleManip < 0.5f)) ? eleVertEdge * (mm.x > 0.0f ? 1.0f : -1.0f) : 0.0f),
				(((qpm.y == 4.0f && xyScaleManip > 0.5f) || (qpm.y == 2.0 && xyScaleManip < 0.5f)) ? eleVertEdge * (mm.y > 0.0f ? 1.0f : -1.0f) : 0.0f),
				(((qpm.z == 4.0f && xyScaleManip > 0.5f) || (qpm.z == 2.0 && xyScaleManip < 0.5f)) ? eleVertEdge * (mm.z > 0.0f ? 1.0f : -1.0f) : 0.0f));
		
	
		P.set(	((((-0.5f) - (Math.abs(normalPos.x) * -0.5f)))  + mm.x - mExtra.x) ,
						((((-0.5f) - (Math.abs(normalPos.y) * -0.5f)))  + mm.y - mExtra.y) ,
						((((-0.5f) - (Math.abs(normalPos.z) * -0.5f)))  + mm.z - mExtra.z) );
		
		
		vertexPos.x += P.x + ((normalPos.x * 0.5f) - (eleE * normalPos.x * 0.25f));	
		vertexPos.y += P.y + ((normalPos.y * 0.5f) - (eleE * normalPos.y * 0.25f));	
		vertexPos.z += P.z + ((normalPos.z * 0.5f) - (eleE * normalPos.z * 0.25f));	
		
		
		shift.set( ByteUtil.divU256(ByteUtil.divUSeg(seg.pos.x) + 128) * 256,
					ByteUtil.divU256(ByteUtil.divUSeg(seg.pos.y) + 128) * 256,
					ByteUtil.divU256(ByteUtil.divUSeg(seg.pos.z) + 128) * 256);
		
		
		vertexPos.x += ((chunkPos.x - (128.0f)) + shift.x)*SegmentData.SEGf;
		vertexPos.y += ((chunkPos.y - (128.0f)) + shift.y)*SegmentData.SEGf;
		vertexPos.z += ((chunkPos.z - (128.0f)) + shift.z)*SegmentData.SEGf;
		
		
		
//		vPos = gl_ModelViewMatrix * vec4(vertexPos,1.0); 
		vPos.set(vertexPos); 
		
		float adiNorm = adi;// + maxadiplus * min(1.0, (max(0.0, length(vPos) - 60.0)*0.065));
		adip.set(
		((1.0f -((quad.x)*adiNorm)) + (Math.abs(quad.x - 1.0f)*adiNorm)), 
		((1.0f -((quad.y)*adiNorm)) + (Math.abs(quad.y - 1.0f)*adiNorm)));

		texCoords.set(quad.x *tiling, quad.y *tiling );

		
		
		
//		if(oreOverlayE > 0.0){
//			gl_TexCoord[2].st = vec2(texCoords.x + 0.0625 * mod((oreOverlayE - 1.0), 16.0), texCoords.y + 0.0625 * (oreOverlayStartingRow + floor((oreOverlayE-1.0) * 0.0625))); //floor(oreOverlayE/16);
//		}else{
//			gl_TexCoord[2].st = vec2(0.9);
//		}
//		if(hitPointsE > 0.0){
//			gl_TexCoord[1].st = vec2(texCoords.x + 0.0625 * (hitPointsE - 1.0), texCoords.y); //hit ovelays are in the first row
//		}else{
//			gl_TexCoord[1].st = vec2(0.9);
//		}
		
		
		textureCoordinate.set( texCoords.x + tiling * xIndex, texCoords.y + tiling * yIndex);


		
		
		textureCoordinate.x += adip.x;
		textureCoordinate.y += adip.y;
//		gl_TexCoord[1].st += adip;
//		gl_TexCoord[2].st += adip;
		
		//note: this is a directional light! 
		
		if(onlyInBuildMode){
			return false;
		}
		
		bufferVertex(vPos, textureCoordinate, normal, layer, index, worldTransform);
		
		return true;
	}
	
	
	private void bufferVertex(final Vector3f vertex, final Vector2f texCoord,
			final Vector3f normal, final int layer, final int index, Transform worldTransform) {
		
		worldTransform.transform(vertex);
		worldTransform.basis.transform(normal);
		
		wtRootInv.transform(vertex);
		wtRootInv.basis.transform(normal);
		
		
		getVertices().add(vertex.x);
		getVertices().add(vertex.y);
		getVertices().add(vertex.z);
		
		getNormals().add(normal.x);
		getNormals().add(normal.y);
		getNormals().add(normal.z);
		
		getTexcoords().add(texCoord.x);
		getTexcoords().add(1.0f-texCoord.y); //flip y coord (obj format?)
		
		getTextureIndex().add((byte) layer);
		
		
	}
	private void resetOcclusion(SegmentData data, CubeMeshBufferContainer containerFromPool) {
		if (data.getSize() <= 0) {
			return;
		}
		assert (data != null);
		occlusionData.reset((SegmentData4Byte) data, containerFromPool);
	}

	private void compute(DrawableRemoteSegment nextUpdatedSegment, CubeMeshBufferContainer containerFromPool, CubeData cubeOptOptMesh, int tries) {
		if (!nextUpdatedSegment.isEmpty() && nextUpdatedSegment.getSegmentData() != null) {
			SegmentData segmentData = nextUpdatedSegment.getSegmentData();
			long t = System.currentTimeMillis();
			segmentData.rwl.readLock().lock();
			try {
				resetOcclusion(segmentData, containerFromPool);

				occlusionData.compute((SegmentData4Byte) segmentData, containerFromPool);

				cubeOptOptMesh.createIndex(segmentData,
						containerFromPool);
				
			} finally {
				segmentData.rwl.readLock().unlock();
			}
		}
	}
	public FloatArrayList getVertices() {
		return vertices.get(objectIndex);
	}
	public void setVertices(FloatArrayList vertices) {
		this.vertices.add(vertices);
	}
	public FloatArrayList getNormals() {
		return normals.get(objectIndex);
	}
	public void setNormals(FloatArrayList normals) {
		this.normals.add(normals);
	}
	public FloatArrayList getTexcoords() {
		return texcoords.get(objectIndex);
	}
	public void setTexcoords(FloatArrayList texcoords) {
		this.texcoords.add(texcoords);
	}
	public ByteArrayList getTextureIndex() {
		return textureIndex.get(objectIndex);
	}
	public void setTextureIndex(ByteArrayList textureIndex) {
		this.textureIndex.add(textureIndex);
	}
	public static boolean isRunning() {
		return running ;
	}
	
}
