package org.schema.game.common.controller.ntsmothers;

public class TimedTransformationList {
	private final TimedTransformation[] list;
	private int startPointer = 0;
	private int endPointer = 0;

	public TimedTransformationList(int size) {
		list = new TimedTransformation[size];
		;
		for (int i = 0; i < size; i++) {
			list[i] = new TimedTransformation();
		}
	}

	public TimedTransformation getLast() {
		return list[endPointer];
	}

	public boolean isEmpty() {
		return startPointer == endPointer;
	}

}
