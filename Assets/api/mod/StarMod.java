package api.mod;

import api.DebugFile;
import api.config.BlockConfig;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.config.FileConfiguration;
import api.utils.particle.ModParticleUtil;
import me.jakev.starloader.IClassTransformer;
import me.jakev.starloader.LaunchClassLoader;
import org.apache.commons.io.IOUtils;
import org.schema.schine.resource.ResourceLoader;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StarMod {
	private static final int MAX_LOGS = 10;
	ModSkeleton skeleton;
	private boolean loggerInit;
	private FileOutputStream logFile;
	//

	/**
	 * Fired to core mods whenever a class is loaded.
	 */
	public byte[] onClassTransform(String className, byte[] byteCode) {
		return byteCode;
	}
	//===================== [ Life cycle events ] ==========================

	/**
	 * Where mods are enabled before/during world load
	 */
	public void onEnable() {
	}

	/**
	 * When a player leaves a server, mods are disabled
	 */
	public void onDisable() {
	}

	/**
	 * Mods that do stuff directly when the game starts
	 * Happens for EVERY mod, not just the ones enabled
	 */
	public void onLoad() {
	}

	/**
	 * is run when a serverstate instance is detected. Use it to run only serverside code.
	 * added by IR0NSIGHT.
	 */
	public void onServerCreated(ServerInitializeEvent event) {
		DebugFile.log("Starmod was run with onServerCreated()", this);
	}

	/**
	 * Method that is run when a GameClientState instance is made. Used to run only clientside code.
	 * added by IR0NSIGHT.
	 */
	public void onClientCreated(ClientInitializeEvent event) {
		DebugFile.log("Starmod was run with onClientCreated()", this);
	}

    //
	@Deprecated
    private final HashMap<String, FileConfiguration> configMapOld = new HashMap<>();

	@Deprecated
    public FileConfiguration getConfig(String name){
        FileConfiguration namedConfig = configMapOld.get(name);
        if(namedConfig == null){
            FileConfiguration newConfig = new FileConfiguration(this, name);
            configMapOld.put(name, newConfig);
            return newConfig;
        }
        return namedConfig;
    }

    //

    public void addClassFileTransformer(IClassTransformer transformer){
        if(skeleton.isCoreMod()){
            ((LaunchClassLoader) getClass().getClassLoader()).registerClassTransformer(transformer);
        }else{
            throw new IllegalStateException("Mod: " + this.skeleton.getDebugName() + " Tried to register a class transformer but was not a core mod.");
        }
    }

	public ModSkeleton getSkeleton() {
		return skeleton;
	}
	// ========== Resource Loading Methods =============

	public void setSkeleton(ModSkeleton skeleton) {
		this.skeleton = skeleton;
	}

	/**
	 * Called when mods should register UniversalRegistry values (URVs)
	 */
	public void onUniversalRegistryLoad() {
	}

	/**
	 * Called after the BlockConfig is loaded.
	 *
	 * @param config Dummy value for compatibility, BlockConfig is a static helper
	 */
	public void onBlockConfigLoad(BlockConfig config) {
	}

	public void onResourceLoad(ResourceLoader loader) {
	}

	public void onLoadModParticles(ModParticleUtil.LoadEvent event) {
	}

    public String getName(){
        return skeleton.getName();
    }
    /**
     *
     * @param url The absolute path to the jar resource, like "me/jakev/extraeffects/res/cloud.png"
     * @return The InputStream of the resource, null if not found.
     */
    @NotNull
    public InputStream getJarResource(String url) throws IllegalArgumentException {
        InputStream resourceAsStream = skeleton.getClassLoader().getResourceAsStream(url);
        if(resourceAsStream == null) throw new IllegalArgumentException("Could not find resource: " + url + " | Mod: " + this.skeleton.getDebugName());
        return resourceAsStream;
    }
    @NotNull
    public BufferedImage getJarBufferedImage(String url) throws IllegalArgumentException {
        InputStream resourceAsStream = skeleton.getClassLoader().getResourceAsStream(url);
        if(resourceAsStream == null) throw new IllegalArgumentException("Could not find resource: " + url + " | Mod: " + this.skeleton.getDebugName());
        BufferedImage image = null;
        try {
            image = ImageIO.read(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(image == null) throw new IllegalArgumentException("Error loading image: " + url + " | Mod: " + this.skeleton.getDebugName());
        return image;
    }

	//============= [ Class Ripper ] ================
	//You should probably just use ASM
	//Should look like: org.schema.common.whatever.ScanAddOn$3
	protected void forceDefine(String name) {
		String classFileFromFQN = LaunchClassLoader.getClassFileFromFQN(name);
		try {
			ZipInputStream zip = new ZipInputStream(new FileInputStream(skeleton.getJarFile()));
			while(true) {
				ZipEntry entry = zip.getNextEntry();
				if(entry == null) break;
				System.err.println("Entry: " + entry.getName());
				System.err.println("FQN: " + classFileFromFQN);
				System.err.println("=-===============");
				if(entry.getName().equals(classFileFromFQN)) {
					System.err.println("ENTRY NAME: " + classFileFromFQN + ", name=" + name);
					((LaunchClassLoader) getClass().getClassLoader()).registerClassBytes(name, IOUtils.toByteArray(zip));
				}
			}
			zip.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void logInfo(String message) {
		logMessage("[INFO]: " + message);
		System.out.println("[INFO]: " + message);
	}

	public void logWarning(String message) {
		logMessage("[WARNING]: " + message);
		System.err.println("[WARNING]: " + message);
	}

	public void logException(String message, Exception exception) {
		logMessage("[EXCEPTION]: " + message + "\n" + exception.getMessage() + "\n" + Arrays.toString(exception.getStackTrace()));
		System.err.println("[EXCEPTION]: " + message);
	}

	public void logFatal(String message, Exception exception) {
		logMessage("[FATAL]: " + message + "\n" + exception.getMessage() + "\n" + Arrays.toString(exception.getStackTrace()));
		System.err.println("[FATAL]: " + message);
		System.exit(skeleton.getSmdResourceId());
	}

	private void logMessage(String message) {
		if(!loggerInit) initLogger();
		try {
			logFile.write((message + "\n").getBytes(StandardCharsets.UTF_8));
			logFile.flush();
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	private void initLogger() {
		try {
			File logsFolder = new File(skeleton.getResourcesFolder() + "/logs");
			if(!logsFolder.exists()) logsFolder.mkdirs();
			else if(logsFolder.listFiles() != null && Objects.requireNonNull(logsFolder.listFiles()).length > 0) {
				File[] logFiles = new File[Objects.requireNonNull(logsFolder.listFiles()).length];
				int j = logFiles.length - 1;
				for(int i = 0; i < logFiles.length && j >= 0; i++) {
					logFiles[i] = Objects.requireNonNull(logsFolder.listFiles())[j];
					j--;
				}
				for(File logFile : logFiles) {
					String fileName = logFile.getName().replace(".txt", "");
					int logNumber = Integer.parseInt(fileName.split("\\.")[1]) + 1;
					String newName = skeleton.getResourcesFolder() + "/logs/" + (skeleton.getName().toLowerCase(Locale.ENGLISH).replace(" ", "_")) + "_log." + logNumber + ".log";
					if(logNumber < MAX_LOGS) logFile.renameTo(new File(newName));
					else logFile.delete();
				}
			}
			File logFile = new File(skeleton.getResourcesFolder() + "/logs/" + skeleton.getName().toLowerCase(Locale.ENGLISH).replaceAll(" ", "_") + "_log.0.log");
			logFile.delete();
			logFile.createNewFile();
			this.logFile = new FileOutputStream(logFile);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		loggerInit = true;
	}
}
