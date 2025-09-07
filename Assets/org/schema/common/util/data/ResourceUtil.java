/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ResourceUtil</H2>
 * <H3>org.schema.common.util.data</H3>
 * ResourceUtil.java
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
package org.schema.common.util.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.resource.FileExt;


/**
 * The Class ResourceUtil.
 */
public class ResourceUtil {

	

	/**
	 * Gets the classe names in package.
	 *
	 * @param jarName     the jar description
	 * @param packageName the package description
	 * @return the classe names in package
	 */
	public List<Class<?>> getClasseInPackage(String jarName,
	                                         String packageName) {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		//		System.err.print("[RESOURCE] [JAR-Package] "+jarName+", package: "+packageName);
		packageName = packageName.replaceAll("\\.", "/");
		try {

			URL url = getResourceURL(packageName);
			JarFile jarFile = null;
			URLConnection con = url.openConnection();
			if (con instanceof JarURLConnection) {
				//				System.err.println("[JAR] (normal)");
				JarURLConnection u = (JarURLConnection) con;
				jarFile = u.getJarFile();
			} else {
				//				System.err.println("[JAR] (hacked) "+con);
				System.setSecurityManager(null);
				Class<?> jnlpConnectionClass = Class.forName("com.sun.jnlp.JNLPCachedJarURLConnection");

				Method jarGetJarFile;
				try {

					//		        	System.err.println("fields");
					Field[] fs = jnlpConnectionClass.getFields();
					for (Field f : fs) {
						System.err.println(f);
					}
					//		        	System.err.println("methods");
					Method[] ms = jnlpConnectionClass.getMethods();
					for (Method m : ms) {
						System.err.println(m);
					}
					jarGetJarFile = jnlpConnectionClass.getDeclaredMethod("getJarFile");
					//					Method jarURL = jnlpConnectionClass.getDeclaredMethod("getJarFileURL");
					//					Method jarEntry = jnlpConnectionClass.getDeclaredMethod("getJarEntry");

					//		        	System.err.println("URL: "+jarURL.invoke(con));
					jarGetJarFile.setAccessible(true);
					//					JarEntry jarE = (JarEntry)jarEntry.invoke(jnlpConnectionClass.cast(con));
					//		        	System.err.println("Entry: "+jarE);
					jarFile = (JarFile) jarGetJarFile.invoke(jnlpConnectionClass.cast(con));
					//		            System.err.println(jarGetJarFile.getName()+"    "+jarGetJarFile.getReturnType());
					//		            System.err.println(jarFile);

				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			JarEntry jarEntry;
			Enumeration<JarEntry> en = jarFile.entries();
			while (en.hasMoreElements()) {
				jarEntry = en.nextElement();
				if (jarEntry == null) {
					break;
				}
				if ((jarEntry.getName().startsWith(packageName))
						&& (jarEntry.getName().endsWith(".class"))) {
					Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(jarEntry.getName().replaceAll("/", "\\.").replaceAll("\\.class", ""));
					classes.add(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classes;
	}

	/**
	 * Gets the file names in package.
	 *
	 * @param jarName     the jar description
	 * @param packageName the package description
	 * @return the file names in package
	 */

	public List<String> getFileNamesInPackage(String jarName,
	                                          String packageName) {
		ArrayList<String> classes = new ArrayList<String>();
		//		System.err.print("[RESOURCE] [PACKAGE] ");
		packageName = packageName.replaceAll("\\.", "/");
		try {

			URL url = getResourceURL(packageName);
			URLConnection con = url.openConnection();
			//			if(con instanceof FileURLConnection){
			//
			//				((FileURLConnection) con).close();
			//				File f = new FileExt(url.getFile());
			//				File[] fs = f.listFiles();
			////				System.err.println("[FILES] <"+fs.length+"> "+Arrays.toString(fs));
			//				for(File file : fs){
			//					classes.add(file.getAbsolutePath());
			//				}
			//
			//			}
			//			else
			if (con instanceof JarURLConnection) {
				//				System.err.println("[JAR] (normal)");
				JarURLConnection u = (JarURLConnection) con;
				JarFile jarFile = u.getJarFile();
				JarEntry jarEntry;
				Enumeration<JarEntry> en = jarFile.entries();
				while (en.hasMoreElements()) {
					jarEntry = en.nextElement();
					if (jarEntry == null) {
						break;
					}
					if ((jarEntry.getName().startsWith(packageName))) {
						classes.add(jarEntry.getName());
					}
				}
			} else {
				//				System.err.println("[JAR] (hacked)");
				Class<?> jnlpConnectionClass = Class.forName("com.sun.jnlp.JNLPCachedJarURLConnection");
				//				Field jarFileField;
				Method jarGetJarFile;
				try {
					jarGetJarFile = jnlpConnectionClass.getDeclaredMethod("getJarFile");
					//					jarFileField = jnlpConnectionClass.getDeclaredField("jarFile");

					JarFile f = (JarFile) jarGetJarFile.invoke(con);
					System.err.println(f);
				} catch (Throwable t) {
					//					jarFileField = jnlpConnectionClass.getDeclaredField("_jarFile");
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return classes;
	}

	/**
	 * Gets the resource as input streamBuffer.
	 *
	 * @param description the description
	 * @return the resource as input streamBuffer
	 * @throws ResourceException the resource exception
	 */
	public InputStream getResourceAsInputStream(String name) throws ResourceException {
		name = name.replaceAll("\\\\", "/");
		name = name.replaceAll("\\./", "");
		while (name.contains("//")) {
			name = name.replaceAll("//", "/");
		}

		//			System.err.print("[RESOURCE] fetch: "+description);
		InputStream in = null;
		{
			//				System.err.println("Trying this Thread");
			in = this.getClass().getClassLoader().getResourceAsStream(name);
			//				System.err.println("Root of Thread CL: "+Thread.currentThread().getContextClassLoader().getResource(""));
		}
		
		if (in == null) {
			//	        	System.err.println("---");
			//	        	System.err.println("This Classes Classloader not working! Trying System Classloader");
			//	        	System.err.println("Root of System CL: "+ClassLoader.getSystemResource(""));
			in = ClassLoader.getSystemResourceAsStream(name);
		}
		if (in == null) {
			File f = new FileExt(name);
			if (f.exists()) {
				try {
					in = new FileInputStream(f);
				} catch (FileNotFoundException e) {

					e.printStackTrace();
				}

			} else {
				System.err.println("[WARNING][ResourceLoader] File Does Not exist: " + name);
			}
		}
		if (in == null) {
			throw new ResourceException("[WARNING][ResourceLoader] Resource not found: '" + name+"'");
		}
		//	        System.err.println(" --> AS STREAM. DONE.");
		return new BufferedInputStream(in, 4096);
	}

	/**
	 * Gets the resource url.
	 *
	 * @param description the description
	 * @return the resource url
	 * @throws ResourceException the resource exception
	 */
	public URL getResourceURL(String name) throws ResourceException {
		name = name.replaceAll("\\./", "");
		name = name.replaceAll("\\\\", "");
		while (name.contains("//")) {
			name = name.replaceAll("//", "/");
		}
		//			System.err.println("[RESOURCE] fetch: "+description);
		URL url = null;
		{
			//				System.err.println("Trying this Thread");
			url = this.getClass().getClassLoader().getResource(name);
			//				System.err.println("Root of Thread CL: "+Thread.currentThread().getContextClassLoader().getResource(""));
		}
		
		if (url == null) {
			//	        	System.err.println("---");
			//	        	System.err.println("This Classes Classloader not working! Trying System Classloader");
			//	        	System.err.println("Root of System CL: "+ClassLoader.getSystemResource(""));
			url = ClassLoader.getSystemResource(name);
		}
		if (url == null) {
			File f = new FileExt(name);
			if (f.exists()) {
				try {
					url = new URL("file:" + f.getAbsolutePath());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
		if (url == null) {
			throw new ResourceException("[WARNING][ResourceLoader] Resource not found: " + name);
		}
		//	        System.err.println(" --> "+url+" DONE.");
		return url;
	}

	public void writeToFile(InputStream is, File file) throws IOException {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		InputStream is2 = is;
		boolean again = true;
		while (again) {
			if (is2.read() > -1) {
				out.writeByte(is.read());
			} else {
				again = false;
			}
		}
		is.close();
		out.close();
	}
}
