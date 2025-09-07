package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIContentPane extends GUIElement {

	public final GUIAnchor tabAnchor;
	private final ObjectArrayList<DividerList> textboxes = new ObjectArrayList<DividerList>();

	private GUIWindowInterface p;

	private Object tabName;

	private GUITextOverlay tabNameText;

	private int textWidth;
	private Vector4f textColorSelected = new Vector4f(1, 1, 1, 1);
	private Vector4f textColorUnselected = new Vector4f(0.67f, 0.67f, 0.67f, 1);
	private int dividerDetail = -1;
	private int divEqualH = -1;
	private GUIActivationCallback guiActivationCallback;

	public GUIContentPane(InputState state, GUIWindowInterface p, Object tabname) {
		super(state);
		this.p = p;
		textboxes.add(new DividerList(0));
		this.tabName = tabname;
		tabAnchor = new GUIAnchor(state){

			@Override
			public boolean isActive() {
				return super.isActive() && (guiActivationCallback == null || guiActivationCallback.isActive(getState()));
			}
			
		};
		tabAnchor.setMouseUpdateEnabled(true);
	}

	public void addDivider(int distance) {
		textboxes.get(textboxes.size() - 1).divWidth = distance;
		DividerList dividerList = new DividerList(textboxes.size() - 1);
		textboxes.add(dividerList);
		addNewTextBox((textboxes.size() - 1), 0);
	}

	public void setContent(int div, int i, GUIAnchor c) {
		textboxes.get(div).get(i).setContent(c);
	}

	public void setContent(int i, GUIAnchor c) {
		setContent(0, i, c);
	}

	@Override
	public void cleanUp() {
		for (int i = 0; i < textboxes.size(); i++) {
			textboxes.get(i).cleanUp();
		}
		if (tabNameText != null) {
			tabNameText.cleanUp();
		}
		if (tabAnchor != null) {
			tabAnchor.cleanUp();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#draw()
	 */
	@Override
	public void draw() {

		int innerOffsetX = p.getInnerOffsetX(); //default 8
		int innerOffsetY = p.getInnerOffsetY(); //default 8
		int lastPos = 0;

		int insetY = p.getInnerCornerTopDistY();
		int insetX = p.getInnerCornerDistX();

		int horizontalRest = p.getInnerWidth() - (insetX - innerOffsetX - innerOffsetX / 2);

		//determine widths of each divider section
		for (int divL = 0; divL < textboxes.size(); divL++) {
			DividerList textBoxesDiv = textboxes.get(divL);
			if (dividerDetail >= 0) {
				if (divL == dividerDetail) {

				} else {
					textBoxesDiv.hWidth = textBoxesDiv.divWidth - innerOffsetX;
					horizontalRest -= (textBoxesDiv.divWidth);
				}
			} else {

				if (divL == textboxes.size() - 1) {
					textBoxesDiv.hWidth = horizontalRest;
				} else {
					textBoxesDiv.hWidth = textBoxesDiv.divWidth - innerOffsetX;
					horizontalRest -= (textBoxesDiv.divWidth);
				}
			}
		}
		//fill in missing
		if (dividerDetail >= 0) {
			DividerList textBoxesDiv = textboxes.get(dividerDetail);
			textBoxesDiv.hWidth = horizontalRest;
		}
		int horizontalTotal = insetX + innerOffsetX;
		//determine horizontal positions
		for (int divL = 0; divL < textboxes.size(); divL++) {
			DividerList textBoxesDiv = textboxes.get(divL);

			textBoxesDiv.hPos = horizontalTotal;
			horizontalTotal += (textBoxesDiv.hWidth + innerOffsetX);

			if (textBoxesDiv.movable) {
				textBoxesDiv.movableAnchor.setPos(horizontalTotal - insetX - innerOffsetX, p.getTopDist() + insetY + innerOffsetY, 0);
				textBoxesDiv.movableAnchor.setWidth(innerOffsetX);
				textBoxesDiv.movableAnchor.setHeight(p.getInnerHeigth() - (insetY - innerOffsetY * 2));
				textBoxesDiv.movableAnchor.draw();

				if (textBoxesDiv.isInside()) {
					GlUtil.glDisable(GL11.GL_TEXTURE_2D);
					GlUtil.glColor4f(1, 1, 1, 1);
					int wIn = 1;
					GL11.glBegin(GL11.GL_QUADS);
					int xStart = (int) (textBoxesDiv.movableAnchor.getPos().x + wIn + 1);
					int xEnd = (int) (textBoxesDiv.movableAnchor.getPos().x + (textBoxesDiv.movableAnchor.getWidth() - wIn * 2));
					int yStart = (int) (textBoxesDiv.movableAnchor.getPos().y + (textBoxesDiv.movableAnchor.getHeight() / 3));
					int yEnd = (int) (textBoxesDiv.movableAnchor.getPos().y + (textBoxesDiv.movableAnchor.getHeight() / 3) * 2);

					GL11.glVertex2f(xStart, yStart);
					GL11.glVertex2f(xStart, yEnd);
					GL11.glVertex2f(xEnd, yEnd);
					GL11.glVertex2f(xEnd, yStart);
					GL11.glEnd();
				}
				textBoxesDiv.checkGrab();
			}
		}

		for (int divL = 0; divL < textboxes.size(); divL++) {
			
			int rest = p.getInnerHeigth() - (insetY + innerOffsetY * 2 - p.getInnerCornerBottomDistY());
			
//			System.out.println("DRAW BOX Start: rest " + rest);
			DividerList textBoxesDiv = textboxes.get(divL);
			if(divEqualH >= 0){
				
				int restWo = rest;
				for(int i = 0; i < Math.min(textBoxesDiv.size(), divEqualH); i++){
					restWo -= textBoxesDiv.get(i).tbHeight;
				}
				
				for (int i = divEqualH; i < textBoxesDiv.size(); i++) {
					GUIInnerTextbox box = textBoxesDiv.get(i);
					box.tbHeight = (restWo / (textBoxesDiv.size() - divEqualH));
				}
			}
			if (textBoxesDiv.isListDetailMode != null && !textBoxesDiv.isEmpty()) {

				
				lastPos = p.getTopDist();

				for (int i = 0; i < textBoxesDiv.size(); i++) {
					GUIInnerTextbox box = textBoxesDiv.get(i);

					box.setWidth(textBoxesDiv.hWidth);

					if (box == textBoxesDiv.isListDetailMode) {

					} else {
						box.setHeight(textBoxesDiv.get(i).tbHeight);
						rest -= box.getHeight() + innerOffsetY;
						lastPos += box.getHeight() + innerOffsetY;
					}
				}
				textBoxesDiv.isListDetailMode.setHeight(rest);

				lastPos = p.getTopDist();
				for (int i = 0; i < textBoxesDiv.size(); i++) {
					GUIInnerTextbox box = textBoxesDiv.get(i);
					box.setPos(textBoxesDiv.hPos, insetY + innerOffsetY + lastPos, 0);
					lastPos += box.getHeight() + innerOffsetY;

					box.draw();
				}
			} else {


				lastPos = p.getTopDist();
				for (int i = 0; i < textBoxesDiv.size(); i++) {
					
					GUIInnerTextbox box = textBoxesDiv.get(i);
					box.setPos(textBoxesDiv.hPos, insetY + innerOffsetY + lastPos, 0);

					box.setWidth(textBoxesDiv.hWidth);

					if (i + 1 >= textBoxesDiv.size()) {
						box.setHeight(rest);
					} else {
						box.setHeight(textBoxesDiv.get(i).tbHeight);
					}
//					System.out.println("DRAW BOX i: " + i + " height " + box.getHeight() + " tbHeight " + box.tbHeight  + " insetY " + insetY + " insetBoxY " + insetBoxY 
//						+ " rest " + rest + " lastPos " + lastPos);
					rest -= (box.getHeight() + innerOffsetY);
					lastPos += box.getHeight() + innerOffsetY;

					box.draw();
				}
			}
		}
	}

	@Override
	public void onInit() {
		addNewTextBox(0, 0);
//		addNewTextBox(UIScale.getUIScale().scale(80));

		this.tabNameText = new GUITextOverlay(FontSize.MEDIUM_19, getState());
		if(tabName == null){
			throw new NullPointerException();
		}
		assert(tabName != null);
		this.tabNameText.setTextSimple(tabName);
		this.tabNameText.onInit();

		this.textWidth = tabNameText.getFont().getWidth(tabName.toString());

	}

	@Override
	public float getHeight() {
		return p.getHeight();
	}

	@Override
	public float getWidth() {
		return p.getWidth();
	}

	public GUIInnerTextbox addNewTextBox(int tbHeight) {
		return addNewTextBox(0, tbHeight);
	}
	public void setTint(int tb, float r, float g, float b, float a){
		getTextboxes(0).get(tb).setTint(r, g, b, a);
	}
	public void setTint(int div, int tb, float r, float g, float b, float a){
		getTextboxes(div).get(tb).setTint(r, g, b, a);
	}
	public GUIInnerTextbox addNewTextBox(int div, int tbHeight) {
		GUIInnerTextbox tb = new GUIInnerTextbox(getState()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUIInnerTextbox#isActive()
			 */
			@Override
			public boolean isActive() {
				return p.isActive();
			}

		};
		if (!getTextboxes(div).isEmpty()) {
			assert (getTextboxes(div).get(getTextboxes(div).size() - 1).tbHeight > 0):"last tb size 0";
//			getTextboxes().get(getTextboxes().size()-1).distance = distance;
			tb.tbHeight = tbHeight;
		}

		tb.onInit();

		int insetBox = 8;
		int inset = p.getInnerCornerDistX();
		tb.setWidth(p.getInnerWidth() - (inset - insetBox * 2));
		getTextboxes(div).add(tb);

		return tb;
	}

	public void setTextBoxHeight(int div, int index, int height) {
		getTextboxes(div).get(index).tbHeight = height;
	}

	public void setTextBoxHeightLast(int div, int height) {
		getTextboxes(div).get(getTextboxes(div).size() - 1).tbHeight = height;
	}

	public void setTextBoxHeight(int index, int height) {
		setTextBoxHeight(0, index, height);
	}

	public void setTextBoxHeightLast(int height) {
		setTextBoxHeightLast(0, height);
	}

	/**
	 * @return the tabName
	 */
	public Object getTabName() {
		return tabName;
	}

	/**
	 * @param tabName the tabName to set
	 */
	public void setTabName(Object tabName) {
		this.tabName = tabName;
	}

	/**
	 * @return the textboxes
	 */
	public ObjectArrayList<GUIInnerTextbox> getTextboxes(int divider) {
		return textboxes.get(divider);
	}
	public int getDividerCount(){
		return textboxes.size();
	}
	/**
	 * @return the textboxes
	 */
	public ObjectArrayList<GUIInnerTextbox> getTextboxes() {
		return getTextboxes(0);
	}

	/**
	 * @return the tabNameText
	 */
	public GUITextOverlay getTabNameText() {
		return tabNameText;
	}

	public Vector4f getTextColorSelected() {
		return textColorSelected;
	}

	public Vector4f getTextColorUnselected() {
		return textColorUnselected;
	}

	/**
	 * @return the textWidth
	 */
	public int getTextWidth() {
		return textWidth;
	}

	public boolean existsContent(int div, int i) {
		return textboxes.get(div) != null && textboxes.get(div).get(i) != null;
	}
	public GUIAnchor getContent(int div, int i) {
		return textboxes.get(div).get(i).getContent();
	}

	public GUIAnchor getContent(int i) {
		return getContent(0, i);
	}

	/**
	 * @param isListDetailMode the isListDetailMode to set
	 */
	public void setListDetailMode(int divDetail, int listDetail, GUIInnerTextbox isListDetailMode) {
		textboxes.get(divDetail).isListDetailMode = isListDetailMode;
	}

	/**
	 * @param isListDetailMode the isListDetailMode to set
	 */
	public void setListDetailMode(int listDetail, GUIInnerTextbox isListDetailMode) {
		setListDetailMode(0, listDetail, isListDetailMode);
	}

	public void setListDetailMode(GUIInnerTextbox isListDetailMode) {
		setListDetailMode(0, 0, isListDetailMode);
	}

	public void setDividerDetail(int i) {
		this.dividerDetail = i;
	}

	public void setDividerWidth(int i, int width) {
		textboxes.get(i).divWidth = width;
	}

	public void setDividerMovable(int i, boolean movable) {
		textboxes.get(i).movable = movable;
	}

	private class DividerList extends ObjectArrayList<GUIInnerTextbox> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int hPos;
		public int hWidth;
		public boolean movable = false;
		GUIAnchor movableAnchor;
		private int divWidth;
		private GUIInnerTextbox isListDetailMode;
		private boolean grabbed;
		private int grabbedXStart;
		private int index;
		private boolean wasDivInsideUnclicked;

		public DividerList(int index) {
			movableAnchor = new GUIAnchor(getState(), 0, 0);
			movableAnchor.setMouseUpdateEnabled(true);
			this.index = index;
		}

		public void cleanUp() {
			for (int i = 0; i < size(); i++) {
				get(i).cleanUp();
			}
		}

		public boolean isInside() {
			return p.isActive() && movableAnchor.isInside();
		}

		public void checkGrab() {

			if (!GlUtil.isColorMask()) {
				return;
			}

			if (grabbed && Mouse.isPrimaryMouseDownUtility()) {
				//continue grabbing
			} else if (isInside() && wasDivInsideUnclicked && !grabbed && Mouse.isPrimaryMouseDownUtility()) {
				grabbed = true;
				this.grabbedXStart = Mouse.getX();
			} else {
				if (!Mouse.isPrimaryMouseDownUtility()) {
					grabbed = false;
				}
			}

			if (grabbed) {

				int diff = Mouse.getX() - grabbedXStart;
				grabbedXStart = Mouse.getX();

				if (dividerDetail == index && index + 1 < textboxes.size()) {
//					System.err.println("DET: "+dividerDetail+" GRABBED: "+diff+"; +1 "+index+"; "+divWidth+" / "+textboxes.get(index+1).divWidth);
					textboxes.get(index + 1).setDiv(diff, true);
				} else {
//					System.err.println("DET: "+dividerDetail+" GRABBED: "+diff+"; "+index+"; "+divWidth+"; TB: "+textboxes.size());
					this.setDiv(diff, false);
				}
			}

			wasDivInsideUnclicked = isInside() && !Mouse.isPrimaryMouseDownUtility();
		}

		public void setDiv(int diff, boolean after) {
			if (after) {
				this.divWidth = Math.min(p.getInnerWidth() + 2, Math.max(14, divWidth - diff));
			} else {
				this.divWidth = Math.min(p.getInnerWidth() - 14, Math.max(2, divWidth - diff));
			}
		}
	}
	/**
	 * use -1 for off
	 * @param startingAtTextBox
	 */
	public void setEqualazingHorizontalDividers(int startingAtTextBox) {
		divEqualH = startingAtTextBox;
	}

	public void setTabActivationCallback(GUIActivationCallback guiActivationCallback) {
		this.guiActivationCallback = guiActivationCallback;
	}

	
}
