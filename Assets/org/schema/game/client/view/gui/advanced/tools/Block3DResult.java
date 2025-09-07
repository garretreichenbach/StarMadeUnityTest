package org.schema.game.client.view.gui.advanced.tools;

import javax.vecmath.Vector4f;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

public abstract class Block3DResult extends AdvResult<Block3DCallback>{
	
	private short currentType;
	private int orientation;
	
	public Block3DResult(){
	}
	
	public short getType(){
		return currentType;
	}
	@Override
	protected void initDefault() {
		change(getDefaultType());
		changeOrientation(getDefaultOrientation());
	}
	@Override
	public String getName() {
		return isBlockSelected() ? "-" : getInfo().getName();
	}
	public void change(short to){
		short val = currentType;
		
		currentType = to;
		
		if(val != currentType){
			if(callback != null){
				callback.onTypeChanged(currentType);
			}
		}
	}
	public void changeOrientation(int to){
		int val = orientation;
		
		orientation = to;
		
		if(val != orientation){
			if(callback != null){
				callback.onOrientationChanged(orientation);
			}
		}
	}
	public int getCurrentBuildIconNum(){
		return isBlockSelected() ? getInfo().getBuildIconNum() : 0; 
	}
	public abstract short getDefaultType();
	public abstract int getDefaultOrientation();

	public ElementInformation getInfo() {
		return ElementKeyMap.getInfoFast(currentType);
	}
	public boolean isBlockSelected() {
		return ElementKeyMap.isInit() && ElementKeyMap.exists(currentType);
	}

	public Vector4f getBackgroundColor() {
		return HALF_TRANS;
	}

	public int getOrientation() {
		return orientation;
	}

}
