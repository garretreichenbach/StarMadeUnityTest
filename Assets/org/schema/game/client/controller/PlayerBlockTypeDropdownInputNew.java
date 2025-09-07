package org.schema.game.client.controller;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
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

public abstract class PlayerBlockTypeDropdownInputNew extends PlayerGameDropDownInput {

	public ObjectArrayList<GUIElement> additionalElements;
	private GUISearchBar searchBar;
	private Object info;
	private boolean numberInput;
	private int[] numberValue;
	private GUIActivatableTextBar[] numberInputBar;
	private boolean includeMeta;

	
	public void setTextNumber(int index, int number){
		numberValue[index] = number;
		numberInputBar[index].setText(String.valueOf(number));
		numberInputBar[index].getTextArea().selectAll();
	}
	public PlayerBlockTypeDropdownInputNew(String windowId, GameClientState state, Object info, ObjectArrayList<GUIElement> additionalElements, int numberInput, int initialNumber, boolean includeMeta) {
		super(windowId, state, UIScale.getUIScale().scale(480),  UIScale.getUIScale().scale(180+numberInput*30), info, UIScale.getUIScale().scale(32));

		this.includeMeta = includeMeta;
		this.additionalElements = additionalElements;

		this.info = info;
		searchBar = new GUISearchBar(state, Lng.str("FILTER DROPDOWN"), ((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().getContent(0), new TextCallback() {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
				if(!PlayerBlockTypeDropdownInputNew.this.numberInput){
					GUIListElement selectedValue = PlayerBlockTypeDropdownInputNew.this.getSelectedValue();
					if(selectedValue != null){
						pressedOK(selectedValue);
					}
				}
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
				return s;
			}


		}, t -> {
			System.err.println("INPUT CHANGED " + t);
			updateDropdown(t);
			return t;
		});
		searchBar.setDeleteOnEnter(false);
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
						if(getSelectedValue() != null && getSelectedValue().getUserPointer() != null){
							pressedOK(getSelectedValue());
						}else{
							System.err.println("[CLIENT)[NumberInputBar] USER POINTER NULL");
						}
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
				numberInputBar[i].setDeleteOnEnter(false);
				numberInputBar[i].setPos(0, 98+index*30, 0);
				numberInputBar[i].getTextArea().setLimit(8);
				((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().getContent(0).attach(0, numberInputBar[i]);
				numberInputBar[i].getTextArea().selectAll();
			}

		}
		updateDropdown("");
	}


	public PlayerBlockTypeDropdownInputNew(String windowId, GameClientState state, Object info, int numberInput, int initialNumber, boolean includeMeta) {
		this(windowId, state, info, null, numberInput, initialNumber, includeMeta);

	}

	private void updateDropdown(String text) {
		update(getState(), info, UIScale.getUIScale().scale(32), "", getElements(getState(), text, additionalElements));
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
		if(includeMeta){
			for(MetaObjectType t :MetaObjectType.values() ){
				
				if(MetaObjectManager.subIdTypes.contains(t.type)){
					short[] sTypes = MetaObjectManager.getSubTypes(t);
					
					for(short s : sTypes){
						MetaObject instantiate = MetaObjectManager.instantiate(t.type, s, false);		
						addMeta(state, instantiate, contain, g);
					}
					
				}else{
					MetaObject instantiate = MetaObjectManager.instantiate(t.type, (short)-1, false);		
					addMeta(state, instantiate, contain, g);
				}
			}
		}
		for (ElementInformation info : ElementKeyMap.sortedByName) {
			if(includeInfo(info)){
				if (contain.trim().length() == 0 || info.getName().toLowerCase(Locale.ENGLISH).contains(contain.trim().toLowerCase(Locale.ENGLISH))) {
					GUIAnchor guiAnchor = new GUIAnchor(state, 800, UIScale.getUIScale().scale(32));
					g.add(guiAnchor);
	
					GUITextOverlay t = new GUITextOverlay(FontSize.TINY_12, state);
					t.setTextSimple(info.getName());
					guiAnchor.setUserPointer(info);
	
					GUIBlockSprite b = new GUIBlockSprite(state, info.getId());
					b.getScale().set(0.5f, 0.5f, 0.5f);
	
					guiAnchor.attach(b);
	
					t.getPos().x = 50;
					t.getPos().y = 7;
	
					guiAnchor.attach(t);
	
				}
			}
		}
		
		

		return g;
	}

	protected boolean includeInfo(ElementInformation info) {
		return true;
	}

	private void addMeta(GameClientState state, MetaObject instantiate, String contain, ObjectArrayList<GUIElement> g) {
		if (contain.trim().length() == 0 || instantiate.getName().toLowerCase(Locale.ENGLISH).contains(contain.trim().toLowerCase(Locale.ENGLISH))) {
			GUIAnchor guiAnchor = new GUIAnchor(state, UIScale.getUIScale().scale(800), UIScale.getUIScale().scale(32));
			g.add(guiAnchor);

			GUITextOverlay t = new GUITextOverlay(FontSize.TINY_12, state);
			t.setTextSimple(instantiate.getName());
			guiAnchor.setUserPointer(instantiate);

//			GUIBlockSprite b = new GUIBlockSprite(state, info.getId());
//			b.getScale().set(0.5f, 0.5f, 0.5f);
//
//			guiAncor.attach(b);

			t.getPos().x = 50;
			t.getPos().y = 7;

			guiAnchor.attach(t);
		}
	}

	@Override
	public void onDeactivate() {
	}

	@Override
	public void pressedOK(GUIListElement current) {
		if (current.getContent().getUserPointer() != null && current.getContent().getUserPointer() instanceof ElementInformation) {
			onOk((ElementInformation) current.getContent().getUserPointer());
		} else if (current.getContent().getUserPointer() != null && current.getContent().getUserPointer() instanceof MetaObject) {
			onOkMeta((MetaObject) current.getContent().getUserPointer());
		} else {
			onAdditionalElementOk(current.getContent().getUserPointer());
		}
		deactivate();
	}

	public abstract void onAdditionalElementOk(Object userPointer);

	public abstract void onOk(ElementInformation info);
	public abstract void onOkMeta(MetaObject object);
	public int getNumberValue(){
		return getNumberValue(0);
	}
	/**
	 * @return the numberValue
	 */
	public int getNumberValue(int index) {
		return numberValue[index];
	}
}
