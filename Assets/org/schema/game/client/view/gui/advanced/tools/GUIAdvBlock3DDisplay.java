package org.schema.game.client.view.gui.advanced.tools;

import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.gui.GUI3DBlockElement;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class GUIAdvBlock3DDisplay extends GUIAdvTool<Block3DResult>{

	private final GUI3DBlockElement blockPreview;
	private final GUIColoredRectangle blockBackground;

	
	public GUIAdvBlock3DDisplay(InputState state, GUIElement dependent, Block3DResult r) {
		super(state, dependent, r);
		blockPreview = new GUI3DBlockElement(getState()){
			@Override
			public void draw() {
				final short type = getRes().getType();
				setBlockType(type);
				final int blockOrientation = getRes().getOrientation();
				
				if (type > Element.TYPE_NONE && ElementKeyMap.isInit()) {
					ElementInformation info = ElementKeyMap.getInfo(type);
					if (info.getBlockStyle() != BlockStyle.NORMAL) {
						blockPreview.setBlockType(type);
						blockPreview.setSidedOrientation(0);
						blockPreview.setShapeOrientation(blockOrientation);
					} else if (ElementKeyMap.getInfo(type).getIndividualSides() > 3) {
						blockPreview.setBlockType(type);
						blockPreview.setShapeOrientation(0);
						blockPreview.setSidedOrientation(blockOrientation);
					} else if (ElementKeyMap.getInfo(type).orientatable) {
						blockPreview.setBlockType(type);
						blockPreview.setShapeOrientation(0);
						blockPreview.setSidedOrientation(blockOrientation);
					} else {
						blockPreview.setBlockType(type);
						blockPreview.setShapeOrientation(0);
						blockPreview.setSidedOrientation(0);
					}
				} else {
					blockPreview.setBlockType((short) 0);
					blockPreview.setShapeOrientation(0);
					blockPreview.setSidedOrientation(0);
				}
				
				
				super.draw();
			}
			
		};
		blockBackground = new GUIColoredRectangle(state, 64, 64, getRes().getBackgroundColor()){

			@Override
			public void draw() {
				setColor(getRes().getBackgroundColor());
				super.draw();
			}
			
		};
		blockPreview.setPos(32, 32, 0);
		
		attach(blockBackground);
		attach(blockPreview);
	}
	@Override
	public int getElementHeight() {
		return 64;
	}
}
