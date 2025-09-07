package org.schema.game.server.ai.behaviortree.condition;

import javax.vecmath.Vector4f;

public enum BTConditionState {
	READY(new Vector4f(0,0,0,1)),
	RUNNIG(new Vector4f(0,0,1,1)),
	FAILURE(new Vector4f(1,0,0,1)),
	SUCCESS(new Vector4f(0,1,0,1))
	;
	
	private BTConditionState(Vector4f color){
	}
}
