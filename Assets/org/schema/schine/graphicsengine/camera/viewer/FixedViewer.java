package org.schema.schine.graphicsengine.camera.viewer;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Transformable;

import com.bulletphysics.linearmath.Transform;

public class FixedViewer extends AbstractViewer {

	protected Transform transform;
	private Transformable entity;
	private Vector3f position = new Vector3f();

	public FixedViewer(Transformable entity) {
		setEntity(entity);
		//		tmpTrans.setIdentity();
	}

	public Transformable getEntity() {
		return entity;
	}

	/**
	 * @param entity the entity to set
	 */
	public void setEntity(Transformable entity) {
		assert (entity != null);
		this.entity = entity;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.CameraPositionable#getPosition()
	 */
	@Override
	public Vector3f getPos() {
		transform = entity.getWorldTransform();
		if (transform != null) {
			position.set(transform.origin);
		} else {
			position.set(0, 0, 0);
		}
		return position;
	}

	//	private Transform tmpTrans = new Transform();
	@Override
	public void update(Timer timer) {
		//		Vector3f v = new Vector3f(tmpTrans.origin);
		//		v.sub(entity.getWorldTransform().origin);
		//		if(v.length() > 10){
		//			try{
		//			throw new IllegalAccessError();
		//			}catch(IllegalAccessError e){
		//				System.err.println("OLD MATRIX");
		//				System.err.println(tmpTrans.getMatrix(new Matrix4f()));
		//				System.err.println("EVIL MATRIX");
		//				System.err.println(entity.getWorldTransform().getMatrix(new Matrix4f()));
		//				e.printStackTrace();
		//			}
		//		}
		//		tmpTrans.set(entity.getWorldTransform());
	}

}
