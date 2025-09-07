package org.schema.schine.physics;

import org.schema.schine.graphicsengine.forms.MeshGroup;

public interface PhysicableMeshNode extends Physical {

	public String getMeshMode();

	public MeshGroup getSceneNode();

}
