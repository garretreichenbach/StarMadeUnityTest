package org.schema.game.client.view.gui.shiphud.newhud;

import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer.PowerConsumerCategory;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.controller.elements.power.reactor.ReactorPriorityQueue;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.input.InputState;

public class PowerStabilizationBar extends FillableVerticalBar {

	@ConfigurationElement(name = "Color")
	public static Vector4i COLOR;

	@ConfigurationElement(name = "ColorWarn")
	public static Vector4i COLOR_WARN;

	@ConfigurationElement(name = "Offset")
	public static Vector2f OFFSET;

	@ConfigurationElement(name = "FlipX")
	public static boolean FLIPX;
	@ConfigurationElement(name = "FlipY")
	public static boolean FLIPY;

	@ConfigurationElement(name = "FillStatusTextOnTop")
	public static boolean FILL_ON_TOP;

	@ConfigurationElement(name = "Position")
	public static GUIPosition POSITION;

	private long lastUpdate;
	
	public PowerStabilizationBar(InputState state) {
		super(state);
	}

	@Override
	public boolean isBarFlippedX() {
		return FLIPX;
	}

	@Override
	public boolean isBarFlippedY() {
		return FLIPY;
	}

	@Override
	public boolean isFillStatusTextOnTop() {
		return FILL_ON_TOP;
	}

	@Override
	protected boolean isLongerBar() {
		return true;
	}

	@Override
	public float getFilled() {
		final PowerInterface pi = getPI();
		if(pi == null){
			return 0;
		}
		return (float) pi.getPowerAsPercent();
	}
	@Override
	public void draw() {

		GlUtil.glPushMatrix();
		transform();

		textDesc.setColor(1, 1, 1, 1);
		text.setColor(1, 1, 1, 1);
		text.setPos(getTextOffsetX()-text.getMaxLineWidth(), getTextOffsetY(), 0);
		textDesc.setPos(getTextOffsetX()-Math.max(150, textDesc.getMaxLineWidth()), getTextOffsetY()+40, 0);
		
		ShaderLibrary.powerBarShader.setShaderInterface(this);
		ShaderLibrary.powerBarShader.load();
		barSprite.draw();
		ShaderLibrary.powerBarShader.unload();
		text.draw();
		textDesc.draw();
		GlUtil.glPopMatrix();
	}
	
	@Override
	public void update(Timer timer) {
		super.update(timer);
		
		if(timer.currentTime - lastUpdate > 60){
			textDesc.getText().set(0, getText());
			text.getText().set(0, getBarName()+" "+StringTools.formatPointZero(getFilled() * 100f) + "%");
			lastUpdate = timer.currentTime;
		}
	}

	@Override
	public String getText() {
		final PowerInterface pi = getPI();
		float filled = getFilled();
		if(filled >= 0.95f || pi == null){
			return Lng.str("All Systems Up");
		}else {
			StringBuffer b = new StringBuffer();
			b.append(Lng.str("Power Instability!\n"));
			ReactorPriorityQueue ppi = pi.getPowerConsumerPriorityQueue();
			List<PowerConsumerCategory> queue = ppi.getQueue();
			int maxDisplay = 5;
			int displayed = 0;
			for(PowerConsumerCategory p : queue){
				double percent = ppi.getPercent(p);
				
				if(ppi.getConsumption(p) > 0 && percent < 1d){
					if(displayed < maxDisplay){
						b.append(Lng.str("%s at %s%%",p.getName(), StringTools.formatPointZero(percent*100d))+"\n");
					}
					displayed++;
				}
			}
			if(displayed > maxDisplay){
				b.append(Lng.str("...%s more", (displayed - maxDisplay))+"\n");
			}
			return b.toString(); 
		}
	}
	public PowerInterface getPI(){
		SimpleTransformableSendableObject<?> currentPlayerObject = ((GameClientState) getState()).getCurrentPlayerObject();
		if(currentPlayerObject == null || !(currentPlayerObject instanceof ManagedSegmentController<?>)){
			return null;
		}
		return ((ManagedSegmentController<?>)currentPlayerObject).getManagerContainer().getPowerInterface();
	}
	@Override
	public Vector4i getConfigColor() {
		return COLOR;
	}
	@Override
	public Vector4i getConfigColorWarn() {
		return COLOR_WARN;
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
	protected String getTag() {
		return "PowerStabilizationBar";
	}

	public void resetDrawn() {
		
	}

	public void drawNoReactorText() {
		GlUtil.glPushMatrix();
		transform();
		textDesc.getText().set(0, Lng.str("No Power Reactor"));
		textDesc.setPos(getTextOffsetX()-textDesc.getMaxLineWidth(), getTextOffsetY()+40, 0);
		textDesc.draw();
		GlUtil.glPopMatrix();
	}
	public void drawText() {
	}

	@Override
	public double getFilledMargin() {
		return 0.95d;
	}

	@Override
	protected String getBarName() {
		return Lng.str("Power/Consumption\n");
	}


	@Override
	protected int getTextOffsetX() {
		return -20;
	}

	@Override
	protected int getTextOffsetY() {
		return 30;
	}

	@Override
	protected Vector4f getColor(float filled) {
		return filled > 0.95 ? color : colorWarn;
	}

}
