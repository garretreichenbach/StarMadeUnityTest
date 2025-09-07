package org.schema.game.client.view.buildhelper;

import org.schema.schine.common.language.Lng;

public enum BuildHelperVarName {
	
	CIRCLE_RADIUS(new Object(){@Override
	public String toString(){return Lng.str("Radius");}}),
	CIRCLE_X_ROT(new Object(){@Override
	public String toString(){return Lng.str("X-Rot");}}),
	CIRCLE_Y_ROT(new Object(){@Override
	public String toString(){return Lng.str("Y-Rot");}}),
	CIRCLE_Z_ROT(new Object(){@Override
	public String toString(){return Lng.str("Z-Rot");}}), 
	ELIPSOID_RADIUS_X(new Object(){@Override
	public String toString(){return Lng.str("Radius X-Axis");}}),
	ELIPSOID_RADIUS_Y(new Object(){@Override
	public String toString(){return Lng.str("Radius Y-Axis");}}),
	ELIPSOID_RADIUS_Z(new Object(){@Override
	public String toString(){return Lng.str("Radius Z-Axis");}}),
	TORUS_RADIUS(new Object(){@Override
	public String toString(){return Lng.str("Torus Radius");}}),
	TORUS_TUBE_RADIUS(new Object(){@Override
	public String toString(){return Lng.str("Tube Radius");}}),
	TORUS_X_ROT(new Object(){@Override
	public String toString(){return Lng.str("X-Rot");}}),
	TORUS_Y_ROT(new Object(){@Override
	public String toString(){return Lng.str("Y-Rot");}}),
	TORUS_Z_ROT(new Object(){@Override
	public String toString(){return Lng.str("Z-Rot");}}), 
	LINE_SEGMENT(new Object(){@Override
	public String toString(){return Lng.str("Line Segment");}}), 
	LINE_THICKNESS(new Object(){@Override
	public String toString(){return Lng.str("Thickness");}}), 
	
	
	;
	private final Object nm;

	private BuildHelperVarName(Object nm){
		this.nm = nm;
	}
	
	@Override
	public String toString(){
		return nm.toString();
	}
}
