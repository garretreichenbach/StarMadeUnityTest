package org.schema.game.client.view.gui.faction;

import java.util.ArrayList;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public abstract class FactionDescriptionPanel extends GUIElement implements GUIChangeListener {

	private int width;
	private int height;
	private GUIAnchor descScrollPanel;
	private GUITextOverlay desc;
	private String currentDesc;
	private boolean updateNeeded;

	public FactionDescriptionPanel(InputState state, int width, int height) {
		super(state);
		this.width = width;
		this.height = height;

		((GameClientState) getState()).getFactionManager().obs.addObserver(this);
	}

	@Override
	public void cleanUp() {
		((GameClientState) getState()).getFactionManager().obs.deleteObserver(this);
	}

	@Override
	public void draw() {
		if (updateNeeded) {
			currentDesc = getCurrentDesc();
			desc.setText(new ArrayList());
			String[] split = currentDesc.split("\\\\n");
			desc.getText().clear();
			for (int i = 0; i < split.length; i++) {
				desc.getText().add(split[i]);
			}
			updateNeeded = false;
		}

		drawAttached();
	}

	@Override
	public void onInit() {
		descScrollPanel = new GUIAnchor(getState(), width, height);
		desc = new GUITextOverlay(getState());

		currentDesc = getCurrentDesc();
		desc.setText(new ArrayList());
		String[] split = currentDesc.split("\\\\n");
		for (int i = 0; i < split.length; i++) {
			desc.getText().add(split[i]);
		}
		descScrollPanel.attach(desc);

		this.attach(descScrollPanel);

	}

	public abstract String getCurrentDesc();

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}


	@Override
	public void onChange(boolean updateListDim) {
		this.updateNeeded = true;		
	}

}
