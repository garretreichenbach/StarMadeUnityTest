package org.schema.game.client.view.mainmenu;

import org.schema.game.client.view.mainmenu.gui.FileChooserStats;
import org.schema.game.client.view.mainmenu.gui.GUIFileChooserPanel;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class FileChooserDialog extends DialogInput{

	private final GUIFileChooserPanel p;
	public FileChooserDialog(InputState state, FileChooserStats stats) {
		super(state);
		p = new GUIFileChooserPanel(state, this, stats);
		p.onInit();
		stats.diag = this;
	}


	@Override
	public GUIElement getInputPanel() {
		return p;
	}

	@Override
	public void onDeactivate() {
		p.cleanUp();
	}

	public void onSelectedChanged(String selectedName) {
		p.onFileNameChanged(selectedName);
	}
	

}
