package org.schema.schine.common;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.glfw.GLFW;
import org.schema.common.util.StringTools;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.List;

public class TextAreaInput implements ClipboardOwner, InputHandler, InputCharHandler {

	private final List<String> chatLog;
	private final StringBuffer chatBuffer;
	public TabCallback onTabCallback;
	public boolean deleteEntryOnEnter = true;
	private int linewrap = -1;
	private int chatCarrier;
	private String cache = "";
	private int selectionStart = -1, selectionEnd = -1;
	private boolean bufferChanged;
	private int chatReverseIndex;
	private OnInputChangedCallback onInputChangedCallback;
	private String cacheCarrier = "";
	private String cacheSelect = "";
	private String cacheSelectStart = "";
	private TextCallback callback;
	private int limit;
	private int lineLimit = 1;
	private String error = "";
	private int minimumLength = 1;
	private int lineIndex;
	private int carrierLineIndex;
	private boolean allowEmptyEntry;
	private InputChecker inputChecker = (entry, callback) -> {
		if(entry.length() >= minimumLength || isAllowEmptyEntry()) {
			return true;
		} else {
			callback.onFailedTextCheck("Minimum length required: " + minimumLength);
			return false;
		}
	};

	public TextAreaInput(int limit, int lineLimit, TextCallback callback) {
		chatBuffer = new StringBuffer(limit);
		chatLog = new ObjectArrayList<String>();
		this.limit = limit;
		this.lineLimit = lineLimit;
		this.callback = callback;
		chatLog.add("/god_mode schema true");
		chatLog.add("/start_ship_ai -1");
		chatLog.add("/give_category_items schema 100 ship");
		chatLog.add("/give_credits schema 999999999");
		chatLog.add("/god_mode schema true");
		chatLog.add("/create_spawner_test");
		chatLog.add("/jump");

	}

	/**
	 * Get the String residing on the clipboard.
	 *
	 * @return any text found on the Clipboard; if none found, return an
	 * empty String.
	 * @throws IOException
	 * @throws UnsupportedFlavorException
	 */
	public static String getClipboardContents(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText =
				(contents != null) &&
						contents.isDataFlavorSupported(flavor);
		if(hasTransferableText) {
			result = (String) contents.getTransferData(flavor);
		}
		return result;
	}

	public void append(String str) {
		if(str == null || str.length() == 0) return;
		if(selectionEnd >= 0 && selectionStart >= 0) {
			int s = Math.min(selectionStart, selectionEnd);
			int e = Math.max(selectionStart, selectionEnd);
			chatBuffer.delete(s, e);
			chatCarrier = s;
			bufferChanged = true;
		}
		for(int i = 0; i < str.length() && chatBuffer.length() < limit && (lineLimit == 1 || lineIndex < lineLimit - 1); i++) {
			if(lineLimit == 1 && str.charAt(i) == '\n') {
				continue;
			}
			if(str.charAt(i) == '\n') {
				lineIndex++;
			}

			chatBuffer.insert(chatCarrier, str.charAt(i));
			//			System.err.println("CURENT CARRIER: " + chatCarrier + "; "
			//					+ chatBuffer.toString());
			chatCarrier++;
			bufferChanged = true;
		}

		resetSelection();
	}

	public void chatKeyBackspace() {
		if(chatBuffer.length() > 0) {
			(new Thread("BackspaceRunner") {
				@Override
				public void run() {
					while(Keyboard.isKeyDown(GLFW.GLFW_KEY_BACKSPACE)) { //Keep deleting while the key is down
						try {
							if(selectionEnd >= 0 && selectionStart >= 0) {
								int s = Math.min(selectionStart, selectionEnd);
								int e = Math.max(selectionStart, selectionEnd);
								chatBuffer.delete(s, e);
								chatCarrier = s;
								resetSelection();
							} else if(chatCarrier > 0) {
								chatBuffer.delete(Math.max(0, chatCarrier - 1), chatCarrier);
								--chatCarrier;
								chatCarrier = Math.max(0, chatCarrier);
							}
							bufferChanged = true;
							sleep(100);
						} catch(InterruptedException exception) {
							exception.printStackTrace();
							return;
						}
					}
				}
			}).start();
		}
		resetSelection();
	}

	public void chatKeyDelete() {
		if(selectionEnd >= 0 && selectionStart >= 0) {
			int s = Math.min(selectionStart, selectionEnd);
			int e = Math.max(selectionStart, selectionEnd);
			chatBuffer.delete(s, e);
			chatCarrier = s;
			resetSelection();
		} else if(chatCarrier < chatBuffer.length()) {

			if(selectionEnd >= 0 && selectionStart >= 0) {
				int s = Math.min(selectionStart, selectionEnd);
				int e = Math.max(selectionStart, selectionEnd);
				chatBuffer.delete(s, e);
				chatCarrier = s;
				resetSelection();
			} else {
				chatBuffer.delete(chatCarrier, chatCarrier + 1);
			}
			bufferChanged = true;
		}
		resetSelection();
	}

	public void chatKeyDown() {

		if(lineLimit == 1) {
			chatReverseIndex--;
			if(chatReverseIndex > 0) {
				chatBuffer.delete(0, chatBuffer.length());
				chatBuffer.append(chatLog.get(chatLog.size() - chatReverseIndex));
				System.err.println("chat carrier reset!!! down "
						+ chatBuffer.length());
				chatCarrier = chatBuffer.length();
			} else {
				chatReverseIndex = 0;
				chatBuffer.delete(0, chatBuffer.length());
				System.err.println("chat carrier reset!!! keyDown");
				resetChatCarrier();
			}
		} else {
			if(carrierLineIndex < lineIndex) {
				int previousNL = cacheCarrier.lastIndexOf("\n");
				int nextNL = chatBuffer.indexOf("\n", chatCarrier);
				if(nextNL >= 0) {

					int newLineOfNextLine = chatBuffer.indexOf("\n", nextNL + 1);
					if(newLineOfNextLine < 0) {
						newLineOfNextLine = chatBuffer.length();
					}
					int max = newLineOfNextLine - nextNL;
					System.err.println("MAX " + max + " / " + (nextNL - chatCarrier) + "; next: " + nextNL + " NNext " + newLineOfNextLine);
					int am = (nextNL) + Math.min(max, (chatCarrier - previousNL));
					while(chatCarrier < chatBuffer.length() && chatCarrier < am) {
						chatCarrier++;
					}
				} else {
					System.err.println("DOWN: " + previousNL + " ---- " + nextNL);
				}
			}
		}
		resetSelection();
		bufferChanged = true;
	}

	public void chatKeyEnd() {
		int before = chatCarrier;
		chatCarrier = Math.max(0, chatBuffer.length());
		updateSelection(before);
		bufferChanged = true;
	}

	public void chatKeyLeft() {
		int before = chatCarrier;
		if(chatCarrier > 0) {
			chatCarrier--;
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)
					|| Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
				while(chatCarrier > 0
						&& ' ' == chatBuffer.charAt(chatCarrier - 1)) {
					chatCarrier--;
				}

				while(chatCarrier > 0
						&& ' ' != chatBuffer.charAt(chatCarrier - 1)) {
					chatCarrier--;
				}
			}
			bufferChanged = true;
		}
		updateSelection(before);

	}

	public void chatKeyPos1() {
		int before = chatCarrier;
		System.err.println("chat carrier reset!!! pos1");
		resetChatCarrier();
		updateSelection(before);
		bufferChanged = true;
	}

	public void chatKeyRight() {
		int before = chatCarrier;
		if(chatCarrier < chatBuffer.length()) {
			chatCarrier++;
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)
					|| Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {

				while(chatCarrier < chatBuffer.length()
						&& ' ' != chatBuffer.charAt(chatCarrier)) {
					System.err.println("chat carrier reset!!! right ");
					chatCarrier++;
				}
				while(chatCarrier < chatBuffer.length()
						&& ' ' == chatBuffer.charAt(chatCarrier)) {
					System.err.println("chat carrier reset!!! right ");
					chatCarrier++;
				}

			}
			bufferChanged = true;
		}
		updateSelection(before);
	}

	public void chatKeyUp() {
		if(lineLimit == 1) {
			chatReverseIndex++;
			if(chatReverseIndex <= chatLog.size()) {
				chatBuffer.delete(0, chatBuffer.length());
				chatBuffer.append(chatLog.get(chatLog.size() - chatReverseIndex));
				System.err.println("chat carrier reset!!! up "
						+ chatBuffer.length());
				chatCarrier = chatBuffer.length();
			} else {
				chatReverseIndex = chatLog.size();
			}
		} else {
			if(carrierLineIndex > 0) {
				int previousNL = Math.max(0, cacheCarrier.lastIndexOf("\n"));

				if(previousNL >= 0) {
					String sub = cacheCarrier.substring(0, previousNL);
					int newLineOfprevLine = sub.lastIndexOf("\n");

					int max = previousNL - newLineOfprevLine;
					int am = newLineOfprevLine + Math.min(max, ((chatCarrier - previousNL)));
					System.err.println("CHAT CARRIER: " + chatCarrier + " -> " + am);
					while(chatCarrier > 0 && chatCarrier > am) {
						chatCarrier--;
					}
				}
			}
		}
		resetSelection();
		bufferChanged = true;
	}

	public void clear() {
		resetSelection();
		chatBuffer.delete(0, chatBuffer.length());
		chatCarrier = 0;
		bufferChanged = true;
		update();
	}

	public void copy() {
		System.err.println("trying copy");
		if(selectionEnd >= 0 && selectionStart >= 0) {
			setClipboardContents(cacheSelect);
			System.err.println("Copied to clipboard: " + cacheSelect);
		}
	}

	public void cut() {
		copy();
		if(selectionEnd >= 0 && selectionStart >= 0) {
			int s = Math.min(selectionStart, selectionEnd);
			int e = Math.max(selectionStart, selectionEnd);
			System.err.println("current: " + chatBuffer.toString());
			chatBuffer.delete(s, e);
			chatCarrier = s;
			resetSelection();
			bufferChanged = true;
		}
	}

	public void enter() {

		if(chatBuffer.length() >= 0 || allowEmptyEntry) {

			String entry = chatBuffer.toString();
			if(inputChecker != null && !inputChecker.check(entry, callback)) {
				return;
			}
			callback.onTextEnter(entry, !entry.startsWith("/"), false);


			chatLog.add(entry);
			if(deleteEntryOnEnter) {
				chatBuffer.delete(0, chatBuffer.length());

				resetChatCarrier();
			} else {

			}
			chatReverseIndex = 0;

			bufferChanged = true;
			resetSelection();

		} else {

		}
		if(!deleteEntryOnEnter) {
			String entry = chatBuffer.toString();
			if(onInputChangedCallback != null) {
				onInputChangedCallback.onInputChanged(entry);
			}
		}
		if(onTabCallback != null) {
			onTabCallback.onEnter();
		}
	}

	/**
	 * @return the cache
	 */
	public String getCache() {
		return cache;
	}

	public void setCache(String cache) {
		this.cache = cache;
	}

	/**
	 * @return the cacheCarrier
	 */
	public String getCacheCarrier() {
		return cacheCarrier;
	}

	/**
	 * @param cacheCarrier the cacheCarrier to set
	 */
	public void setCacheCarrier(String cacheCarrier) {
		this.cacheCarrier = cacheCarrier;
	}

	/**
	 * @return the cacheSelect
	 */
	public String getCacheSelect() {
		return cacheSelect;
	}

	/**
	 * @param cacheSelect the cacheSelect to set
	 */
	public void setCacheSelect(String cacheSelect) {
		this.cacheSelect = cacheSelect;
	}

	/**
	 * @return the cacheSelectStart
	 */
	public String getCacheSelectStart() {
		return cacheSelectStart;
	}

	/**
	 * @param cacheSelectStart the cacheSelectStart to set
	 */
	public void setCacheSelectStart(String cacheSelectStart) {
		this.cacheSelectStart = cacheSelectStart;
	}

	/**
	 * @return the carrierLineIndex
	 */
	public int getCarrierLineIndex() {
		return carrierLineIndex;
	}

	/**
	 * @return the chatCarrier
	 */
	public int getChatCarrier() {
		return chatCarrier;
	}

	/**
	 * @param chatCarrier the chatCarrier to set
	 */
	public void setChatCarrier(int chatCarrier) {
		this.chatCarrier = chatCarrier;
	}

	/**
	 * @return the chatLog
	 */
	public List<String> getChatLog() {
		return chatLog;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public InputChecker getInputChecker() {
		return inputChecker;
	}

	public void setInputChecker(InputChecker inputChecker) {
		this.inputChecker = inputChecker;
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getLineIndex() {
		return lineIndex;
	}

	/**
	 * @return the lineLimit
	 */
	public int getLineLimit() {
		return lineLimit;
	}

	/**
	 * @param lineLimit the lineLimit to set
	 */
	public void setLineLimit(int lineLimit) {
		this.lineLimit = lineLimit;
	}

	/**
	 * @return the onAppendCallback
	 */
	public OnInputChangedCallback getOnInputChangedCallback() {
		return onInputChangedCallback;
	}

	/**
	 * @param onAppendCallback the onAppendCallback to set
	 */
	public void setOnInputChangedCallback(OnInputChangedCallback onAppendCallback) {
		this.onInputChangedCallback = onAppendCallback;
	}

	@Override
	public void handleCharEvent(KeyEventInterface e) {
		if(!e.isRightControl() && !e.isAnyControl() && !e.isAnyAlt()) {
			assert (e.getCharacter() != null) : e;
			assert (e.getCharacter().length() > 0) : e;
//			if (!GraphicsContext.OS.toLowerCase(Locale.ENGLISH).contains("mac") || GraphicsContext.IME || e.isPressed() ) {
			handleCharacterFromEvent(e);
//			}
		}
		update();
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {

		boolean handled = false;
		if(e.isPressed()) {
			switch(e.getKey()) {
				case (GLFW.GLFW_KEY_KP_ENTER):
				case (GLFW.GLFW_KEY_ENTER):
					if(lineLimit == 1) {
						enter();
					} else {
						if(lineIndex + 1 < lineLimit - 1) {
							append("\n");
							callback.newLine();
						} else {
							System.err.println("[TextAreaInput] line limit reached. lines " + (lineIndex + 1) + "/" + lineLimit);
						}
					}

					handled = true;

					break;
				case (GLFW.GLFW_KEY_DELETE):
					chatKeyDelete();
					handled = true;
					break;
				case (GLFW.GLFW_KEY_LEFT):
					chatKeyLeft();
					handled = true;
					break;
				case (GLFW.GLFW_KEY_RIGHT):
					chatKeyRight();
					handled = true;
					break;
				case (GLFW.GLFW_KEY_BACKSPACE):
					chatKeyBackspace();
					handled = true;
					break;
				case (GLFW.GLFW_KEY_UP):
					chatKeyUp();
					handled = true;
					break;
				case (GLFW.GLFW_KEY_DOWN):
					chatKeyDown();
					handled = true;
					break;
				case (GLFW.GLFW_KEY_HOME):
					chatKeyPos1();
					handled = true;
					break;
				case (GLFW.GLFW_KEY_END):
					chatKeyEnd();
					handled = true;
					break;

				case (GLFW.GLFW_KEY_V):
					if(e.isAnyControl()) {
						paste();
						handled = true;
						break;
					}
					break;
				case (GLFW.GLFW_KEY_C):
					if(e.isAnyControl()) {
						copy();
						handled = true;
						break;
					}
					break;
				case (GLFW.GLFW_KEY_A):
					if(e.isAnyControl()) {
						selectAll();
						handled = true;
						break;
					}
					break;
				case (GLFW.GLFW_KEY_X):
					if(e.isAnyControl()) {
						cut();
						handled = true;
						break;
					}
					break;
				case (GLFW.GLFW_KEY_TAB):
					onTab();
					handled = true;
					break;
				default:
					break;
			}
		}


		update();
	}


	public void handleCharacterFromEvent(KeyEventInterface e) {
		String eventCharacter = e.getCharacter();

		append(eventCharacter);
//		if (!Character.isIdentifierIgnorable(eventCharacter)) {
//			String keyName = String.valueOf(eventCharacter);
//			append(keyName);
//		}
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		System.out.println("Lost clipboard ownership " + this);
	}

	public void onInputChanged(String string) {
		if(onInputChangedCallback != null) {
			String ret = onInputChangedCallback.onInputChanged(string);
			if(ret != null && !ret.equals(string)) {
				int s = 0;
				int e = chatBuffer.length();
				chatBuffer.delete(s, e);
				chatCarrier = Math.max(0, s);
				resetSelection();
				append(ret);
				setBufferChanged();
			}
		}
	}

	private void onTab() {
		try {

			if(onTabCallback != null && onTabCallback.catchTab(this)) {
			} else {
				String[] prefixes = callback.getCommandPrefixes();
				if(prefixes != null) {
					for(int i = 0; i < prefixes.length; i++) {
						if(chatBuffer.length() >= prefixes[i].length() && chatBuffer.indexOf(prefixes[i]) == 0) {
							String s = chatBuffer.substring(prefixes[i].length()); //get rid of /
							chatBuffer.delete(0, chatBuffer.length());

							resetChatCarrier();
							chatReverseIndex = 0;

							bufferChanged = true;
							resetSelection();
							append(prefixes[i] + callback.handleAutoComplete(s, callback, prefixes[i]));
							return;
						}
					}
				}
				String s = chatBuffer.substring(0); //get rid of /
				chatBuffer.delete(0, chatBuffer.length());

				resetChatCarrier();
				chatReverseIndex = 0;

				bufferChanged = true;
				resetSelection();
				String handleAutoComplete = callback.handleAutoComplete(s, callback, "#");
				if(handleAutoComplete != null) {
					append(handleAutoComplete);
				}
			}
		} catch(PrefixNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void paste() {

		try {
			String clipboardContent = getClipboardContents(DataFlavor.stringFlavor);
			append(clipboardContent);
		} catch(UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	private void resetChatCarrier() {

		chatCarrier = 0;
	}

	public void resetSelection() {
		if(selectionEnd >= 0 || selectionStart >= 0) {
			selectionEnd = -1;
			selectionStart = -1;
			bufferChanged = true;
		}
		update();
	}

	public void selectAll() {
		if(chatBuffer.length() > 0) {
			selectionStart = 0;
			selectionEnd = chatBuffer.length();
			bufferChanged = true;
		}
	}

	public void setBufferChanged() {
		bufferChanged = true;
	}

	/**
	 * Place a String on the clipboard, and make this class the
	 * owner of the Clipboard's contents.
	 */
	public void setClipboardContents(String aString) {
		StringSelection stringSelection = new StringSelection(aString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, this);
	}

	public void update() {
		if(bufferChanged) {
			cache = chatBuffer.toString();
			if(cache.length() > 0) {
				cacheCarrier = cache.substring(0, chatCarrier);
				if(selectionStart >= 0 && selectionEnd >= 0) {
					int s = Math.min(selectionStart, selectionEnd);
					int e = Math.max(selectionStart, selectionEnd);
					cacheSelect = cache.substring(s, e);
					cacheSelectStart = cache.substring(0, s);
					//				System.err.println("SELECTION: ["+s+", "+e+"]");
				} else {
					cacheSelect = "";
					cacheSelectStart = "";
				}
			} else {
				chatCarrier = 0;
				cacheCarrier = "";
				cacheSelect = "";
				cacheSelectStart = "";
			}
			bufferChanged = false;
			onInputChanged(cache);
			lineIndex = StringTools.coundNewLines(chatBuffer.toString()) - 1;
			carrierLineIndex = StringTools.coundNewLines(cacheCarrier.toString()) - 1;
		}
		if(selectionStart >= 0 && (selectionStart == selectionEnd)) {
			resetSelection();
		}
//		try {
//			throw new Exception(getCache()+" OF "+this);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public void updateSelection(int before) {
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)
				|| Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
			if(selectionStart < 0) {
				selectionStart = before;
			}
			selectionEnd = chatCarrier;

		} else {
			resetSelection();
		}
	}

	/**
	 * @return the linewrap
	 */
	public int getLinewrap() {
		return linewrap;
	}

	/**
	 * @param linewrap the linewrap to set
	 */
	public void setLinewrap(int linewrap) {
		this.linewrap = linewrap;
	}

	/**
	 * @return the minimumLength
	 */
	public int getMinimumLength() {
		return minimumLength;
	}

	/**
	 * @param minimumLength the minimumLength to set
	 */
	public void setMinimumLength(int minimumLength) {
		this.minimumLength = minimumLength;
	}

	public boolean isAllowEmptyEntry() {
		return allowEmptyEntry;
	}

	public void setAllowEmptyEntry(boolean allowZeroEntry) {
		this.allowEmptyEntry = allowZeroEntry;
	}

}
