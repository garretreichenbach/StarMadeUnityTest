package org.schema.game.client.view;

import api.StarLoaderHooks;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.opengl.ATIMeminfo;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
import org.schema.common.ParseException;
import org.schema.common.config.ConfigParserException;
import org.schema.common.util.data.DataUtil;
import org.schema.game.client.view.gui.shiphud.newhud.HudConfig;
import org.schema.game.client.view.shader.CubeMeshQuadsShader13.CubeTexQuality;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.explosion.ExplosionRunnable;
import org.schema.game.common.data.player.PlayerSkin;
import org.schema.game.common.util.FolderZipper;
import org.schema.game.common.version.VersionContainer;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.SettingStateParseError;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.GuiConfig;
import org.schema.schine.graphicsengine.shader.ErrorDialogException;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.texture.DDSLoader;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.graphicsengine.texture.textureImp.Texture3D;
import org.schema.schine.resource.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GameResourceLoader extends ResourceLoader {

	public static final String CUSTOM_EFFECT_CONFIG_PATH = "." + File.separator + "customEffectConfig" + File.separator;
	public static final String CUSTOM_BLOCK_BEHAVIOR_CONFIG_PATH = "." + File.separator + "customBlockBehaviorConfig" + File.separator;
	public static final String CUSTOM_FACTION_CONFIG_PATH = "." + File.separator + "customFactionConfig" + File.separator;
	public static final String CUSTOM_TEXTURE_PATH = "." + File.separator + "customBlockTextures" + File.separator;
	public static final String CUSTOM_TEXTURE_TEMPLATE_PATH = "." + File.separator + "data" + File.separator + "textures" + File.separator + "customTemplates" + File.separator;
	public static final String CUSTOM_CONFIG_IMPORT_TEMPLATE_PATH = "." + File.separator + "data" + File.separator + "config" + File.separator + "customConfigTemplate" + File.separator;
	public static final String buttonSpriteName = "buttons-8x8";
	public static final String CUSTOM_CONFIG_IMPORT_PATH = "." + File.separator + "customBlockConfig" + File.separator;
	public static int cubeTexture3d;
	public static int cubeTexture3dNormal;
	public static Texture lavaTexture;
	public static Texture overlayTextures;
	public static Texture overlayTexturesLow;
	public static Texture[] cubeTextures;
	public static Texture[] cubeNormalTextures;
	public static Texture[] cubeTexturesLow;
	public static Texture[] cubeNormalTexturesLow;
	public static Texture[] effectTextures;
	public static Texture[] gasGiantTextures;
	public static Texture[] gasGiantRingMaps;
	public static Texture skyTexture;
	public static Texture nebulaTexture;
	public static Texture simpleStarFieldTexture;
	public static Texture marpleTexture;
	public static Texture3D noiseVolume;
	public static PlayerSkin[] traidingSkin;
	private static byte[] buffer;

	public GameResourceLoader() {
		super(0);
	}

	public static File getConfigInputFile() {
		return new FileExt(CUSTOM_CONFIG_IMPORT_PATH + "BlockConfigImport.xml");
	}

	@Override
	public void loadServer() throws FileNotFoundException, ResourceException, ParseException, SAXException, IOException, ParserConfigurationException {
		super.loadServer();
		enqueueModels();
		Controller.getResLoader().loadQueue.add(new ResourceLoadEntryOther("ElementDataInitialization") {
			@Override
			protected void loadResource(ResourceLoader resourceLoader) throws IOException {
				ElementKeyMap.initDataForGame();
			}
		});
		
		Controller.getResLoader().forceLoadAll();
	}
	@Override
	public void loadClient() {
		Controller.getResLoader().loadQueue.add(new ResourceLoadEntryOther("ElementDataInitialization") {
			@Override
			public void loadResource(ResourceLoader resourceLoader) throws ResourceException, IOException {
				ElementKeyMap.initDataForGame();
			}
		});
	}
	public static void createCustomTextureZip() throws IOException {

		String hash = "";
		hash = FileUtil.createFilesHashRecursively(CUSTOM_TEXTURE_PATH, pathname -> {
			boolean accept = pathname.isDirectory() || pathname.getName().toLowerCase(Locale.ENGLISH).endsWith(".png");
//					System.err.println("ACCEPT: "+pathname+": "+accept);
			return accept;
		});
		File f = new FileExt(CUSTOM_TEXTURE_PATH + "pack.zip");
		File fa = new FileExt(CUSTOM_TEXTURE_PATH + "hash.txt");
		if (fa.exists() && f.exists()) {

			BufferedReader r = new BufferedReader(new FileReader(fa));
			String hashRead = r.readLine();
			r.close();

			if (hash.equals(hashRead)) {
				System.out.println("[RESOURCES][CustomTextures] No need to create pack.zip. Hash matches (as rewriting a zip changes the hash on it)");
				return;
			}
		}
		fa.delete();

		System.err.println("[CustomTextures] Writing hash: " + hash);
		BufferedWriter sb = new BufferedWriter(new FileWriter(fa));
		sb.append(hash);
		sb.flush();
		sb.close();

		if (f.exists()) {
			f.delete();
		}

		FolderZipper.zipFolder(CUSTOM_TEXTURE_PATH, CUSTOM_TEXTURE_PATH + "pack.zip", "pack.zip", null);
	}

	public static void copyDefaultCustomTexturesTo(String path) throws IOException {
		String custom = path;
		File customDir = new FileExt(custom);

		FileUtil.copyDirectory(new FileExt(CUSTOM_TEXTURE_TEMPLATE_PATH), customDir);

	}

	public static void copyCustomConfig(String path) throws IOException {
		String custom = path;
		File customDir = new FileExt(custom);

		FileUtil.copyDirectory(new FileExt(CUSTOM_CONFIG_IMPORT_TEMPLATE_PATH), customDir);
	}

	private static Texture getBlockOverlayTexture(String pack, int res) throws IOException {
//INSERTED CODE
		Texture texture2D = Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/overlays.png", GL11.GL_LINEAR, true, true);
		texture2D = StarLoaderHooks.onOverlayTextureLoad(texture2D, pack, res);
		///
		return texture2D;	}

	private static void loadCubeTextures(Texture[] t, String pack, int res, String custom) throws IOException {
		t[0] = Controller.getTexLoader().getTexture2DAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t000", GL11.GL_LINEAR, true, true);
		t[1] = Controller.getTexLoader().getTexture2DAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t001", GL11.GL_LINEAR, true, true);
		t[2] = Controller.getTexLoader().getTexture2DAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t002", GL11.GL_LINEAR, true, true);
		t[3] = Controller.getTexLoader().getTexture2DAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t003", GL11.GL_LINEAR, true, true);
		t[7] = Controller.getTexLoader().getTexture2DAnyFormat(custom + "/" + res + "/custom", GL11.GL_LINEAR, true, true);
		//INSERTED CODE
		StarLoaderHooks.onCubeTextureLoad(t, pack, res, custom);
		///
	}

	private static void loadCubeNormalTextures(Texture[] t, String pack, int res, String custom, boolean checkForTga) throws IOException {
		t[0] = Controller.getTexLoader().getTexture2DAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t000_NRM", GL11.GL_LINEAR, true, true);
		t[1] = Controller.getTexLoader().getTexture2DAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t001_NRM", GL11.GL_LINEAR, true, true);
		t[2] = Controller.getTexLoader().getTexture2DAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t002_NRM", GL11.GL_LINEAR, true, true);
		t[3] = Controller.getTexLoader().getTexture2DAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t003_NRM", GL11.GL_LINEAR, true, true);
		t[7] = Controller.getTexLoader().getTexture2DAnyFormat(custom + "/" + res + "/custom_NRM", GL11.GL_LINEAR, false, true);

		//INSERTED CODE
//		StarLoaderHooks.onCubeTextureLoad(t, pack, res, custom);
		///
		if(checkForTga){
			ShaderLibrary.USE_CUBE_TEXTURE_EMISSION = (new FileExt(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t000_NRM.tga")).exists();
			System.err.println("[RESOURCE] emission texture present: "+ShaderLibrary.USE_CUBE_TEXTURE_EMISSION);
		}
	}
	public static File[] copyTextures(String path) throws IOException{

		String pack = EngineSettings.G_TEXTURE_PACK.getObject().toString();
		File packDir = new FileExt(DataUtil.dataPath + "./textures/block/" + pack + "/");
		if (!packDir.exists()) {
			System.err.println("WARNING: texture pack: " + pack + " does not exist. Reverting to default");
			EngineSettings.G_TEXTURE_PACK.setFromString("Default");
			pack = EngineSettings.G_TEXTURE_PACK.getObject().toString();
		}
		
		int res = 512;
		File resTest = new FileExt(DataUtil.dataPath + "./textures/block/" + pack + "/"+res+"/");
		while(res > 64 && !resTest.exists()){
			res /= 2;
			resTest = new FileExt(DataUtil.dataPath + "./textures/block/" + pack + "/"+res+"/");
		}
		
		String custom = EngineSettings.CLIENT_CUSTOM_TEXTURE_PATH.getObject().toString();
		
		File[] f = new File[8];
		
		f[0] = getTexture2DFileAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t000");
		f[1] =  getTexture2DFileAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t001");
		f[2] =  getTexture2DFileAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t002");
		f[3] =  getTexture2DFileAnyFormat(DataUtil.dataPath + "./textures/block/" + pack + "/" + res + "/t003");

		f[7] =  getTexture2DFileAnyFormat(custom + "/" + res + "/custom");
		
		
		for(int i = 0; i < f.length; i++){
			if(f[i] != null){
				File nFile = new FileExt(path+"/"+f[i].getName());
				nFile.createNewFile();
				FileUtil.copyFile(f[i], nFile);
				f[i] = nFile;
			}
		}
		return f;
	}
	public static File getTexture2DFileAnyFormat(String resourceName) throws IOException {
		File png = new FileExt(resourceName+".png");
		File tga = new FileExt(resourceName+".tga");
		
		
		if(tga.exists()){
			return tga;
		}else if(png.exists()){
			return png;
		}else{
			throw new FileNotFoundException("Neither .png or .tga found for resource "+resourceName);
		}
		
		
	}
	public static List<ResourceLoadEntry> getBlockTextureResourceLoadEntry() {
		final List<ResourceLoadEntry> l = new ObjectArrayList<ResourceLoadEntry>();
		
		
		final int res = EngineSettings.G_TEXTURE_PACK_RESOLUTION.getInt();
		final String pack = EngineSettings.G_TEXTURE_PACK.getObject().toString();
		final File packDir = new FileExt(DataUtil.dataPath + "./textures/block/" + pack + "/");
		final String customPath = EngineSettings.CLIENT_CUSTOM_TEXTURE_PATH.getString();
		final File customDir = new FileExt(customPath);
		
		l.add( new ResourceLoadEntryOther("Extract_Textures") {
			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				extractIfBlockTexturesIfNecessary();
			}
		});
		
		l.add( new ResourceLoadEntryOther("Copy_Custom_Textures") {
			@Override
			public boolean canLoad(){
				assert(customDir != null);
				return customDir != null && !customDir.exists();
			}
			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				assert(false);
				try {
					throw new Exception("Custom textures in this resolution do not exist for this server. reverting to default");
				} catch (Exception e) {
					e.printStackTrace();
//					GLFrame.processErrorDialogExceptionWithoutReportWithContinue(e, null);
				}
				try {
					EngineSettings.CLIENT_CUSTOM_TEXTURE_PATH.setString("./customBlockTextures");
					EngineSettings.write();
					copyDefaultCustomTexturesTo(EngineSettings.CLIENT_CUSTOM_TEXTURE_PATH.getString());
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				EngineSettings.CLIENT_CUSTOM_TEXTURE_PATH.setString(customPath);
			}
		});
		
		l.add( new ResourceLoadEntryOther(pack+packDir+res) {
			@Override
			public boolean canLoad(){
				return true;
			}

			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {

				String texPack = pack;
				if (!packDir.exists()) {
					System.err.println("WARNING: texture pack: " + texPack + " does not exist. Reverting to default");
					try {
						EngineSettings.G_TEXTURE_PACK.setFromString("Default");
					} catch (SettingStateParseError e) {
						e.printStackTrace();
					}
					texPack =  EngineSettings.G_TEXTURE_PACK.getObject().toString();
				}
				
				try {
					System.err.println("[RESOURCE] Loading cube textures");
					

					printMemory("GL_MEMORY Before DEFFUSE");
					
					if(cubeTextures != null){
						for(Texture t : cubeTextures){
							if(t != null){
								t.cleanUp();
							}
						}
					}
					if(cubeTexturesLow != null){
						for(Texture t : cubeTexturesLow){
							if(t != null){
								t.cleanUp();
							}
						}
					}
					if(overlayTexturesLow != null){
						overlayTexturesLow.cleanUp();
					}
					if(overlayTextures != null){
						overlayTextures.cleanUp();
					}
					cubeTextures = new Texture[8];
					loadCubeTextures(cubeTextures, texPack, res, customPath);
					overlayTextures = getBlockOverlayTexture(texPack, res);

					if ("64".equals(res) || !(new FileExt(DataUtil.dataPath + "./textures/block/" + texPack + "/64/")).exists()) {
						cubeTexturesLow = cubeTextures;
						overlayTexturesLow = overlayTextures;
					} else {
						cubeTexturesLow = new Texture[8];
						try {
							loadCubeTextures(cubeTexturesLow, texPack, 64, customPath);
						} catch (Exception e) {
							System.err.println("Exception: ERROR LOADING LOW QUALITY TEXTURES!");
							e.printStackTrace();
							System.err.println("Catched Exception: No low quality available. Program may continue but performance on big objects might suffer");
							cubeTexturesLow = cubeTextures;
						}
						try {
							overlayTexturesLow = getBlockOverlayTexture(texPack, 64);
						} catch (Exception e) {
							System.err.println("Exception: ERROR LOADING LOW QUALITY OVERLAYS!");
							e.printStackTrace();
							System.err.println("Catched Exception: No low quality overlay available. Program may continue but performance on big objects might suffer");
							overlayTexturesLow = overlayTextures;
						}

					}

					printMemory("GL_MEMORY AFTER DIFFUSE");
						

					System.err.println("[RESOURCE] Loading cube textures DONE");
				} catch (IOException e) {
					e.printStackTrace();
					throw new ResourceException(DataUtil.dataPath + "./textures/block/");
				}
			}
		});
		
		l.add( new ResourceLoadEntryOther(pack+packDir+res+"normal") {
			@Override
			public boolean canLoad(){
				return true;
			}
			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				
				String texPack = pack;
				if (!packDir.exists()) {
					System.err.println("WARNING: texture pack: " + texPack + " does not exist. Reverting to default");
					try {
						EngineSettings.G_TEXTURE_PACK.setFromString("Default");
					} catch (SettingStateParseError e) {
						e.printStackTrace();
					}
					texPack = EngineSettings.G_TEXTURE_PACK.getObject().toString();
				}
				try{
					if (EngineSettings.G_NORMAL_MAPPING.isOn()) {
						if(cubeNormalTextures != null){
							for(Texture t : cubeNormalTextures){
								if(t != null){
									t.cleanUp();
								}
							}
						}
						if(cubeNormalTexturesLow != null){
							for(Texture t : cubeNormalTexturesLow){
								if(t != null){
									t.cleanUp();
								}
							}
						}
						cubeNormalTextures = new Texture[8];
						try {
							for (int i = 0; i < 3; i++) {
								File f = new FileExt(DataUtil.dataPath + "./textures/block/" + texPack + "/" + res + "/t00" + i + "_NRM.tga");
								if (!f.exists()){
									f = new FileExt(DataUtil.dataPath + "./textures/block/" + texPack + "/" + res + "/t00" + i + "_NRM.png");
								}
								if (!f.exists() && cubeTextures[i] != null) {
									System.err.println("WARNING: NOT USING NORAML MAPS BECAUSE NORMAL MAPS DONT EXIST " + f.getAbsolutePath());
									throw new ErrorDialogException("\n\nNormal map (neither png or tga) for this texture pack not found: \n" + f.getAbsolutePath() + "\n"+ (new FileExt(DataUtil.dataPath + "./textures/block/" + texPack + "/" + res + "/t00" + i + "_NRM.tga")).getAbsolutePath()+" \n\npress 'retry' to play without bump mapping");
								}
							}
							File f = new FileExt(customPath + "/" + res + "/custom_NRM.png");
							if (!f.exists() && cubeTextures[7] != null) {
								System.err.println("WARNING: NOT USING NORAML MAPS BECAUSE NORMAL MAPS DONT EXIST " + f.getAbsolutePath());
								throw new ErrorDialogException("\n\nNormal map for this texture pack not found: \n" + f.getAbsolutePath() + "\n\npress 'retry' to play without bump mapping");
							}
						} catch (ErrorDialogException e) {
							GLFrame.processErrorDialogExceptionWithoutReportWithContinue(e, null);
							EngineSettings.G_NORMAL_MAPPING.setOn(false);
						}
	
						loadCubeNormalTextures(cubeNormalTextures, texPack, res, customPath, true);
	
						if ("64".equals(res) || !(new FileExt(DataUtil.dataPath + "./textures/block/" + texPack + "/64/")).exists()) {
							cubeNormalTexturesLow = cubeNormalTextures;
						} else {
							cubeNormalTexturesLow = new Texture[8];
							try {
								loadCubeNormalTextures(cubeNormalTexturesLow, texPack, 64, customPath, false);
							} catch (Exception e) {
								System.err.println("Exception: ERROR LOADING LOW QUALITY NORMAL MAPS!");
								e.printStackTrace();
								System.err.println("Catched Exception: No low quality normal maps available. Program may continue but performance on big objects might suffer");
								cubeNormalTexturesLow = cubeNormalTextures;
							}
						}
	
						printMemory("GL_MEMORY AFTER NORMAL");
					}
				
				} catch (IOException e) {
					e.printStackTrace();
					throw new ResourceException(DataUtil.dataPath + "./textures/block/");
				}
			}
		});
		
		return l;
	}
	private static void extractIfBlockTexturesIfNecessary() {
		File lastExtracted = new FileExt("lastExtr");
		boolean needsExtr = true;
		DataInputStream s = null;
		try {
			if(lastExtracted.exists()){
				s = new DataInputStream(new FileInputStream(lastExtracted));
				if(VersionContainer.build.equals(s.readUTF())){
					needsExtr = false;
				}
				
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(s != null){
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		File test = new FileExt("./data/textures/block/Default/64/t000.png");
		System.err.println("[TEXTURELOADER] Checking if textures exist: "+test.exists());
		if(!needsExtr && !test.exists()){
			System.err.println("[TEXTURELOADER] Textures do not exist! Forcing Re-Extraction of Texture Archives!");
			needsExtr = true;
		}
		
		if(needsExtr){
			buffer = new byte[10*1024];
			extractDirRec(new FileExt("./data/textures/block/"));
			buffer = null;
			
			DataOutputStream o = null;
			try {
				lastExtracted.delete();
				o = new DataOutputStream(new FileOutputStream(lastExtracted));
					
				o.writeUTF(VersionContainer.build);
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				if(o != null){
					try {
						o.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	private static void extractDirRec(File file) {
		if(file.isDirectory()){
			File[] listFiles = file.listFiles();
			for(File ff : listFiles ){
				extractDirRec(ff);
			}
		} else {
			if(file.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip")){
				try {
					ZipInputStream zis =  new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
					
					ZipEntry ze = zis.getNextEntry();
					
					File outFile = new FileExt(file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-4));
					BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile));             
					System.out.println("[RESOURCE] Extracting "+outFile.getAbsolutePath()+" ...");
		            int len;
		            while ((len = zis.read(buffer)) > 0) {
		            	fos.write(buffer, 0, len);
		            }
					zis.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			
			}
		}
	}

	public static void printMemory(String over) {
		if (GraphicsContext.current.getCapabilities().GL_NVX_gpu_memory_info) {
			System.err.println(over);
			int CURRENT_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
			System.err.println("CURRENT_AVAILABLE: " + (CURRENT_AVAILABLE / 1024) + "MB");
			int TOTAL_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
			System.err.println("TOTAL_AVAILABLE: " + (TOTAL_AVAILABLE / 1024) + "MB");
			int INFO_DEDICATED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX);
			System.err.println("INFO_DEDICATED: " + (INFO_DEDICATED / 1024) + "MB");
			int INFO_EVICTED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTED_MEMORY_NVX);
			System.err.println("INFO_EVICTED: " + (INFO_EVICTED / 1024) + "MB");

		} else if (GraphicsContext.current.getCapabilities().GL_ATI_meminfo) {
			System.out.println("ATI VBO_FREE: " + GL11.glGetInteger(ATIMeminfo.GL_VBO_FREE_MEMORY_ATI));
			System.out.println("ATI RENDERBUFFER_FREE: " + GL11.glGetInteger(ATIMeminfo.GL_RENDERBUFFER_FREE_MEMORY_ATI));
			System.out.println("ATI TEXTURE_FREE: " + GL11.glGetInteger(ATIMeminfo.GL_TEXTURE_FREE_MEMORY_ATI));

		}
		GlUtil.printGlErrorCritical("Memory size access failed");
	}

	public static int getOverlayTextures(CubeTexQuality quality) {
		if (quality == CubeTexQuality.SELECTED) {
			return overlayTextures.getTextureId();
		} else {
			return overlayTexturesLow.getTextureId();
		}
	}

	public static int getCubeTexture(int i, CubeTexQuality quality) {
		if (quality == CubeTexQuality.SELECTED) {
			return cubeTextures[i].getTextureId();
		} else {
			return cubeTexturesLow[i].getTextureId();
		}
	}

	public static int getCubeNormalTexture(int i, CubeTexQuality quality) {
		if (quality == CubeTexQuality.SELECTED) {
			return cubeNormalTextures[i].getTextureId();
		} else {
			return cubeNormalTexturesLow[i].getTextureId();
		}
	}


	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.core.ResourceLoader#loadCustom()
	 */
	@Override
	public List<? extends ResourceLoadEntry> loadCustom() throws ResourceException {
		@SuppressWarnings("unchecked")
		List<ResourceLoadEntry> customLoadList = (List<ResourceLoadEntry>) super.loadCustom();
		if (noiseVolume == null) {

		}

		customLoadList.add(new ResourceLoadEntryOther("NoiseVolume") {

			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				//				System.err.println("Init noise volume");
				GlUtil.printGlErrorCritical();
				noiseVolume = new Texture3D();
				try {
					DDSLoader.load(new FileExt(DataUtil.dataPath + "effects/thruster/NoiseVolume.dds"), noiseVolume, false);
				} catch (IOException e1) {
					e1.printStackTrace();
					throw new ResourceException("noise volume");
				}
				GlUtil.printGlErrorCritical();
				//				System.err.println("Init noise volume DONE");
			}
		});

		customLoadList.add(new ResourceLoadEntryOther("marple") {

			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				try {
					marpleTexture = Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "textures/marble-seamless-texture.png", true);
				} catch (IOException e) {
					e.printStackTrace();
					throw new ResourceException(DataUtil.dataPath + "textures/marble-seamless-texture.png");
				}
			}
		});
		customLoadList.add(new ResourceLoadEntryOther("LavaTex") {

			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				try {
					lavaTexture = Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "textures/lava.png", true);
				} catch (IOException e) {
					e.printStackTrace();
					throw new ResourceException(DataUtil.dataPath + "textures/lava.png");
				}
			}
		});
		/*
		customLoadList.add(new ResourceLoadEntryOther("GasGiantColormaps") {
		   @Override
		   protected void loadResource(ResourceLoader resourceLoader){
			   gasGiantTextures = new Texture[]{
					   lavaTexture
			   };
		   }
	   });
		 */

		customLoadList.add(new ResourceLoadEntryOther("GasGiantColormaps") {
			@Override
			protected void loadResource(ResourceLoader resourceLoader) throws ResourceException {
					try {
						gasGiantTextures = new Texture[]{
								Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "image-resource/gasgiant/Gas_Giant_1.png", true),
								Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "image-resource/gasgiant/Gas_Giant_2.png", true),
								Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "image-resource/gasgiant/Gas_Giant_3.png", true)
						};
					} catch (IOException e) {
						System.err.println("[GasGiants] Error in GameResourceLoader while attempting to load colour maps for gas giants:");
						e.printStackTrace();
						throw new ResourceException(DataUtil.dataPath + "image-resource/gasgiant/");
					}
				}
		});
		customLoadList.add(new ResourceLoadEntryOther("GasGiantRingPatterns") {
			@Override
			protected void loadResource(ResourceLoader resourceLoader) throws ResourceException {
				try {
					gasGiantRingMaps = new Texture[]{
							Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "image-resource/gasgiant/ringstrip.png", true)
					};
				} catch (IOException e) {
					e.printStackTrace();
					throw new ResourceException(DataUtil.dataPath + "image-resource/gasgiant/ringstrip.png");
				}
			}
		});
//		customLoadList.add(new ResourceLoadEntryOther("NebulaTexture") {
//			@Override
//			protected void loadResource(ResourceLoader resourceLoader) throws ResourceException {
//				try {
//					nebulaTexture = Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "image-resource/nebula.png", false);
//				} catch(IOException exception) {
//					exception.printStackTrace();
//					throw new ResourceException(DataUtil.dataPath + "image-resource/nebula.png");
//				}
//			}
//		});
		customLoadList.add(new ResourceLoadEntryOther("ExplosionData") {
			
			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				ExplosionRunnable.initialize();
			}
		});
		customLoadList.add(new ResourceLoadEntryOther("SimpeStarField") {

			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				try {
					simpleStarFieldTexture = Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "textures/simplestarfield.png", true);
				} catch (IOException e) {
					e.printStackTrace();
					throw new ResourceException(DataUtil.dataPath + "textures/simplestarfield.png");
				}
			}
		});
		customLoadList.add(new ResourceLoadEntryOther("Effects") {
			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				try {
					System.err.println("[RESOURCE] Loading effects");
					effectTextures = new Texture[2];
					effectTextures[0] = Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "effects/dudvmap.jpg", true);
					effectTextures[1] = Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "effects/noise.png", true);
					System.err.println("[RESOURCE] Loading effects done");
				} catch (IOException e) {
					e.printStackTrace();
					throw new ResourceException(DataUtil.dataPath + "effects/");
				}
			}
		});
		customLoadList.add(new ResourceLoadEntryOther("TradingSkins") {
			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				traidingSkin = new PlayerSkin[1];
				try {
					traidingSkin[0] = PlayerSkin.create(new FileExt(DataUtil.dataPath + "textures/skins/"), "traid00");
				} catch (IOException e) {
					e.printStackTrace();
					throw new ResourceException(DataUtil.dataPath + "effects/");
				}
			}
		});

		customLoadList.add(new ResourceLoadEntryOther("SkyTexture"+EngineSettings.G_USE_HIGH_QUALITY_BACKGROUND.isOn()) {
			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				String res;
				System.err.println("[RESOURCE] Loading background");
				if (!EngineSettings.G_USE_HIGH_QUALITY_BACKGROUND.isOn()) {
					res = DataUtil.dataPath + "sky/milkyway/Milky-Way-texture-cube";
				} else {
					res = DataUtil.dataPath + "sky/generic/generic";
				}
				try {
					skyTexture = (Controller.getTexLoader().getCubeMap(res, "png"));
					System.err.println("[RESOURCE] Loading background DONE");
				} catch (IOException e) {
					e.printStackTrace();
					throw new ResourceException(DataUtil.dataPath + res);
				}

			}
		});
		customLoadList.add(new ResourceLoadEntryOther("Occlusion"+EngineSettings.G_USE_OCCLUSION_CULLING.isOn()) {
			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException {
				if (EngineSettings.G_USE_OCCLUSION_CULLING.isOn()) {
					System.err.println("[OCLUSION] INITIALIZING OCCLUSION QUERIES: " + EngineSettings.G_MAX_SEGMENTSDRAWN.getInt() * 2);
					printMemory("Before Oclusion Queries Initialization");
					SegmentDrawer.segmentOcclusion.reinitialize(EngineSettings.G_MAX_SEGMENTSDRAWN.getInt() * 2);
					printMemory("After Oclusion Queries Initialization");
				}

			}
		});
		
		List<ResourceLoadEntry> blockTextureResourceLoadEntry = getBlockTextureResourceLoadEntry();
		
		customLoadList.addAll(blockTextureResourceLoadEntry);

		/*
		File dir = new FileExt("./data/tutorial/");
		for (File subDir : dir.listFiles()) {
			if (subDir.isDirectory()) {
				for (File f : subDir.listFiles()) {
					if (subDir.exists()) {
						File[] files = subDir.listFiles();
						for (int i = 0; i < files.length; i++) {
							if (files[i].getName().endsWith(".png")) {
								ResourceLoadEntry e = new ResourceLoadEntryImage(files[i]);
								customLoadList.add(e);
							}
						}
					}
				}
			}
		}
		 */
		return customLoadList;
	}

	public enum StandardButtons {
		BUY_BUTTON,
		SELL_BUTTON,
		OK_BUTTON,
		CANCEL_BUTTON,
		SPAWN_BUTTON,
		EXIT_BUTTON,
		RESUME_BUTTON,
		OPTIONS_BUTTON,
		EXIT_TO_WINDOWS_BUTTON,
		SUICIDE_BUTTON,
		BUY_MORE_BUTTON,
		SELL_MORE_BUTTON,
		GREEN_TEAM_BUTTON,
		BLUE_TEAM_BUTTON,
		SAVE_SHIP_BUTTON,
		SAVE_SHIP_LOCAL_BUTTON,
		UPLOAD_LOCAL_SHIP_BUTTON,
		NEXT_BUTTON,
		BACK_BUTTON,
		SKIP_BUTTON,
		END_TUTORIAL_BUTTON,
		TAKE_BUTTOM,
		PUT_BUTTOM,
		TAKE_QUANTITY_BUTTOM,
		PUT_QUANTITY_BUTTOM,
		MESSAGE_LOG_BUTTOM,;

		public int getSpriteNum(boolean pressed) {
			return this.ordinal() + (pressed ? 32 : 0);
		}

	}

	@Override
	public void onStopClient() {
		//TODO clean up custom textures
	}

	@Override
	public void enqueueConfigResources(final String guiConfigFileName, boolean reenqueue){
		
		{
		ResourceLoadEntry e = new ResourceLoadEntryOther("CONFIG_HUD"){

			@Override
			public void loadResource(ResourceLoader resourceLoader)
					throws ResourceException, IOException {
				try {
					HudConfig.load();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new ResourceException("HUD CONFIG FAILED TO PARSE");
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					throw new ResourceException("HUD CONFIG FAILED TO PARSE");
				} catch (SAXException e) {
					e.printStackTrace();
					throw new ResourceException("HUD CONFIG FAILED TO PARSE");
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
					throw new ResourceException("HUD CONFIG FAILED TO PARSE");
				} catch (ConfigParserException e) {
					e.printStackTrace();
					throw new ResourceException("HUD CONFIG FAILED TO PARSE");
				}
			}
			
		};
		assert( e != null);
		if(reenqueue){
			loadedDataEntries.remove(e);
		}
		loadQueue.add(e);
		}
		{
			ResourceLoadEntry e = new ResourceLoadEntryOther("CONFIG_GUI"){

				@Override
				public void loadResource(ResourceLoader resourceLoader)
						throws ResourceException, IOException {
					try {
						System.err.println("[CLIENT] Loading GUI CONFIG: "+guiConfigFileName);
						GuiConfig.load(guiConfigFileName);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						throw new ResourceException("GUI CONFIG FAILED TO PARSE");
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						throw new ResourceException("GUI CONFIG FAILED TO PARSE");
					} catch (SAXException e) {
						e.printStackTrace();
						throw new ResourceException("GUI CONFIG FAILED TO PARSE");
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
						throw new ResourceException("GUI CONFIG FAILED TO PARSE");
					} catch (ConfigParserException e) {
						e.printStackTrace();
						throw new ResourceException("GUI CONFIG FAILED TO PARSE");
					}
				}
				
			};
			this.loadedDataEntries.remove(e);
			assert( e != null);
			loadQueue.add(e);
		}
		resetLoadCounts();
			
	}
	public static void enqueueGuiConfig(){
		
	}
}
