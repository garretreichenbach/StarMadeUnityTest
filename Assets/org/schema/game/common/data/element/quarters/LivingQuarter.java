package org.schema.game.common.data.element.quarters;

import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.resource.tag.Tag;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class LivingQuarter extends Quarter {
	public LivingQuarter(SegmentController s) {
		super(s);
	}

	@Override
	public QuarterType getType() {
		return QuarterType.LIVING;
	}

	@Override
	public int getMaxDim() {
		return 0;
	}

	@Override
	public int getMinCrew() {
		return 0;
	}

	@Override
	public int getMaxCrew() {
		return 0;
	}

	@Override
	public void update(Timer timer) {

	}

	@Override
	public void forceUpdate() {

	}

	@Override
	public ConfigGroup createConfigGroup() {
		return null;
	}

	@Override
	public ConfigGroup createDamagedConfigGroup() {
		return null;
	}

	@Override
	public GUIMainWindow createGUI(SegmentPiece segmentPiece, PlayerState playerState, DialogInput dialogInput) {
		return null;
	}

	@Override
	public Tag toTagExtra() {
		return null;
	}

	@Override
	public void fromTagExtra(Tag tag) {

	}
}
