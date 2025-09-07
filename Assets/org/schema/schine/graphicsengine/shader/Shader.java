/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Shader</H2>
 * <H3>org.schema.schine.graphicsengine.shader</H3>
 * Shader.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.graphicsengine.shader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.resource.ResourceLoader;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class Shader.
 */
public class Shader {

	private final ArrayList<String> defined = new ArrayList<String>();
	private final ShaderModifyInterface[] replace;
	public FloatBuffer matrixBuffer;
	public boolean recompiled = true;
	private Map<String, Integer> handleLookUp;
	/**
	 * The shader interface.
	 */
	private Shaderable shaderInterface;
	/**
	 * The vertexshader path.
	 */
	private String vertexshaderPath;
	/**
	 * The fragmentshader path.
	 */
	private String fragmentshaderPath;
	/**
	 * The shaderprogram.
	 */
	private int shaderprogram;
	private String geometryShaderPath;
	private boolean validated;
	public int optionBits = -1;
	private String vsrc;
	private String fsrc;

	/**
	 * Instantiates a new shader loader.
	 *
	 * @param gl                 the gl
	 * @param glu                the glu
	 * @param vertexshaderPath   the vertexshader path
	 * @param fragmentshaderPath the fragmentshader path
	 * @throws ResourceException the resource exception
	 * @ the error diolog exception
	 */
	public Shader(String vertexshaderPath,
	              String fragmentshaderPath, ShaderModifyInterface... replace) throws ResourceException {
		super();
		this.vertexshaderPath = vertexshaderPath;
		this.fragmentshaderPath = fragmentshaderPath;

		handleLookUp = new HashMap<String, Integer>();
		this.replace = replace;
		compile();
	}

	public Shader(String vertexshaderPath,
	              String fragmentshaderPath, String geometryShaderPath, ShaderModifyInterface... replace) throws ResourceException {
		super();
		this.vertexshaderPath = vertexshaderPath;
		this.fragmentshaderPath = fragmentshaderPath;
		this.geometryShaderPath = geometryShaderPath;

		handleLookUp = new HashMap<String, Integer>();
		this.replace = replace;
		compile();
	}

	/**
	 * Glu check error.
	 *
	 * @param gl    the gl
	 * @param where the where
	 */
	public static void gluCheckError(String where) {
		int error = GL11.glGetError();
		if (error != GL11.GL_NO_ERROR) {
			System.err.println("[SHADER] error in GL: " + where);
		}
	}

	public void bindAttributes(int shaderProgram) {

	}

	public void cleanUp() {
		//		System.out.println("[CLEANUP] [SHADER] cleaning shader "+vertexshaderPath+", "+fragmentshaderPath);
		try {
			GL20.glDeleteProgram(shaderprogram);
			shaderprogram = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void validate() {
		GlUtil.printGlErrorCritical();
		GL20.glGetProgrami(shaderprogram, GL20.GL_LINK_STATUS);
		GlUtil.printGlErrorCritical();
		// printShaderInfoLog();
		GL20.glValidateProgram(shaderprogram);
		int res = GL20.glGetProgrami(shaderprogram, GL20.GL_VALIDATE_STATUS);
		GlUtil.printGlErrorCritical();
		if (res != 1) {
			IntBuffer len = MemoryUtil.memAllocInt(1);
			String glGetProgramInfoLog = GL20.glGetProgramInfoLog(shaderprogram, GL20.glGetProgrami(shaderprogram, GL20.GL_INFO_LOG_LENGTH));
			throw new RuntimeException("\n" + vertexshaderPath + ", \n" + fragmentshaderPath + " \n\n" + glGetProgramInfoLog + "\nLINK STATUS: " + res);

		} else {
							System.err.println("[SHADER][SUCCESS] loading " + vertexshaderPath
									+ " and " + fragmentshaderPath);
			//				System.err.println("[SHADER] Program bound to " + getShaderprogram());
		}
	}

	/**
	 * Compile.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 * @throws ResourceException the resource exception
	 * @ the error diolog exception
	 */
	public void compile() throws ResourceException {
		// if(GameDrawManager.shaderMode == GameDrawManager.SHADER_MODE_OFF){
		// return;
		// }
//		System.err.println("[INIT][CLIENT][SHADER] Loading Shader: " + vertexshaderPath + "; " + fragmentshaderPath);

		try {

			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);
			int v = compileVertexShader();
			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);
			int f = compileFragmentShader();
			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);
			int g = compileGeometryShader();
			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);

			if (shaderprogram != 0) {
				GL20.glDeleteProgram(shaderprogram);
			}
			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);
			shaderprogram = GL20.glCreateProgram();
			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);

			if (v > 0) {
				GL20.glAttachShader(shaderprogram, v);
				GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);
				//				System.err.println("vertexShader for "+vertexshaderPath+" "+clazz);
			} else {
				//				System.err.println("no vertexShader for "+fragmentshaderPath+" "+clazz);
			}
			if (f > 0) {
				//				System.err.println("fragmentShader for "+fragmentshaderPath+" "+f);
				GL20.glAttachShader(shaderprogram, f);
				GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);
			} else {
				//				System.err.println("no fragmentShader for "+vertexshaderPath+" "+f);
			}
			if (g > 0) {
				//				System.err.println("geometryShader for "+geometryShaderPath+" "+g);
				GL20.glAttachShader(shaderprogram, g);
				GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);
			} else {
				//				System.err.println("no geometryShader for "+vertexshaderPath+" "+g);
			}
			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);

//			GL20.glLinkProgram(getShaderprogram());

			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);

			bindAttributes(shaderprogram);

			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);
			// printShaderInfoLog();
			GL20.glLinkProgram(shaderprogram);
			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);
			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);
			int linkState = GL20.glGetProgrami(shaderprogram, GL20.GL_LINK_STATUS);
			GlUtil.printGlErrorCritical(vertexshaderPath +"; "+ fragmentshaderPath);

			if (linkState != 1) {

				int logLen = GL20.glGetProgrami(shaderprogram, GL20.GL_INFO_LOG_LENGTH);

				String glGetProgramInfoLog = GL20.glGetProgramInfoLog(shaderprogram, logLen);
				System.err.println("VERTEX SOURCE:\n"+vsrc+"\n\n---------------------------");
				System.err.println("FRAGMENT SOURCE:\n"+fsrc+"\n\n---------------------------");
				throw new RuntimeException("\n" + vertexshaderPath + ", \n" + fragmentshaderPath + " \n\n" + glGetProgramInfoLog + "\nLINK STATUS: " + linkState);

			}
			validated = false;
			//validate();
			//			System.out.println("[SHADER] shader successfully loaded "+vertexshaderPath+"; "+fragmentshaderPath);
			// printShaderInfoLog();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ShaderException e) {
			e.printStackTrace();
			try {
				if (e.info.startsWith("0(")) {

					int ln = Integer.parseInt(e.info.substring(2, e.info.indexOf(")")));
					BufferedReader r = new BufferedReader(new StringReader(e.source));
					int i = 0;
					String l = null;

					while (i < ln && (l = r.readLine()) != null) {
						i++;
					}
					System.err.println("ERROR IN LINE: " + ln + " -> " + l);
					throw new RuntimeException(e.getMessage() + "\nLINE " + ln + ": \n" + l);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (NumberFormatException ee) {
				ee.printStackTrace();
			}
			throw new RuntimeException(e);
		}
		GlUtil.printGlErrorCritical();
		this.recompiled = true;
	}

	public int compileVertexShader() throws ResourceException, IOException, ShaderException {
		if (vertexshaderPath == null) {
			return -1;
		}
		GlUtil.printGlErrorCritical();
		int v = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		GlUtil.printGlErrorCritical();
		BufferedReader brv;
		brv = new BufferedReader(new InputStreamReader(
				ResourceLoader.resourceUtil
						.getResourceAsInputStream(vertexshaderPath)));
		GlUtil.printGlErrorCritical();
		vsrc = "";
		String line;
		
		
		
		while ((line = brv.readLine()) != null) {
			if (line.contains("#IMPORT")) {
				String file = line.replaceAll("#IMPORT", "").trim();
				BufferedReader reader;
				reader = new BufferedReader(new InputStreamReader(
						ResourceLoader.resourceUtil
								.getResourceAsInputStream(file)));
				while ((line = reader.readLine()) != null) {
					vsrc += line + "\n";
				}
				reader.close();
			} else {
				vsrc += line + "\n";
			}
		}
		brv.close();
		if (replace != null) {
			for (int i = 0; i < replace.length; i++) {
				vsrc = replace[i].handle(vsrc);
			}
			boolean foundDelayed = false;
			do{
				BufferedReader mtrall = new BufferedReader(new StringReader(vsrc));
				vsrc = "";
				foundDelayed = false;
				while ((line = mtrall.readLine()) != null) {
					if(line.contains("#DELAYED")){
						foundDelayed = true;
						line = line.replaceAll("#DELAYED", "#");
					}
					vsrc += line + "\n";
				}
				mtrall.close();
				if(foundDelayed){
					for (int i = 0; i < replace.length; i++) {
						vsrc = replace[i].handle(vsrc);
					}
				}
				
			}while(foundDelayed);
			
		}

		GL20.glShaderSource(v, vsrc);
		GlUtil.printGlErrorCritical();
		GL20.glCompileShader(v);
		GlUtil.printGlErrorCritical();
		int status = GL20.glGetShaderi(v, GL20.GL_COMPILE_STATUS);
		if (status != GL11.GL_TRUE) {
			GlUtil.printGlErrorCritical();
			String glGetShaderInfoLog = GL20.glGetShaderInfoLog(v, GL20.glGetShaderi(v, GL20.GL_INFO_LOG_LENGTH));
			System.err.println(vsrc);
			System.err.println("[SHADER] ERROR COMPILING VERTEX SHADER " + vertexshaderPath + " STATUS: " + status);
			System.err.println("LOG: " + glGetShaderInfoLog);
			throw new ShaderException(vertexshaderPath, glGetShaderInfoLog, vsrc);

		}
		return v;
	}

	public int compileFragmentShader() throws ResourceException, IOException, ShaderException {
		if (fragmentshaderPath == null) {
			return -1;
		}
		int f = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

		String line;
		BufferedReader brf;
		brf = new BufferedReader(new InputStreamReader(
				ResourceLoader.resourceUtil
						.getResourceAsInputStream(fragmentshaderPath)));

		fsrc = "";
		while ((line = brf.readLine()) != null) {
			if (line.contains("#IMPORT")) {
				String file = line.replaceAll("#IMPORT", "").trim();

//				System.err.println("FOUND IMPORT: "+file+"; on "+fragmentshaderPath);
				BufferedReader reader;

				reader = new BufferedReader(new InputStreamReader(
						ResourceLoader.resourceUtil
								.getResourceAsInputStream(file)));
				while ((line = reader.readLine()) != null) {
					fsrc += line + "\n";
				}
				reader.close();
			} else {
				fsrc += line + "\n";
			}
		}
		brf.close();
		if (replace != null) {
			for (int i = 0; i < replace.length; i++) {
				fsrc = replace[i].handle(fsrc);
			}
			boolean foundDelayed = false;
			do{
				BufferedReader mtrall = new BufferedReader(new StringReader(fsrc));
				fsrc = "";
				foundDelayed = false;
				while ((line = mtrall.readLine()) != null) {
					if(line.contains("#DELAYED")){
						foundDelayed = true;
						line = line.replaceAll("#DELAYED", "#");
					}
					fsrc += line + "\n";
				}
				mtrall.close();
				if(foundDelayed){
					for (int i = 0; i < replace.length; i++) {
						fsrc = replace[i].handle(fsrc);
					}
				}
				
			}while(foundDelayed);
			
		}
		

//		byte[] programbytes = fsrc.getBytes();
//		ByteBuffer b = GlUtil.getDynamicByteBuffer(programbytes.length, 0);
//		b.put(programbytes);
//		b.rewind();
		GL20.glShaderSource(f, fsrc);

		GL20.glCompileShader(f);
		int status = GL20.glGetShaderi(f, GL20.GL_COMPILE_STATUS);
		if (status != GL11.GL_TRUE) {


			String glGetShaderInfoLog = GL20.glGetShaderInfoLog(f, GL20.glGetShaderi(f, GL20.GL_INFO_LOG_LENGTH));

			System.err.println("[SHADER] ERROR COMPILING FRAGMENT SHADER " + vertexshaderPath + " STATUS: " + status);

			System.err.println(fsrc);
			System.err.println("LOG: " + glGetShaderInfoLog);

			if (glGetShaderInfoLog.contains("'min' : function not available in current GLSL version - trying implict argument conversion") && fsrc.contains("shadowCoef")) {
				GLFrame.processErrorDialogExceptionWithoutReport(new GraphicsNotSupportedException("Error while compoiling shader. "
						+ "Your graphics card does not support the graphics settings chosen.\n\n"
						+ "Please disable shadows, and try again. Also, if you have an intel card, please try to update your driver."), null);
			}

			throw new ShaderException(fragmentshaderPath, glGetShaderInfoLog, fsrc);

		}
		return f;
	}

	public int compileGeometryShader() throws ResourceException, IOException, ShaderException {
		if (geometryShaderPath == null) {
			return -1;
		}
		int v = GL20.glCreateShader(GL32.GL_GEOMETRY_SHADER);

		BufferedReader brv;
		brv = new BufferedReader(new InputStreamReader(
				ResourceLoader.resourceUtil
						.getResourceAsInputStream(geometryShaderPath)));

		String vsrc = "";
		String line;
		while ((line = brv.readLine()) != null) {
			if (line.contains("#IMPORT")) {
				String file = line.replaceAll("#IMPORT", "").trim();
				BufferedReader reader;
				reader = new BufferedReader(new InputStreamReader(
						ResourceLoader.resourceUtil
								.getResourceAsInputStream(file)));
				while ((line = reader.readLine()) != null) {
					vsrc += line + "\n";
				}

			} else {
				vsrc += line + "\n";
			}
		}
		if (replace != null) {
			for (int i = 0; i < replace.length; i++) {
				vsrc = replace[i].handle(vsrc);
			}
			boolean foundDelayed = false;
			do{
				BufferedReader mtrall = new BufferedReader(new StringReader(vsrc));
				vsrc = "";
				foundDelayed = false;
				while ((line = mtrall.readLine()) != null) {
					if(line.contains("#DELAYED")){
						foundDelayed = true;
						line = line.replaceAll("#DELAYED", "#");
					}
					vsrc += line + "\n";
				}
				mtrall.close();
				if(foundDelayed){
					for (int i = 0; i < replace.length; i++) {
						vsrc = replace[i].handle(vsrc);
					}
				}
				
			}while(foundDelayed);
		}

//		byte[] programbytes = vsrc.getBytes();
//		ByteBuffer b = GlUtil.getDynamicByteBuffer(programbytes.length, 0);
//		b.put(programbytes);
//		b.rewind();
		// System.err.println("VertexProgram: \n\n"+vsrc);
		GL20.glShaderSource(v, vsrc);
		GL20.glCompileShader(v);
		int status = GL20.glGetShaderi(v, GL20.GL_COMPILE_STATUS);
		if (status != GL11.GL_TRUE) {
			int len = GL20.glGetShaderi(v, GL20.GL_INFO_LOG_LENGTH);
			String glGetProgramInfoLog = GL20.glGetShaderInfoLog(v, len);
			System.err.println(vsrc);
			System.err.println("[SHADER] ERROR COMPILING GEOMETRY SHADER " + geometryShaderPath + " STATUS: " + status);
			System.err.println("LOG: " + glGetProgramInfoLog);
			throw new ShaderException(geometryShaderPath, glGetProgramInfoLog, vsrc);
		}
		return v;
	}

	/**
	 * @return the vertexshaderPath
	 */
	public String getVertexshaderPath() {
		return vertexshaderPath;
	}

	/**
	 * @return the fragmentshaderPath
	 */
	public String getFragmentshaderPath() {
		return fragmentshaderPath;
	}

	/**
	 * @return the geometryShaderPath
	 */
	public String getGeometryShaderPath() {
		return geometryShaderPath;
	}

	public int getHandle(String param) {
		Integer handle = handleLookUp.get(param);
		if (handle == null) {
			int newHandle = GL20.glGetUniformLocation(shaderprogram, param);
			handleLookUp.put(param, newHandle);
			//			System.err.println("looked up "+newHandle);
			return newHandle;
		}
		return handle;

	}

	/**
	 * Gets the shader interface.
	 *
	 * @return the shader interface
	 */
	public Shaderable getShaderInterface() {
		return shaderInterface;
	}

	/**
	 * Sets the shader interface.
	 *
	 * @param shaderInterface the new shader interface
	 */
	public void setShaderInterface(Shaderable shaderInterface) {
		this.shaderInterface = shaderInterface;
	}

	/**
	 * Gets the shaderprogram.
	 *
	 * @return the shaderprogram
	 */
	public int getShaderprogram() {
		return shaderprogram;
	}

	/**
	 * Sets the shaderprogram.
	 *
	 * @param shaderprogram the shaderprogram to set
	 */
	public void setShaderprogram(int shaderprogram) {
		this.shaderprogram = shaderprogram;
	}

	/**
	 * Load.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 * @
	 */
	public void load() {

		if (!validated) {
			GlUtil.printGlErrorCritical();
		}

		if (!EngineSettings.G_SHADERS_ACTIVE.isOn() || EngineSettings.G_WIREFRAMED.isOn()) {
			return;
		}
		if (shaderprogram == 0) {
			try {
				throw new GLException("Shader not loaded: " + vertexshaderPath + "; " + fragmentshaderPath);
			} catch (GLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		;
		GL20.glUseProgram(shaderprogram);
		if (!validated) {
			GlUtil.printGlErrorCritical("SHADER: " + shaderprogram + "; " + vertexshaderPath + "; " + fragmentshaderPath);
		}
		GlUtil.loadedShader = this;
		if (shaderInterface != null) {
			shaderInterface.updateShaderParameters(this);
		}
		if (!validated) {
			int current = 0;
			GlUtil.printGlErrorCritical("Shader " + shaderprogram + "; " + vertexshaderPath + "; " + fragmentshaderPath + "; interface " + (shaderInterface != null ? shaderInterface.getClass().toString() : "null"));
			if (EngineSettings.F_FRAME_BUFFER.isOn() || EngineSettings.isShadowOn()) {
				current = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
//				System.err.println("[SHADER] validate: current FBO: " + current);
			}

			GlUtil.printGlErrorCritical();
			if (current != 0) {
				GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			}
			GlUtil.printGlErrorCritical();
			validate();
			validated = true;
			GlUtil.printGlErrorCritical();

			if (current != 0) {
				GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, current);
			}

			GlUtil.printGlErrorCritical();
		}
		//		System.err.println("[SHADER] "+this);
	}

	public void loadWithoutUpdate() {
		if (!EngineSettings.G_SHADERS_ACTIVE.isOn() || EngineSettings.G_WIREFRAMED.isOn()) {
			return;
		}
		GlUtil.loadedShader = this;
		GL20.glUseProgram(shaderprogram);
	}

	/**
	 * Prints the shader info log.
	 *
	 * @param gl the gl
	 * @return the int
	 */
	int printShaderInfoLog() {

		return 0;
	}

	public void reset() {
		handleLookUp.clear();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SHADER[" + vertexshaderPath + ", " + fragmentshaderPath + " bound to " + shaderprogram + " with interface " + shaderInterface + "]";
	}

	/**
	 * Unload.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 */
	public void unload() {
		if (!EngineSettings.G_SHADERS_ACTIVE.isOn() || EngineSettings.G_WIREFRAMED.isOn()) {
			return;
		}
		if (shaderInterface != null) {
			shaderInterface.onExit();
		}
		GlUtil.loadedShader = null;
		GL20.glUseProgram(0);

	}

	public void unloadWithoutExit() {
		if (!EngineSettings.G_SHADERS_ACTIVE.isOn() || EngineSettings.G_WIREFRAMED.isOn()) {
			return;
		}
		GlUtil.loadedShader = null;
		GL20.glUseProgram(0);
	}

	/**
	 * @return the defined
	 */
	public ArrayList<String> getDefined() {
		return defined;
	}

}
