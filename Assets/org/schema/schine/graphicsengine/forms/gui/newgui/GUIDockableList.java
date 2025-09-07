package org.schema.schine.graphicsengine.forms.gui.newgui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.List;

public class GUIDockableList extends GUIElement implements ExpandableCallback{

	
	
	private GUIElementList mainList;
	private GUIElement insideChecker;
	private final ObjectOpenHashSet<String> elementIds = new ObjectOpenHashSet<String>();
	private boolean flagDirty = true;
	private boolean init;
	public int mainWidth = 400;
	private int scrollerHeight;
	private int scrollerWidth;
	private List<GUIElementList> subLists = new ObjectArrayList<GUIElementList>();
	private GUISelectable sel;
	
	public GUIDockableList(InputState state, GUISelectable sel) {
		super(state);
		this.sel = sel;
	}

	@Override
	public void cleanUp() {
		if(!init){
			return;
		}
		mainList.cleanUp();
	}

	@Override
	public boolean isActive() {
		return super.isActive() && sel.isActive();
	}

	@Override
	public boolean isInside() {
		return insideChecker.isInside();
	}

	@Override
	public void draw() {
		if(!init){
			onInit();
		}
		
		if(flagDirty){
			recalculate();
			flagDirty = false;
			mainList.updateDim();
		}
		drawAttached();
	}
	private void recalculate() {
		
		mainList.updateDim();
		assert(!mainList.isEmpty()):mainList.size();
	}
	public void setHeightScroller(int height){
		if(!init) onInit();
		this.scrollerHeight = (height);
	}
	public void setWidthScroller(int width){
		if(!init) onInit();
		this.mainWidth = width;
		assert mainList != null;
		this.scrollerWidth = (mainWidth+mainList.rightInset);
	}
	@Override
	public void onInit() {
		if(init){
			return;
		}
		mainList = new GUIElementList(getState()){

			@Override
			public void updateDim() {
				for(GUIListElement e : this){
					if(e.getContent() instanceof GUIScrollablePanel){
						if(((GUIScrollablePanel)e.getContent()).getContent() instanceof GUIElementList){
							((GUIElementList)((GUIScrollablePanel)e.getContent()).getContent()).updateDim();
						}
					}
				}
				super.updateDim();
			}
			@Override
			public String toString() {
				return "[MAIN LIST]";
			}
		};
		mainList.onInit();
		mainList.rightInset = 16;
		
		mainList.setMouseUpdateEnabled(true);
		init = true;
		mainList.getPos().x = -16; 
		attach(mainList);
		
		insideChecker = new GUIElement(getState()) {
			
			private int height;

			@Override
			public void onInit() {
			}
			
			@Override
			public void draw() {
				
				height = 0;
				for(GUIElementList sublist : subLists){
					height += sublist.getHeight();
				}
				setPos(GUIDockableList.this.getPos());
				getPos().add(mainList.getPos());
				
				checkMouseInsideWithTransform();
			}
			
			@Override
			public void cleanUp() {
			}
			
			@Override
			public float getWidth() {
				return mainList.getWidth();
			}
			
			@Override
			public float getHeight() {
				return height;
			}
			@Override
			public String toString() {
				return "[INSIDE CHECKER]";
			}
		};
		attach(insideChecker);
		mainList.updateDim();
	}

	@Override
	public float getHeight() {
		return scrollerHeight;
	}

	@Override
	public float getWidth() {
		return mainWidth;
	}
	@Override
	public String toString() {
		return "[DOCKABLE LSIT]";
	}
	public DockerElementExpandable addElementFixed(String id, String title, boolean closable, GUICallback closeCallback, int subList){
		return addElement(id, title, closable, closeCallback, subList, 1f,1f,1f,1f, false, false);
	}
	public DockerElementExpandable addElementFixed(String id, String title, boolean closable, GUICallback closeCallback, int subList, Vector4f tint){
		return addElement(id, title, closable, closeCallback, subList, tint.x, tint.y, tint.z, tint.w, false, false);
	}
	public DockerElementExpandable addElementExpanded(String id, String title, boolean closable, GUICallback closeCallback, int subList, boolean expanded){
		return addElement(id, title, closable, closeCallback, subList, 1f,1f,1f,1f, true, expanded);
	}
	public DockerElementExpandable addElementExpanded(String id, String title, boolean closable, GUICallback closeCallback, int subList, Vector4f tint, boolean expanded){
		return addElement(id, title, closable, closeCallback, subList, tint.x, tint.y, tint.z, tint.w, true, expanded);
	}
	public DockerElementExpandable addElement(String id, String title, boolean closable, GUICallback closeCallback, final int subListIndex, float r, float g, float b, float a, boolean expandable, boolean expanded){
		final DockerElementExpandable d = new DockerElementExpandable(getState(), this, closable, closeCallback, mainWidth, expandable, id, expanded);
		boolean add = elementIds.add(id);
		assert(add):id+" aleady in "+elementIds;
		
		
		
		GUIListElement l = new GUIListElement(d.window, d.window, getState());
		
		
		while(mainList.size()-1 < subListIndex){
			final GUIElementList sublist = new GUIElementList(getState());
			
			GUIScrollablePanel scrollPanel;
			scrollPanel = new GUIScrollablePanel(mainWidth+mainList.rightInset, 300, getState()){
				@Override
				public void draw() {
					setWidth(scrollerWidth);
					
					int rest = scrollerHeight;
					for(int i = 0; i < mainList.size(); i++){
						final int height = (int) (i < mainList.size()-1 ? subLists.get(i).getHeight() : rest);
						if(i == subListIndex){
							setHeight(height);
						}
						rest -= height;
					}
					super.draw();
					d.window.selectedBackground = isSelected();
				}
				
			};
			sublist.rightInset = 16;
			scrollPanel.setContent(sublist);
			scrollPanel.onInit();
			sublist.setScrollPane(scrollPanel);
			this.subLists.add(sublist);
			
			GUIListElement e = new GUIListElement(scrollPanel, scrollPanel, getState());
			mainList.addWithoutUpdate(e);
		}
		GUIElementList subList = subLists.get(subListIndex);
		subList.addWithoutUpdate(l);
		flagDirty = true;
		d.onInit();
		d.setTitle(title, this);
		d.window.backgroundTint.set(r, g, b, a);
		return d;
	}
	public void removeElement(String windowId) {
		for(GUIElementList e : subLists){
			for(GUIListElement a : e){
				GUIPlainWindow sc = ((GUIPlainWindow)a.getContent());
				if(sc.getWindowId().equals(windowId)){
					boolean remove = elementIds.remove(sc.getWindowId());
					assert(remove);
					e.remove(a);
					break;
				}
			}
		}
		mainList.updateDim();
	}
	public static class DockerElementExpandable implements GUIDockableDirtyInterface{
		public int mainHeightAdditional;
		private final GUIPlainWindow window;
		private boolean init;
		private boolean markDirty;
		private InnerWindowBuilderInterface bInterface;
		private final int mainWidth;
		private final GUIDockableList l;
		public DockerElementExpandable(InputState state, final GUIDockableList l, boolean closable, GUICallback closeCallback, int mainWidth, boolean expandable, String windowId, boolean expanded) {
			this.mainWidth = mainWidth;
			this.l = l; 
			if(expandable){
				window = new GUIExpandableWindow(state, mainWidth, 100, 0, 0, windowId, expanded){
					@Override
					public void draw() {
						onDraw();
						super.draw();
					}
					@Override
					public boolean isActive(){
						return DockerElementExpandable.this.l.isActive();
					}
				};
			}else{
				window = new GUIPlainWindow(state, mainWidth, 100, 0, 0, windowId){
					@Override
					public void draw() {
						onDraw();
						super.draw();
					}
				};
			}
			window.setClosable(closable);
			window.setCloseCallback(closeCallback);
			window.setResizable(false);
			window.setMovable(false);
			
			//reset pos in case something was loaded
			window.setPos(0,0,0);
		}
		public void setTitle(String title, GUIDockableList guiDockableList) {
			if(window instanceof GUIExpandableWindow){
				((GUIExpandableWindow)window).setTitle(title, guiDockableList);
			}
		}
		public void onInit(){
			if(init){
				return;
			}
			
			
			window.onInit();
			window.innerHeightSubstraction = 0; //distance of lower end
			window.innerWidthSubstraction = 28;
			
			window.yInnerOffset = 12; //space between text boxes
			if(window instanceof GUIExpandableWindow){
				mainHeightAdditional = 8; 
				window.insetCornerDistTop = 22; //y position of inner (distance between text and entending content)
				window.insetCornerDistBottom = 4;
			}else{
				mainHeightAdditional = 8;
				window.insetCornerDistTop = 0; //y position of inner
				window.insetCornerDistBottom = 4;
			}
			window.getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(30));
			
		}

		public void onDraw() {
			if(markDirty){
				markDirty = false;
			}
			window.setWidth(mainWidth);
			bInterface.updateOnDraw();
			int h = 0;
			for(int d = 0; d < window.getMainContentPane().getDividerCount(); d++){
				for(int t = 0; t < window.getMainContentPane().getTextboxes(d).size(); t++){
					GUIInnerTextbox tb = window.getMainContentPane().getTextboxes(d).get(t);
					bInterface.adaptTextBox(t, tb);
					h += tb.tbHeight + window.getInnerOffsetY();
				}
			}
			setWindowHeight((int) (h + window.getInnerCornerTopDistY()  + window.innerHeightSubstraction + mainHeightAdditional)); //+ getInnerOffsetY()			
		}
		
		public void build(InnerWindowBuilderInterface bInterface){
			this.bInterface = bInterface;
			bInterface.build(window.getMainContentPane(), this);
		}

		@Override
		public void flagDirty() {
			markDirty = true;
			l.flagDirty = true;
		}

		@Override
		public void setWindowHeight(int height) {
			boolean changed = (int)window.getHeight() != height;
			window.setHeight(height);
			if(changed){
				flagDirty();
			}
		}
		@Override
		public String toString() {
			return "[DockerElementExpandable "+window+"; "+window.getWindowId()+"]";
		}
		
		
	}

	@Override
	public void onExpandedChanged() {
		flagDirty = true;
	}

	public boolean isSelected(){
		return sel != null && sel.isSelected();
	}

	
}
