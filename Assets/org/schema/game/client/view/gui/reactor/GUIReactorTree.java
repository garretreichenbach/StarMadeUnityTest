package org.schema.game.client.view.gui.reactor;


import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorSet;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraph;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2FloatOpenHashMap;

public class GUIReactorTree extends GUIElement implements GUIChangeListener, ReactorTreeListener{

	private final ManagedSegmentController<?> m;
	private ReactorTree tree;
	private int maxX;
	private int maxY;
	public GUIReactorManagerInterface manI;
	private boolean builtOnce;
	private GUIActiveInterface actIface;
	public static long selected = Long.MIN_VALUE;
	
	private static int lastSegCon;
	private static Long2ObjectOpenHashMap<Short2FloatOpenHashMap> lastScrollX = new Long2ObjectOpenHashMap<Short2FloatOpenHashMap>();
	private static Long2ObjectOpenHashMap<Short2FloatOpenHashMap> lastScrollY = new Long2ObjectOpenHashMap<Short2FloatOpenHashMap>();
	
	private GUIGraph reactorGraph;
	private GUIScrollablePanel p;
	private GUIElement dependent;

	public GUIReactorTree(GameClientState state, GUIActiveInterface actIface, ManagedSegmentController<?> m, GUIReactorManagerInterface manI, GUIElement dependent, ReactorTree tree) {
		super(state);
		this.actIface = actIface;
		this.manI = manI;
		this.m = m;
		this.tree = tree;
		this.dependent = dependent;
		m.getManagerContainer().getPowerInterface().addObserver(this);
		
	}

	@Override
	public GameClientState getState(){
		return (GameClientState)super.getState();
	}
	@Override
	public void cleanUp() {
		for(AbstractSceneNode e : getChilds()){
			e.cleanUp();
		}
		m.getManagerContainer().getPowerInterface().deleteObserver(this);
	}

	@Override
	public boolean isActive() {
		return super.isActive() && (actIface == null || actIface.isActive());
	}
	@Override
	public void draw() {
//		drawLines();
		super.drawAttached();
		
		lastScrollX.get(tree.getId()).put(manI.getSelectedTab().id, p.getScrollX());
		lastScrollY.get(tree.getId()).put(manI.getSelectedTab().id, p.getScrollY());
	}
	public PowerInterface getPowerInterface(){
		return m.getManagerContainer().getPowerInterface();
	}
	@Override
	public void onInit() {
		if(!builtOnce){
			build();
		}
	}
	
	private void build(){
		
		if(reactorGraph != null){


			p.cleanUp();
			reactorGraph.cleanUp();
		}
		reactorGraph = tree.getTreeGraphCurrent(manI.getSelectedTab(), this);
		
		
		if(lastSegCon != tree.pw.getSegmentController().getId()){
			
			lastScrollX.clear();
			lastScrollY.clear();
			lastSegCon = tree.pw.getSegmentController().getId();
		}
		if(!lastScrollX.containsKey(tree.getId())){
			lastScrollX.put(tree.getId(), new Short2FloatOpenHashMap());
			lastScrollY.put(tree.getId(), new Short2FloatOpenHashMap());
		}
		reactorGraph.onInit();
		p = new GUIScrollablePanel(10, 10, this.dependent, getState());
		p.setContent(reactorGraph);
		p.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);
		p.onInit();
		
		detachAll();
		if(reactorGraph.isEmptyGraph()){
			GUITextOverlay l = new GUITextOverlay(FontSize.BIG_20, getState());
			l.setTextSimple(
					Lng.str("Reactor chambers improve the functionality of your ship (jump drive, scanners, cloak, etc). "
					+ "Place down a chamber block and use conduit blocs it to your main reactor.\n"
					+ "You can chain multiple reactor chambers together to get even more effects for your structure."));
			l.onInit();
			GUIAnchor bb = new GUIAnchor(getState()){
				@Override
				public void draw(){
					this.setWidth(dependent.getWidth());
					this.setHeight(dependent.getHeight());
					super.draw();
				}
			};
			l.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset);
			bb.attach(l);
			l.autoWrapOn = bb;
			p.setContent(bb);
		}
		p.scrollHorizontal(lastScrollX.get(tree.getId()).get(manI.getSelectedTab().id));
		p.scrollVertical(lastScrollY.get(tree.getId()).get(manI.getSelectedTab().id));
	
		attach(p);
		
		builtOnce = true;
	}

	@Override
	public float getWidth() {
		return (maxX+1)*GUIReactorTreeNode.NODE_WIDTH;
	}

	@Override
	public float getHeight() {
		return (maxY+1)*GUIReactorTreeNode.NODE_HEIGHT;
	}

	@Override
	public void onChange(boolean updateListDim) {
		
	}

	public ReactorTree getTree(){
		return tree;
	}
	public void setSelected(GUIReactorTreeNode element) {
		if(element == null || element.element == null){
			selected = Long.MIN_VALUE;
		}else{
			selected = element.element.getId();
		}
	}

	public float getReactorCapacityOf(ElementInformation info) {
		
		return tree.getReactorCapacityOf(info);
	}

	@Override
	public void onTreeChanged(ReactorSet s) {
		boolean found = false;
		
		for(ReactorTree t : s.getTrees()){
			if(t.getId() == this.tree.getId()){
				
				for(AbstractSceneNode e : getChilds()){
					e.cleanUp();
				}
				detachAll();
				this.tree = t;
				build();
				found = true;
			}
		}
		if(!found){
			manI.onTreeNotFound(this);
		}		
	}
	

}
