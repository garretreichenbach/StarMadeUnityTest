package org.schema.game.client.view.gui.catalog;

import javax.vecmath.Vector4f;

import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.input.InputState;

public class CatalogRateDialogPanel extends GUIInputPanel {

	private GUICallback selectedCallback;

	public CatalogRateDialogPanel(InputState state, GUICallback guiCallback, CatalogPermission p) {
		super("CatalogRateDialogPanel", state, guiCallback, "Rate", "Rate " + p.getUid());
		setOkButton(false);
		this.selectedCallback = guiCallback;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();

		for (int i = 0; i < 10; i++) {
			float p = i / 10f;
			GUITextButton b = new GUITextButton(
					getState(), 30, 30,
					new Vector4f(1f - p, p, 0.2f, 1.0f),
					new Vector4f(1, 1, 1, 1),
					FontSize.BIG_24, String.valueOf(i + 1), selectedCallback);
			b.setUserPointer(i);
			if (i < 9) {
				b.setTextPos(6, 0);
			}
			int y = 35;

			int x = (i * 40) /*% (5*40)*/;

			b.setPos(x, y, 0);
			getContent().attach(b);
		}

	}

}
