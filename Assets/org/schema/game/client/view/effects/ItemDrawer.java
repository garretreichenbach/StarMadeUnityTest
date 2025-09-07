package org.schema.game.client.view.effects;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.resource.ResourceLoader;

import java.util.ArrayList;

public class ItemDrawer implements Drawable {

	protected GameClientState state;
	protected boolean init;
	protected Sprite sprite[];

	public ItemDrawer(GameClientState state) {
		this.state = state;

	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {

		if (!init) {
			onInit();
		}
		RemoteSector currentRemoteSector = state.getCurrentRemoteSector();
		if (currentRemoteSector == null || currentRemoteSector.getItems().isEmpty()) {
			//			System.err.println("REMOTE SECTOR IS NULL");
			return;
		}

		for (int i = 0; i < sprite.length; i++) {
			Sprite sprite = this.sprite[i];

			sprite.setScale(0.01f, 0.01f, 0.01f);
			sprite.setFlip(true);
			sprite.setBillboard(true);

			Sprite.draw3D(sprite, currentRemoteSector.getItems().values(), Controller.getCamera());

			sprite.setBillboard(false);
			sprite.setFlip(false);
			sprite.setScale(1f, 1f, 1f);
		}
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	//INSERTED CODE (Replaced method) - Built/meta icons are loaded dynamically
	public void onInit() {
		ResourceLoader resLoader = Controller.getResLoader();
		ArrayList<Sprite> sprites = new ArrayList<>();
		for (String s : resLoader.getImageLoader().getSpriteMap().keySet()) {
			if(s.startsWith("build-icons-") || s.startsWith("meta-icons-")){
				sprites.add(resLoader.getSprite(s));
			}
		}
		this.sprite = sprites.toArray(new Sprite[0]);
		this.init = true;
	}

}
