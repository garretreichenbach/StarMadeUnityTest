package org.schema.schine.graphicsengine.forms.gui.newgui;

import java.util.List;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUITilePane<E> extends GUIElement {
	public final int tileWidth;
	public final int tileHeight;
	public final int tileWidthS;
	public final int tileHeightS;
	private int height;
	private int width;
	private boolean cachedPosition;
	private float cachePositionX;
	private float cachePositionXP;
	private float cachePositionY;
	private float cachePositionYP;
	private float cachedScrollX = -1;
	private float cachedScrollY = -1;
	private int cachedIndex;
	public GUIScrollablePanel scrollPanel;
	
	private final List<GUITileParam<E>> tiles = new ObjectArrayList<GUITileParam<E>>();
	private GUIElement dependent;
	public GUITilePane(InputState state, GUIElement dependent, int tileWidth, int tileHeight) {
		super(state);
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;

		this.tileWidthS = tileWidth - GUIInnerTextbox.INSET*2;
		this.tileHeightS = tileHeight - GUIInnerTextbox.INSET*2;
		
		this.width = tileWidth;
		this.height = tileHeight;
		
		assert(dependent != null);
		this.dependent = dependent;
	}
	public GUIActiveInterface activeInterface;
	private boolean tileDirty;
	private float lastDepWidth;
	private float lastDepHeight;
	
	
	public int spacingX = 5;
	public int spacingY = 5;
	
	@Override
	public void cleanUp() {
				
	}
	@Override
	public void draw() {
		if (isInvisible()) {
			return;
		}
		if(tileDirty || (dependent.getWidth() != lastDepWidth || dependent.getHeight() != lastDepHeight)){
			resort();
			tileDirty = false;
			
			lastDepWidth = dependent.getWidth();
			lastDepHeight = dependent.getHeight();
			
		}
		if(scrollPanel == null){
			super.drawAttached();
		}else{
			drawScrolled(true, false);
		}
	}
	
	private void drawScrolled(final boolean after, final boolean all){
		if ((cachedScrollX != scrollPanel.getScrollX() || cachedScrollY != scrollPanel.getScrollY())) {
			cachedPosition = false;
		}
		GlUtil.glPushMatrix();

		setInside(false);

		transform();

		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		}
		if (scrollPanel != null && (cachedScrollX != scrollPanel.getScrollX() || cachedScrollY != scrollPanel.getScrollY())) {
			cachedPosition = false;
		}

		float x = 0;
		float y = 0;
		float xP = 0;
		float yP = 0;
		int p = 0;
		int size = getChilds().size();
		boolean firstDrawn = false;

		if (cachedPosition) {
			x = cachePositionX;
			y = cachePositionY;
			xP = cachePositionXP;
			yP = cachePositionYP;
			p = cachedIndex;
		}
		
		int totalIt = 0;
		for (int i = p; i < size; i++) {
			GUITile listElement = (GUITile)getChilds().get(i);
			
			int w = 0;
			int h = 0;
			if (!firstDrawn) {
				for (int j = i; j >= Math.max(i - 1, 0); j--) {
					GUITile e = (GUITile)getChilds().get(j);
					while(j-1 >= Math.max(i - 1, 0) ){
						GUITile next = (GUITile)getChilds().get(j-1);
						if(next.getPos().y == listElement.getPos().y){
							j--;
						}else{
							break;
						}
					}
					w += e.getScale().x * e.getWidth();
					h += e.getScale().y * e.getHeight();
				}
			}
			boolean drawIt = true;

			if (firstDrawn && (
					x > scrollPanel.getScrollX() + scrollPanel.getWidth() ||
							y > scrollPanel.getScrollY() + scrollPanel.getHeight())) {
				drawIt = false;
				//we are at the end
				break;
			} else if (!firstDrawn && (
					x < scrollPanel.getScrollX() - w
							||
							y < scrollPanel.getScrollY() - h)) {
				drawIt = false;
			}

			if (drawIt) {
				GlUtil.translateModelview(xP, yP, 0);
				if (!firstDrawn) {
					cachePositionX = x;
					cachePositionXP = xP;
					cachePositionY = y;
					cachePositionYP = yP;
					cachedScrollX = scrollPanel != null ? scrollPanel.getScrollX() : 0;
					cachedScrollY = scrollPanel != null ? scrollPanel.getScrollY() : 0;
					cachedIndex = i;
					cachedPosition = true;
					firstDrawn = true;
				}

				xP = 0;
				yP = 0;

				listElement.drawWithoutTransform();
				while(i+1 < getChilds().size() ){
					GUITile next = (GUITile)getChilds().get(i+1);
					if(next.getPos().y == listElement.getPos().y){
						GlUtil.glPushMatrix();
						GlUtil.translateModelview(next.getPos().x, 0, 0);
						next.drawWithoutTransform();
						
						GlUtil.glPopMatrix();
						i++;
					}else{
						break;
					}
				}
				p++;

			}
			y += listElement.getScale().y * listElement.getHeight();
			yP += listElement.getScale().y * listElement.getHeight();
			totalIt++;
		}
		
		GlUtil.glPopMatrix();
	}
	private void resort() {
		int x = 0;
		int y = spacingY;
		
		detachAll();
		
		for(int i = 0; i < tiles.size(); i++){
			GUITile t = tiles.get(i);
			
			assert(dependent != null);
			
			
			if(x > 0 && x + tileWidth > dependent.getWidth()){
				x = 0;
				y += spacingY + tileHeight;
			}
			
			x += spacingX;
			
			t.setPos(x, y, 0);
			
			attach(t);
			
			x += tileWidth;
			
		}
		
		width = x + tileWidth;
		height = y + tileHeight;
	}
	@Override
	public void onInit() {
		
	}
	
	public void removeTile(GUIElement tile){
		for(int i = 0; i < tiles.size(); i++){
			GUITile t = tiles.get(i);
			if(t.getContent().getChilds().contains(tile)){
				tileDirty = true;
				t.cleanUp();
				tiles.remove(i);
				return;
			}
		}
		
	}
	
	public GUITile addButtonTile(String tile, String description, HButtonColor type, GUICallback callback, final GUIActivationCallback actCallback){
		
		GUIHorizontalButton b = new GUIHorizontalButton(getState(), type, tile, callback, activeInterface, actCallback);
		b.onInit();
		b.setWidth(tileWidthS);
		
		int distanceToButton = 2;
		int pp = 2;
		
		GUIAnchor c = new GUIAnchor(getState(), tileWidthS-pp*2, tileHeightS-(b.getHeight()+distanceToButton+pp*2));
		
		GUITextOverlay dc = new GUITextOverlay(FontSize.SMALL_15, getState()){

			@Override
			public void draw() {
				
				if(actCallback == null || actCallback.isActive(getState())){
					setColor(1, 1, 1, 1);
				}else{
					setColor(0.9f, 0.4f, 0.4f, 0.4f);
				}
				super.draw();
			}
			
		};
		
		
		dc.setTextSimple(description);
		
		dc.autoWrapOn = c;
		dc.wrapSimple = false;
		
		c.attach(dc);
		
		c.setPos(pp, (pp+b.getHeight()+distanceToButton), 0);
		
		GUITileButtonDesc<E> t = new GUITileButtonDesc<E>(getState(), tileWidth, tileHeight);
		t.onInit();
		
		t.getContent().attach(c);
		t.getContent().attach(b);
		
		
		tiles.add(t);
		tileDirty = true;
		
		t.button = b;
		t.descriptionText = dc;
		
		return t;
	}
	
	public void addTile(GUIElement tile, E userData){
		GUITileParam<E> t = new GUITileParam<E>(getState(), tileWidth, tileHeight, userData);
		t.onInit();
		t.getContent().attach(tile);
		tiles.add(t);
		tileDirty = true;
	}
	
	@Override
	public float getWidth() {
		return width;
	}
	@Override
	public float getHeight() {
		return height;
	}
	public void clear() {
		for(int i = 0; i < tiles.size(); i++){
			tiles.get(i).cleanUp();
		}
		tiles.clear();
		tileDirty = true;
	}
	public List<GUITileParam<E>> getTiles() {
		return tiles;
	}
}
