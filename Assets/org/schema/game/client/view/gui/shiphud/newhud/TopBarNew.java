package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.PlayerPanel;
import org.schema.game.client.view.gui.TopBarInterface;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;

public class TopBarNew extends HudConfig implements TopBarInterface {
	@ConfigurationElement(name = "Color")
	public static Vector4i COLOR;

	@ConfigurationElement(name = "Position")
	public static GUIPosition POSITION;

	@ConfigurationElement(name = "Offset")
	public static Vector2f OFFSET;

	@ConfigurationElement(name = "ShopX")
	public static float SHOP_X;
	@ConfigurationElement(name = "MailX")
	public static float MAIL_X;
	@ConfigurationElement(name = "TradeX")
	public static float TRADE_X;

	private GUIOverlay iconShop;

	private GUIOverlay iconMail;

	private GUIOverlay iconTrading;

	private BuffDebuff buffDebuff;

	private PlayerPanel panel;

	public TopBarNew(InputState state, PlayerPanel panel) {
		super(state);
		width = UIScale.getUIScale().scale(500);
		height = UIScale.getUIScale().scale(20);
		this.panel = panel;
	}

	@Override
	public Vector4i getConfigColor() {
		return COLOR;
	}

	@Override
	public GUIPosition getConfigPosition() {
		return POSITION;
	}

	@Override
	public Vector2f getConfigOffset() {
		return OFFSET;
	}

	@Override
	public void updateOrientation() {
		super.updateOrientation();

		if (panel.isPanelActive()) {
			getPos().y += 24;
		}
	}

	@Override
	protected String getTag() {
		return "TopBar";
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		updateOrientation();
		buffDebuff.update(timer);
		width = UIScale.getUIScale().scale(500);
		height = UIScale.getUIScale().scale(20);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();

		buffDebuff = new BuffDebuff(getState()) {

			/* (non-Javadoc)
			 * @see org.schema.game.client.view.gui.shiphud.newhud.BuffDebuff#draw()
			 */
			@Override
			public void draw() {
				if (((GameClientState) getState()).getShip() != null) {
					super.draw();
				}
			}

		};

		this.iconShop = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_Sprites-8x8-gui-"), getState()) {

			@Override
			public void draw() {
				if (!((GameClientState) getState()).isInShopDistance()) {
					getSprite().getTint().set(0.5f, 0.5f, 0.5f, 1.0f);
				} else {
					getSprite().getTint().set(1f, 1f, 1f, 1.0f);
				}
				super.draw();
			}			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#onInit()
			 */
			@Override
			public void onInit() {
				super.onInit();
				getSprite().setTint(new Vector4f(1, 1, 1, 1));
			}



		};
		this.iconMail = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_Sprites-8x8-gui-"), getState()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#onInit()
			 */
			@Override
			public void onInit() {
				super.onInit();
				getSprite().setTint(new Vector4f(1, 1, 1, 1));
			}

			@Override
			public void draw() {
				if (!((GameClientState) getState()).getController().getClientChannel().getPlayerMessageController().hasUnreadMessages()) {
					getSprite().getTint().set(0.5f, 0.5f, 0.5f, 1.0f);
				} else {
					getSprite().getTint().set(1f, 1f, 1f, 1.0f);
				}
				super.draw();
			}

		};
		this.iconTrading = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_Sprites-8x8-gui-"), getState()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIOverlay#onInit()
			 */
			@Override
			public void onInit() {
				super.onInit();
				getSprite().setTint(new Vector4f(1, 1, 1, 1));
			}

			@Override
			public void draw() {
				getSprite().getTint().set(0.5f, 0.5f, 0.5f, 1.0f);
				super.draw();
			}

		};

		GUITextOverlay shopText = new GUITextOverlay(FontSize.SMALL_15, getState()) {
			@Override
			public void draw() {
				if (((GameClientState) getState()).isInShopDistance()) {
					super.draw();
				}
			}

		};
		
		GUITextOverlay tutorialText = new GUITextOverlay(FontSize.SMALL_15, getState()) {
			@Override
			public void draw() {
				if (EngineSettings.TUTORIAL_NEW.isOn()) {
					super.draw();
				}
			}

		};
		GUITextOverlay mailText = new GUITextOverlay(FontSize.SMALL_15, getState()) {

			@Override
			public void draw() {
				if (((GameClientState) getState()).getController().getClientChannel().getPlayerMessageController().hasUnreadMessages()) {
					super.draw();
				}
			}
		};
		GUITextOverlay tradeText = new GUITextOverlay(FontSize.SMALL_15, getState()) {
			@Override
			public void draw() {
			}

		};

		shopText.setTextSimple(new Object() {
			@Override
			public String toString() {
				return "'" + KeyboardMappings.SHOP_PANEL.getKeyChar() + "'";
			}

		});
		tutorialText.setTextSimple(new Object() {
			@Override
			public String toString() {
				return Lng.str("Tutorial '%s'", KeyboardMappings.TUTORIAL.getKeyChar());
			}
			
		});
		mailText.setTextSimple(new Object() {
			@Override
			public String toString() {
				return "'F4'";
			}

		});
		tradeText.setTextSimple(new Object() {
			@Override
			public String toString() {
				return "";
			}

		});
		shopText.getPos().x = 9;
		shopText.getPos().y = 32;
		mailText.getPos().x = 3;
		mailText.getPos().y = 32;
		tradeText.getPos().x = 3;
		tradeText.getPos().y = 32;
		
		tutorialText.setPos(-94, 4, 0);

		iconShop.attach(shopText);
		iconMail.attach(mailText);
		iconTrading.attach(tradeText);
		iconShop.attach(tutorialText);

		this.iconShop.onInit();
		this.iconMail.onInit();
		this.buffDebuff.onInit();
		this.iconTrading.onInit();
		this.iconShop.setSpriteSubIndex(40);
		this.iconMail.setSpriteSubIndex(41);
		this.iconTrading.setSpriteSubIndex(42);

		iconShop.getPos().x = SHOP_X;
		iconMail.getPos().x = MAIL_X;
		iconTrading.getPos().x = TRADE_X;

		attach(iconShop);
		attach(iconMail);
		attach(iconTrading);
		attach(buffDebuff);
		orientate(POSITION.value);
		getPos().x += OFFSET.x;
		getPos().y += OFFSET.y;
	}

	@Override
	public void updateCreditsAndSpeed() {
		orientate(POSITION.value);
		getPos().x += OFFSET.x;
		getPos().y += OFFSET.y;
	}

	@Override
	public void notifyEffectHit(SimpleTransformableSendableObject obj,
	                            OffensiveEffects offensiveEffects) {
		buffDebuff.notifyEffectHit(obj, offensiveEffects);
	}



}
