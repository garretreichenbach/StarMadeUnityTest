package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector4f;

public interface StatisticsGraphListInterface {
	public int getSize();

	public double getAmplitudePercentAtIndex(int i, long maxAmplitude);

	public long getAmplitudeAtIndex(int i);

	public long getTimeAtIndex(int i);

	public int getStartIndexFrom(long time);

	public int getClosestIndexFrom(long time);

	public int getEndIndexFrom(long time);

	public long getMaxAplitude(long startTime, long endTime);

	public Vector4f getColor();

	public void select(int clickIndex);

	public boolean isSelected(int clickIndex);

	public void notifyGUI();

}
