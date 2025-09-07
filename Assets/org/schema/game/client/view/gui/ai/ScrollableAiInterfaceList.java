package org.schema.game.client.view.gui.ai;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;

import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ai.AiInterfaceContainer;
import org.schema.game.common.controller.ai.UnloadedAiEntityException;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIEnterableList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public abstract class ScrollableAiInterfaceList extends GUIAnchor implements GUIChangeListener {

	private final static int topBarHeight = 30;
	private GUITextButton nameSort;
	private GUIScrollablePanel scrollPanel;
	private GUIAnchor topBar;
	private GUIElementList list;
	private boolean showAdmin;
	private Order lastOrder;

	public ScrollableAiInterfaceList(InputState state, int width, int height, boolean showAdmin) {
		super(state, width, height);

		this.showAdmin = showAdmin;

		list = new GUIElementList(getState());
		topBar = new GUIAnchor(getState(), width, topBarHeight);

		((GameClientState) state).getPlayer().getPlayerAiManager().addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		super.draw();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();

		nameSort = new GUITextButton(getState(), UIScale.getUIScale().scale(217), UIScale.getUIScale().scale(20),
				new Vector4f(0.4f, 0.4f, 0.4f, 0.5f),
				new Vector4f(1, 1, 1, 1),
				FontSize.SMALL_15,
				"Name", new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					order(Order.NAME);
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		rebuildList(list);

		scrollPanel = new GUIScrollablePanel(getWidth(), getHeight() - topBar.getHeight(), getState());

		scrollPanel.setContent(list);

		scrollPanel.getPos().y = topBar.getHeight();

		topBar.attach(nameSort);

		this.attach(topBar);

		this.attach(scrollPanel);

	}

	private void rebuildList(GUIElementList list) {
		Collection<AiInterfaceContainer> entries = getEntries();

		HashSet<String> activated = new HashSet<String>();
		for (GUIListElement l : list) {
			if (l instanceof AiCommandableGUIListElement) {
				AiCommandableGUIListElement e = ((AiCommandableGUIListElement) l);
				if (e.getContent() instanceof GUIEnterableList && ((GUIEnterableList) e.getContent()).isExpended()) {
					activated.add(e.getAiInterface().getUID());
				}
			}
		}

		list.clear();
		int i = 0;
		for (final AiInterfaceContainer f : entries) {
			AiCommandableGUIListElement aiCommandableGUIListElement = new AiCommandableGUIListElement(f, this, showAdmin, getState(), i, activated.contains(f.getUID()));
			list.add(aiCommandableGUIListElement);
			list.setScrollPane(scrollPanel);

			i++;
		}

		if (lastOrder == null) {
			order(Order.NAME);
		} else {
			orderFixed(lastOrder);
		}
		//		detach(topBar);
		//		if(list.size() > 0){
		//			attach(topBar);
		//		}
	}

	public void order(Order order) {
		order.comp = Collections.reverseOrder(order.comp);
		orderFixed(order);
	}

	public void orderFixed(Order order) {
		Collections.sort(list, order.comp);
		lastOrder = order;
		nameSort.getColorText().set(0.7f, 0.7f, 0.7f, 0.7f);
		switch(order) {
			case NAME -> nameSort.getColorText().set(1, 1, 1, 1);
		}

		for (int i = 0; i < list.size(); i++) {
			((AiInterfaceEnterableList) list.get(i).getContent()).updateIndex(i);
		}
	}

	public abstract Collection<AiInterfaceContainer> getEntries();


	@Override
	public final void onChange(boolean updateListDim) {
		if(updateListDim) {
			list.updateDim();
		}else {
			rebuildList(list);
		}
	}

	enum Order {

		NAME((o1, o2) -> {
			try {
				return ((AiCommandableGUIListElement) o1).getAiInterface().getRealName().toLowerCase(Locale.ENGLISH)
						.compareTo(((AiCommandableGUIListElement) o2).getAiInterface().getRealName().toLowerCase(Locale.ENGLISH));
			} catch (UnloadedAiEntityException e) {
				return ((AiCommandableGUIListElement) o1).getAiInterface().getUID().toLowerCase(Locale.ENGLISH)
						.compareTo(((AiCommandableGUIListElement) o2).getAiInterface().getUID().toLowerCase(Locale.ENGLISH));
			}
		}),;
		Comparator<GUIListElement> comp;

		private Order(Comparator<GUIListElement> comp) {
			this.comp = comp;
		}
	}

}
