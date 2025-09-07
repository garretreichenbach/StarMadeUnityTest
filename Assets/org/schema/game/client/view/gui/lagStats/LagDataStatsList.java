package org.schema.game.client.view.gui.lagStats;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.gui.newgui.StatisticsGraphListInterface;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class LagDataStatsList extends ObjectArrayList<LagDataStatsEntry> implements StatisticsGraphListInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final Vector4f color;
	public GUILagObjectsScrollableList scollableList;

	public LagDataStatsList(Vector4f color) {
		super();
		this.color = color;
	}

	@Override
	public int getSize() {
		return size();
	}

	@Override
	public double getAmplitudePercentAtIndex(int i, long maxAmplitude) {
		return (double) get(i).volume / (double) maxAmplitude;
	}

	@Override
	public long getAmplitudeAtIndex(int i) {
		return get(i).volume;
	}

	@Override
	public long getTimeAtIndex(int i) {
		return get(i).time;
	}

	@Override
	public int getStartIndexFrom(long time) {
		if (size() == 0) {
			return -1;
		}
		if (size() == 1) {
			return 0;
		}
		int def = 0;
		for (int i = size() - 1; i > 0; i--) {
			assert (get(i).time < get(i - 1).time);
			if (get(i).time < time && get(i - 1).time > time) {
				return i;
			}
		}
		//everything is smaller
		return size() - 1;
	}

	@Override
	public int getClosestIndexFrom(long time) {
		if (size() == 0) {
			return -1;
		}
		if (size() == 1) {
			return 0;
		}
		int def = 0;
		for (int i = size() - 1; i > 0; i--) {
			assert (get(i).time < get(i - 1).time);
			if (get(i).time < time && get(i - 1).time > time) {
				if (Math.abs(get(i - 1).time - time) < Math.abs(get(i).time - time)) {
					return i - 1;
				}
				return i;
			}
		}
		//everything is smaller
		return size() - 1;
	}

	@Override
	public int getEndIndexFrom(long time) {
		return getStartIndexFrom(time);
	}

	@Override
	public long getMaxAplitude(long startTime, long endTime) {
		int startIndexFrom = 0;
		int endIndexFrom = getStartIndexFrom(startTime);
		long highest = 0;
		for (int i = startIndexFrom; i <= endIndexFrom; i++) {
			highest = Math.max(highest, getAmplitudeAtIndex(i));
		}
		return highest;
	}

	/**
	 * @return the color
	 */
	@Override
	public Vector4f getColor() {
		return color;
	}

	@Override
	public void select(int clickIndex) {
		if (clickIndex > 0 && clickIndex < size()) {
			if (get(clickIndex).selected) {
				deselectAll();
			} else {
				deselectAll();
				get(clickIndex).selected = true;
			}
		}
	}

	@Override
	public boolean isSelected(int clickIndex) {
		return clickIndex > 0 && clickIndex < size() && get(clickIndex).selected;
	}

	@Override
	public void notifyGUI() {
		scollableList.notifyGUIFromDataStatistics();
	}

	void deselectAll() {
		for (int i = 0; i < size(); i++) {
			get(i).selected = false;
		}
	}

	public void clearAllBefore(long time) {
		for(int i = 0; i < size; i++){
			if(get(i).time < time){
				remove(i);
				i--;
			}
		}
	}

	public boolean isAnySelected() {
		for(int i = 0; i < size; i++){
			if(get(i).selected){
				return true;
			}
		}
		return false;
	}

	public LagDataStatsEntry getSelected() {
		for(int i = 0; i < size; i++){
			if(get(i).selected){
				return get(i);
			}
		}
		return null;
	}

}
