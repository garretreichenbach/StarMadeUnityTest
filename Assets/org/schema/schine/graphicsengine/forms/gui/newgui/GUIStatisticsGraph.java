package org.schema.schine.graphicsengine.forms.gui.newgui;

import java.text.SimpleDateFormat;

import org.lwjgl.opengl.GL11;
import org.schema.common.util.StringTools;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.DropDownCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public abstract class GUIStatisticsGraph extends GUIElement implements GUICallback, DropDownCallback {

	static SimpleDateFormat dateFormat = StringTools.getSimpleDateFormat(Lng.str("HH:mm:ss"), "HH:mm:ss");

	private static long timeFrameInMS = 2 * 60000;

	protected final GUITabbedContent ps;

	GUITextOverlay maxText;

	GUITextOverlay startTimeText;

	GUITextOverlay endTimeText;

	private GUIElement dependend;

	private StatisticsGraphListInterface[] e;

	private boolean[] selected;

	public GUIStatisticsGraph(InputState state, GUITabbedContent ps, GUIElement dependend, StatisticsGraphListInterface... e) {
		super(state);
		this.dependend = dependend;
		this.e = e;
		this.ps = ps;
		selected = new boolean[e.length];
		maxText = new GUITextOverlay(state);
		startTimeText = new GUITextOverlay(state);
		endTimeText = new GUITextOverlay(state);
		setMouseUpdateEnabled(true);
		setCallback(this);
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		long maxAmplitude = 0;
		long curAmplitude = 0;
		long currentTime = System.currentTimeMillis();
		for (int i = 0; i < e.length; i++) {
			if (e[i].getSize() == 0) {
				continue;
			}
			long startTime = currentTime - timeFrameInMS;
			long endTime = currentTime;
			maxAmplitude = Math.max(maxAmplitude, e[i].getMaxAplitude(startTime, endTime));
			if (i == 0) {
				curAmplitude = e[i].getAmplitudeAtIndex(0);
			}
		}
		maxText.setTextSimple(formatMax(maxAmplitude, curAmplitude));
		startTimeText.setTextSimple(dateFormat.format(currentTime - timeFrameInMS));
		endTimeText.setTextSimple(dateFormat.format(currentTime));
		for (int i = 0; i < e.length; i++) {
			draw(e[i], i, maxAmplitude);
		}
		maxText.setPos((int) (getWidth() / 2 - (maxText.getMaxLineWidth() / 2) - 10), 0, 0);
		startTimeText.draw();
		endTimeText.setPos(getWidth() - endTimeText.getMaxLineWidth(), 0, 0);
		endTimeText.draw();
		maxText.draw();
	}

	public abstract String formatMax(long maxAmplitude, long curAmplitude);

	@Override
	public void onInit() {
	}

	public void draw(StatisticsGraphListInterface e, int index, long maxAmplitude) {
		if (e.getSize() == 0) {
			return;
		}
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glLineWidth(1);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		long currentTime = System.currentTimeMillis();
		long startTime = currentTime - timeFrameInMS;
		long endTime = currentTime;
		int startIndex = 0;
		// e.getEndIndexFrom(endTime);
		int endIndex = e.getStartIndexFrom(startTime);
		// System.err.println("STARTTTTT "+startIndex+"; "+endIndex);
		GlUtil.glPushMatrix();
		transform();
		GlUtil.glColor4f(e.getColor());
		GL11.glBegin(GL11.GL_LINE_STRIP);
		int dist = (int) (getWidth() / ((endIndex + 1) - startIndex));
		int distToRight = 5;
		int c = 0;
		double range = e.getTimeAtIndex(startIndex) - e.getTimeAtIndex(endIndex);
		for (int i = startIndex; i < (endIndex + 1); i++) {
			double tRange = e.getTimeAtIndex(startIndex) - e.getTimeAtIndex(i);
			double t = (getWidth() + 40) * (tRange / range);
			GL11.glVertex2d((getWidth() + 20) - (int) t, getHeight() - (int) (e.getAmplitudePercentAtIndex(i, maxAmplitude) * getHeight()));
			c++;
		}
		GL11.glEnd();
		selected[index] = false;
		final int selectedT;
		if (ps == null) {
			selectedT = 0;
		} else {
			selectedT = ps.getSelectedTab();
		}
		int mouseIndex = getMouseIndex();
		for (int i = startIndex; i < (endIndex + 1); i++) {
			if (e.isSelected(i)) {
				GlUtil.glColor4f(e.getColor());
				double tRange = e.getTimeAtIndex(startIndex) - e.getTimeAtIndex(i);
				double t = (getWidth() + 40) * (tRange / range);
				GL11.glBegin(GL11.GL_LINES);
				GL11.glVertex2d((getWidth() + 20) - (int) t, 0);
				GL11.glVertex2d((getWidth() + 20) - (int) t, getHeight());
				GL11.glEnd();
				selected[index] = true;
			} else if (index == selectedT) {
				if (i == mouseIndex) {
					GlUtil.glColor4f(0.7f, 0.7f, 0.7f, 0.7f);
					double tRange = e.getTimeAtIndex(startIndex) - e.getTimeAtIndex(i);
					double t = (getWidth() + 40) * (tRange / range);
					GL11.glBegin(GL11.GL_LINES);
					GL11.glVertex2d((getWidth() + 20) - (int) t, 0);
					GL11.glVertex2d((getWidth() + 20) - (int) t, getHeight());
					GL11.glEnd();
				} else {
					GlUtil.glColor4f(0.3f, 0.3f, 0.3f, 0.3f);
					double tRange = e.getTimeAtIndex(startIndex) - e.getTimeAtIndex(i);
					double t = (getWidth() + 40) * (tRange / range);
					GL11.glBegin(GL11.GL_LINES);
					GL11.glVertex2d((getWidth() + 20) - (int) t, 0);
					GL11.glVertex2d((getWidth() + 20) - (int) t, getHeight());
					GL11.glEnd();
				}
			}
		}
		GlUtil.glColor4f(1, 1, 1, 1);
		checkMouseInside();
		GlUtil.glPopMatrix();
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
	}

	@Override
	public float getHeight() {
		return dependend.getHeight();
	}

	@Override
	public float getWidth() {
		return dependend.getWidth();
	}

	private int getMouseIndex() {
		int selected;
		if (ps == null) {
			selected = 0;
		} else {
			selected = ps.getSelectedTab();
		}
		long currentTime = System.currentTimeMillis();
		long startTime = Math.max(e[selected].getTimeAtIndex(e[selected].getSize() - 1), currentTime - timeFrameInMS);
		long endTime = currentTime;
		int startIndex = 0;
		int endIndex = e[selected].getStartIndexFrom(startTime);
		double range = e[selected].getTimeAtIndex(startIndex) - e[selected].getTimeAtIndex(endIndex);
		double aRange = (getRelMousePos().x / (getWidth() + 40d)) * (endTime - startTime);
		long absTime = startTime + (long) (aRange);
		int clickIndex = e[selected].getClosestIndexFrom(absTime);
		return clickIndex;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse() && ps != null) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
			AudioController.fireAudioEventID(27);
			e[ps.getSelectedTab()].select(getMouseIndex());
			e[ps.getSelectedTab()].notifyGUI();
		}
	}

	/**
	 * @return the selected
	 */
	public boolean[] isSelected() {
		return selected;
	}

	@Override
	public void onSelectionChanged(GUIListElement element) {
		if (element.getContent().getUserPointer() != null && element.getContent().getUserPointer() instanceof Long) {
			timeFrameInMS = (Long) element.getContent().getUserPointer();
		}
	}
}
