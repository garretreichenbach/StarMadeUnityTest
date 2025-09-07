/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>MeshGroup</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * MeshGroup.java
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
package org.schema.schine.graphicsengine.forms;

import java.util.HashSet;

import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.physics.Physical;

/**
 * The Class MeshGroup.
 */
public class MeshGroup extends Mesh {

	/**
	 * The split mesh.
	 */
	private MeshGroup splitMesh;

	/**
	 * Static vbo multidraw.
	 *
	 * @param staticMeshes the static meshes
	 * @param mesh         the mesh
	 * @param gl           the gl
	 * @param glu          the glu
	 * @ the error diolog exception
	 */
	public static void staticVBOMultidraw(HashSet<Physical> staticMeshes, Mesh mesh) {
		if (mesh.isInvisible()) {
			return;
		}
		GlUtil.glPushMatrix();
		for (AbstractSceneNode f : mesh.getChilds()) {
			// System.err.println("drawing world childs: "+this.getChilds());
			Mesh.staticVBOMultidraw(staticMeshes, (Mesh) f);
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public void cleanUp() {
		if (splitMesh != null) {
			splitMesh.cleanUp();
		}
		super.cleanUp();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.Mesh#applyPhysics(java.util.HashMap, org.schema.schine.network.objects.NetworkGameEntity)
	 */
	//	@Override
	//	public void applyPhysics(HashMap<Mesh , float[]> map, NetworkEntity n){
	//
	//		if (this.isCollisionObject() && !this.getChilds().isEmpty()
	//				&& this.getChilds().get(0) instanceof Mesh) {
	//			fs = map.get(this.getChilds().get(0));
	////			n.getPhysicsDataContainer().transform.setFromOpenGLMatrix(fs);
	////			System.err.println("applying "+n.x+", "+n.y+", "+n.z+" to "+n);
	//		}
	//		super.applyPhysics(map, n);
	//	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.Mesh#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
		if (!isLoaded()) {
			AbstractScene.infoList.add("Loading " + getName());
			return;
		}
		if (isInvisible()) {

			return;
		}
		GlUtil.glPushMatrix();
		// System.err.println("world transform ");
		transform();
		// while(getChilds().size() <= 1 && parent == null){
		// MeshGroup w = new MeshGroup();
		// w.parent = this;
		// getChilds().add(w);//debug
		// }
		for (AbstractSceneNode f : getChilds()) {
			// System.err.println("drawing world childs: "+this.getChilds());
			//			if(!(f instanceof OldTerrain && i == 0))
			f.draw();
		}
		GlUtil.glPopMatrix();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.Mesh#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
		System.err.println("-- OGL: new MeshGroup created");
	}

	/**
	 * Gets the split mesh.
	 *
	 * @return the split mesh
	 */
	public MeshGroup getSplitMesh() {
		return splitMesh;
	}

	/**
	 * Sets the split mesh.
	 *
	 * @param splitMesh the new split mesh
	 */
	public void setSplitMesh(MeshGroup splitMesh) {
		this.splitMesh = splitMesh;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.Mesh#transform(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void transform() {
		//FIXME so that physics work relative to the world position
		if (!isCollisionObject()) {
			AbstractSceneNode.transform(this);
		}
	}

	public void drawRaw() {
		if (isInvisible()) {

			return;
		}
		GlUtil.glPushMatrix();
		// System.err.println("world transform ");
		transform();
		// while(getChilds().size() <= 1 && parent == null){
		// MeshGroup w = new MeshGroup();
		// w.parent = this;
		// getChilds().add(w);//debug
		// }
		for (AbstractSceneNode f : getChilds()) {
			// System.err.println("drawing world childs: "+this.getChilds());
			//			if(!(f instanceof OldTerrain && i == 0))
			if(f instanceof Mesh){
				GlUtil.glPushMatrix();
				((Mesh)f).transform();
				((Mesh)f).loadVBO(true);
				((Mesh)f).renderVBO();
				((Mesh)f).unloadVBO(true);
				GlUtil.glPopMatrix();
			}else{
				f.draw();
			}
		}
		GlUtil.glPopMatrix();
	}

}
