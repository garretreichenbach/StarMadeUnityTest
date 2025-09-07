package org.schema.game.client.view.gui.faction;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.vecmath.Vector4f;

import org.schema.game.common.data.player.faction.FactionNewsPost;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class FactionNewsPostGUIListEntry extends GUIListElement {

	private GUIColoredRectangle bg;
	private GUIColoredRectangle bgHead;
	private FactionNewsPost newsPost;

	public FactionNewsPostGUIListEntry(InputState state, FactionNewsPost f, int index) {
		super(state);

		bg = new GUIColoredRectangle(getState(), 540, 100,
				index % 2 == 0 ? new Vector4f(0.1f, 0.1f, 0.1f, 1) : new Vector4f(0.2f, 0.2f, 0.2f, 1)
		);

		this.setContent(bg);

		this.setSelectContent(bg);
		this.newsPost = f;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIListElement#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();

		GUITextOverlay op = new GUITextOverlay(getState());
		GUITextOverlay date = new GUITextOverlay(getState());
		GUITextOverlay message = new GUITextOverlay(getState());

		bgHead = new GUIColoredRectangle(getState(), 550, 20, new Vector4f(0.3f, 0.3f, 0.4f, 0.8f));

		op.setTextSimple(newsPost.getOp());
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date(newsPost.getDate()));
		date.setTextSimple(c.getTime().toString());
		message.setText(new ArrayList());

		String[] split = newsPost.getMessage().split("\\\\n");

		for (String m : split) {
			message.getText().add(m);
		}
		bgHead.attach(op);
		bgHead.attach(date);
		date.getPos().x = 300;
		message.getPos().y = 20;

		bg.attach(bgHead);
		bg.attach(message);
	}

}
