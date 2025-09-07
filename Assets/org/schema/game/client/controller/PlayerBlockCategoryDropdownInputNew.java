package org.schema.game.client.controller;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUISearchBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

import java.util.Locale;

public abstract class PlayerBlockCategoryDropdownInputNew extends PlayerGameDropDownInput {

	public ObjectArrayList<GUIElement> additionalElements;
	private GUISearchBar searchBar;
	private Object info;
	private int[] numberValue;
	private GUIActivatableTextBar[] numberInputBar;
	private boolean includeMeta;

	public PlayerBlockCategoryDropdownInputNew(String windowId, GameClientState state, Object info, ObjectArrayList<GUIElement> additionalElements, int numberInput, int initialNumber, boolean includeMeta) {
		super(windowId, state, UIScale.getUIScale().scale(480), UIScale.getUIScale().scale(180+numberInput*30), info, UIScale.getUIScale().scale(32));

		this.includeMeta = includeMeta;
		this.additionalElements = additionalElements;

		this.info = info;
		searchBar = new GUISearchBar(state, Lng.str("FILTER DROPDOWN"), ((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().getContent(0), new TextCallback() {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void newLine() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback,
			                                 String prefix) throws PrefixNotFoundException {
				return null;
			}


		}, t -> {
			System.err.println("INPUT CGHANGED " + t);
			updateDropdown(t);
			return t;
		});

		searchBar.setPos(0, UIScale.getUIScale().h, 0);
		((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().getContent(0).attach(searchBar);

		if (numberInput > 0) {
			numberInputBar = new GUIActivatableTextBar[numberInput];
			numberValue = new int[numberInput];
			
			for(int i = 0; i < numberInputBar.length; i++){
				int index = i;
				String placeholder = switch(i) {
					case 0 -> Lng.str("AMOUNT");
					case 1 -> Lng.str("PULL UP TO");
					default -> Lng.str("VALUE");
				};
				numberInputBar[i] = new GUIActivatableTextBar(state, fontSize, placeholder, ((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().getContent(0), new TextCallback() {
	
					@Override
					public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
					}
	
					@Override
					public void onFailedTextCheck(String msg) {
					}
	
					@Override
					public void newLine() {
					}
	
					@Override
					public String handleAutoComplete(String s, TextCallback callback,
					                                 String prefix) throws PrefixNotFoundException {
						return null;
					}
	
					@Override
					public String[] getCommandPrefixes() {
						return null;
					}
				}, t -> {
					try {
						numberValue[index] = Integer.parseInt(t.trim());
					} catch (NumberFormatException e) {
					}
					;
					return t;
				});
				if (initialNumber > 0) {
					numberInputBar[i].appendText(String.valueOf(initialNumber));
				}
				numberInputBar[i].setPos(0, 98+index*30, 0);
				numberInputBar[i].getTextArea().setLimit(8);
				((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().getContent(0).attach(0, numberInputBar[i]);
			}
		}
		updateDropdown("");
	}

	public PlayerBlockCategoryDropdownInputNew(String windowId, GameClientState state, Object info, int numberInput, int initialNumber, boolean includeMeta) {
		this(windowId, state, info, null, numberInput, initialNumber, includeMeta);

	}

	private void updateDropdown(String text) {
		update(getState(), info, 32, "", getElements(getState(), text, additionalElements));
	}



	public ObjectArrayList<GUIElement> getElements(GameClientState state, String contain, ObjectArrayList<GUIElement> additionalElements) {

		ObjectArrayList<GUIElement> g = new ObjectArrayList<GUIElement>();

		if (additionalElements != null) {
			for (GUIElement a : additionalElements) {
				if (a.getUserPointer() == null || contain.trim().length() == 0 || a.getUserPointer().toString().toLowerCase(Locale.ENGLISH).contains(contain.trim().toLowerCase(Locale.ENGLISH))) {
					g.add(a);
				}
			}
		}
		addCatRecursive(ElementKeyMap.getCategoryHirarchy(), contain, state, g, -1);
		
		String m = "Meta Items";
		if ((contain.trim().length() == 0 || m.toLowerCase(Locale.ENGLISH).contains(contain.trim().toLowerCase(Locale.ENGLISH))) && includeMeta){
			GUIAnchor guiAnchor = new GUIAnchor(state, UIScale.getUIScale().scale(800), UIScale.getUIScale().scale(32));
			g.add(guiAnchor);

			GUITextOverlay t = new GUITextOverlay(FontSize.TINY_12, state);
			t.setTextSimple(m);
			guiAnchor.setUserPointer(MetaObject.class);

			t.getPos().x = UIScale.getUIScale().scale(3);
			t.getPos().y = UIScale.getUIScale().scale(7);

			guiAnchor.attach(t);
		}
		
		
		

		return g;
	}
	private ObjectArrayList<ElementInformation> lTmp = new ObjectArrayList<ElementInformation>();
	private void addCatRecursive(ElementCategory categoryHirarchy, String contain, GameClientState state, ObjectArrayList<GUIElement> g, int lvl) {
		lTmp.clear();
		categoryHirarchy.getInfoElementsRecursive(lTmp);
		boolean okItems = false;
		for(ElementInformation i : lTmp){
			if(!i.isDeprecated() && i.isShoppable()){
				okItems = true;
				break;
			}
		}
		if(okItems){
			if(lvl >= 0){
				String cat = categoryHirarchy.getCategory();
			
			
				if (contain.trim().length() == 0 || cat.toLowerCase(Locale.ENGLISH).contains(contain.trim().toLowerCase(Locale.ENGLISH))) {
					GUIAnchor guiAnchor = new GUIAnchor(state, 800, 32);
					g.add(guiAnchor);
		
					GUITextOverlay t = new GUITextOverlay(FontSize.TINY_12, state);
					t.setTextSimple(cat);
					guiAnchor.setUserPointer(categoryHirarchy);
		
					t.getPos().x = 3+lvl*5;
					t.getPos().y = 7;
		
					guiAnchor.attach(t);
		
				}	
			
			}
			
			for(ElementCategory child : categoryHirarchy.getChildren()){
				addCatRecursive(child, contain, state, g, lvl+1);
			}
		}
	}


	@Override
	public void onDeactivate() {
	}

	@Override
	public void pressedOK(GUIListElement current) {
		if (current.getContent().getUserPointer() != null && current.getContent().getUserPointer() instanceof ElementCategory) {
			onOk((ElementCategory) current.getContent().getUserPointer());
		} else if (current.getContent().getUserPointer() == MetaObject.class) {
			onOkMeta();
		} else {
			onAdditionalElementOk(current.getContent().getUserPointer());
		}
		deactivate();
	}

	public abstract void onAdditionalElementOk(Object userPointer);

	public abstract void onOk(ElementCategory info);
	public abstract void onOkMeta();

	/**
	 * @return the numberValue
	 */
	public int getNumberValue(int index) {
		return numberValue[index];
	}
}
