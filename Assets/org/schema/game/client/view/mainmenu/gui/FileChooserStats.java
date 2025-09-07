package org.schema.game.client.view.mainmenu.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.FileChooserDialog;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public abstract class FileChooserStats extends GUIObservable implements OnInputChangedCallback, TextCallback, DropDownCallback{

	private File currentPath;
	private String selectedName;
	private FileFilter filters[];
	private FileFilter filter;
	private InputState state;
	
	public FileChooserDialog diag;
	
	public FileChooserStats(InputState state, File currentPath, String selectedName, FileFilter ... filter) {
		this.currentPath = currentPath;
		this.selectedName = selectedName;
		this.filters = filter;
		this.state = state;
	}
	
	public void getFiles(List<GUIFileEntry> c) {
		c.clear();
		
		
		if(currentPath.getAbsoluteFile().getParentFile() == null) {
			File[] listRoots = File.listRoots();
			for(File f : listRoots) {
				//add roots
				c.add(new GUIFileEntry(f));
			}
		}else {
			//addupwards
			c.add(new GUIFileEntry(null));
		}
		File[] files = currentPath.listFiles();
		if(files != null && files.length > 0) {
			for(File f : files) {
				c.add(new GUIFileEntry(f));
			}
		}


		Collections.sort(c);
	}


	public boolean isFilteredOut(GUIFileEntry e) {
		return filter != null && !e.isUpDir() && !filter.accept(e.file);
	}

	public void onDoubleClick(GUIFileEntry f) {
		if(f.isDirectory()) {
			if(f.isUpDir()) {
				onPathChanged(currentPath.getAbsoluteFile().getParentFile().getAbsolutePath());
				GUIFileChooserPanel.pathBar.setText(getCurrentPath()); //Todo: Band-aid fix, should be done in a better way
			}else {
				currentPath = f.file;
			}
			lastF = null;	
			lastSel = null;

			notifyObservers();
		}else {
			onPressedOk(diag);
		}
	}

	public void onSingleClick(GUIFileEntry f) {
		selectedName = f.getName();
		diag.onSelectedChanged(selectedName);
	}

	@Override
	public String[] getCommandPrefixes() {
		return null;
	}

	@Override
	public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
		return null;
	}

	@Override
	public void onFailedTextCheck(String msg) {
	}

	@Override
	public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
	}

	@Override
	public void newLine() {
	}

	@Override
	public String onInputChanged(String t) {
		return selectedName = t;
	}

	public Collection<? extends GUIElement> getFileTypesGUIElements() {
		List<GUIElement> l = new ObjectArrayList<GUIElement>();
		if(filters.length > 0) {
			for(FileFilter f : filters) {
				if(filter == null) {
					filter = f;
				}
				GUITextOverlay all = new GUITextOverlay(state);
				all.setTextSimple(f.getDescription());
				all.getPos().x = 5;
				all.getPos().y = 2;
				l.add(all);
				all.setUserPointer(f);
			}
		}else {
			GUITextOverlay all = new GUITextOverlay(state);
			all.setTextSimple(Lng.str("ANY FILE"));
			all.getPos().x = 5;
			all.getPos().y = 2;
			l.add(all);
			all.setUserPointer(null);
		}
		
		
		return l;
	}

	public void onPressedOk(DialogInput diag) {
		onSelectedFile(currentPath, selectedName);
		diag.deactivate();
	}

	public abstract void onSelectedFile(File dir, String file);

	public void onPressedCancel(DialogInput diag) {
		diag.deactivate();
	}

	@Override
	public void onSelectionChanged(GUIListElement element) {
		filter = element.getContent().getUserPointer() != null ? (FileFilter)element.getContent().getUserPointer() : null;
		notifyObservers();
	}

	public String getCurrentSelectedName() {
		return selectedName;
	}

	String lastSel = null;
	File lastF;
	public boolean isDirectorySelected() {
		if(lastSel == null || !selectedName.equals(lastSel)) {
			lastF = new File(currentPath, selectedName);	
			lastSel = selectedName;
		}
		return lastF.exists() && lastF.isDirectory();
	}

	public void onPressedDesktop() {
		String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		if(System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win")) {
			File homeFolder = new File(System.getProperty("user.home"));
			File[] files = homeFolder.listFiles();
			if(files != null) {
				for(File f : files) {
					if(f.getName().startsWith("OneDrive")) desktopPath = f.getAbsolutePath();
				}
				desktopPath += File.separator + "Desktop";
			}
		}
		onPathChanged(desktopPath);
	}

	public void onPressedHome() {
		onPathChanged(System.getProperty("user.home"));
	}

	public void onPressedComputer() {
		onPathChanged(File.separator);
	}

	public String getCurrentPath() {
		return currentPath.getAbsolutePath();
	}

	public String onPathChanged(String t) {
		currentPath = new File(t);
		lastF = null;
		lastSel = null;
		notifyObservers();
		return t;
	}
}
