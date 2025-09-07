package org.schema.game.common.controller;

public interface TransientSegmentController {

	public boolean isTouched();

	public void setTouched(boolean b, boolean checkEmpty);

	public boolean isMoved();

	public void setMoved(boolean b);

	public boolean needsTagSave();
}
