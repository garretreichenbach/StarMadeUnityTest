package org.schema.schine.common.language.editor;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.schema.schine.common.language.LanguageReader;
import org.schema.schine.common.language.Translation;
import org.schema.schine.resource.FileExt;
import org.xml.sax.SAXException;

import com.bulletphysics.util.ObjectArrayList;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class LanguageEditor extends Observable{

	private static int close;
	public boolean changesPending;
	public List<Translation> list;
	public int total;
	public int missing;
	public int translated;
	public int selectionIndex;
	private String languageName;

	public JList translationList;
	protected boolean autofillDupe;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenu mnEdit;
	private JMenuItem mntmNew;
	private JMenuItem mntmSave;
	private JMenuItem mntmSaveAs;
	private JCheckBoxMenuItem mntmAutoSave;
	private JMenuItem mntmLoad;
	private boolean init;
	private JMenuItem mntmExit;
	private File lastFile;
	private SearchDialog searchDiag;
	private boolean autosave = true;
	private JFileChooser fc;
	File defaultLanguage = new FileExt("."+File.separator+"data"+File.separator+"language"+File.separator+"defaultPack.xml");
	private JFrame f;
	
	public void init() throws IOException, SAXException, ParserConfigurationException{
		
		
//		LanguageReader.loadLangFile("."+File.separator+"language"+File.separator+"english"+File.separator+"pack.xml", params);
		
		
		f = new JFrame("StarMade Language Editor");
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	    manager.addKeyEventDispatcher(new DB());
		buildMenu(f);
		
		f.requestFocus();
		
		f.setSize(1000, 600);
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - f.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - f.getHeight()) / 2);
	    f.setLocation(x, y);
		LanguageEditorMainPanel main = new LanguageEditorMainPanel(f, this);
		f.setContentPane(main);
		f.setVisible(true);
		f.setDefaultCloseOperation(close);
		
		
		
	}
	Integer autosaveCount = 0;
	
	private ObjectArrayFIFOQueue<List<Translation>> cc = new ObjectArrayFIFOQueue<List<Translation>>();
	private JMenuItem mntmSearch;
	private JMenuItem mntmImportFromCrowdin;
	public List<Translation> get(){
		synchronized(cc){
			if(cc.isEmpty()){
				final List<Translation> l = new ObjectArrayList<Translation>(list.size());
				for(int i = 0; i < list.size(); i++){
					l.add(new Translation());
				}
				return l;
			}else{
				return cc.dequeue();
			}
		}
	}
	private class DB implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.isControlDown() && e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_F && e.isControlDown()) {
                openSearch();
            } 
            return false;
        }
    }
	public void put(List<Translation> c){
		synchronized (cc) {
			cc.enqueue(c);
		}
	}
	
	public void autosave(final JFrame f){
		
		
		
		final List<Translation> l = get();
		for(int i = 0; i < l.size(); i++){
			l.get(i).set(list.get(i));
		}
		
		(new Thread(() -> {
			File file = new FileExt("."+File.separator+"language_autosave_"+autosaveCount+".xml");
			System.err.println("AUTOSAVE: "+file);
			save(f, file, l);
			synchronized(autosaveCount){
				autosaveCount = (autosaveCount+1)%10;
			}
			put(l);
		})).start();
	}
	private void save(JFrame f, File output, List<Translation> list) {
		try{
			LanguageReader.save(output, languageName, null, list);
			changesPending = false;
		}catch(Exception e){
			
			e.printStackTrace();
			JOptionPane.showMessageDialog(f,
				    "Error loading file. Check logs",
				    "error",
				    JOptionPane.ERROR_MESSAGE);
		}
	}
	private void load(Frame f, File file) {
		try {
			calculateInital(file, defaultLanguage);
			EventQueue.invokeLater(() -> {
				try {
					setChanged();
					notifyObservers();
					translationList.setSelectedIndex(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			
			e.printStackTrace();
			JOptionPane.showMessageDialog(f,
				    "Error loading file. Check logs",
				    "error",
				    JOptionPane.ERROR_MESSAGE);
		} 
		changesPending = false;
	}
	private void buildMenu(final JFrame f) {
		menuBar = new JMenuBar();
		f.setJMenuBar(menuBar);
		
		f.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F){
					if(e.isShiftDown()){
						openSearch();
					}
				}
			}
		});
		
		mnFile = new JMenu("File");
		mnEdit = new JMenu("Edit");
		menuBar.add(mnFile);
		menuBar.add(mnEdit);
		mntmSearch = new JMenuItem("Search");
		mntmSearch.addActionListener(e -> openSearch());
		mnEdit.add(mntmSearch);
		mntmNew = new JMenuItem("New Translation");
		mntmNew.addActionListener(e -> {
			if(!init){
				languageName = (String)JOptionPane.showInputDialog(
	                    f,
	                    "Enter Language Name",
	                    "New Language",
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    null,
	                    "");
				try {

					calculateInital(defaultLanguage, defaultLanguage);
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(f,
						    "Error loading default file. Check logs",
						    "error",
						    JOptionPane.ERROR_MESSAGE);
					e2.printStackTrace();
				}
				f.setTitle("StarMade Language Editor ("+languageName+")");
				missing = 0;
				translated = 0;
				total = 0;
				for(Translation a : list){
					if(a.translator.equals("default")){
						missing ++;
					}else{
						translated++;
					}
					total++;
				}
				EventQueue.invokeLater(() -> {
					try {
						setChanged();
						notifyObservers();
						translationList.setSelectedIndex(0);
					} catch(Exception e14) {
						e14.printStackTrace();
					}
				});
				init = true;;
			}else{
				Object[] options = {"Ok", "Cancel"};
				int n = JOptionPane.showOptionDialog(f, "Do you want to start a new translation?\nAll unsaved progress will be lost!", "Warning",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, options[1]);
				switch (n) {
					case 0:
						    languageName = (String)JOptionPane.showInputDialog(
			                    f,
			                    "Enter Language Name",
			                    "New Language",
			                    JOptionPane.PLAIN_MESSAGE,
			                    null,
			                    null,
			                    "");
						    f.setTitle("StarMade Language Editor ("+languageName+")");
							lastFile = null;
							for(Translation l : list){
								l.translation = l.original;
								l.translator = "default";
							}
							missing = 0;
							translated = 0;
							total = 0;
							for(Translation a : list){
								if(a.translator.equals("default")){
									missing ++;
								}else{
									translated++;
								}
								total++;
							}
							EventQueue.invokeLater(() -> {
								try {
									setChanged();
									notifyObservers();
									//									((TranslationListModel)translationList.getModel()).allChanged();
								} catch(Exception e13) {
									e13.printStackTrace();
								}
							});

						break;
					case 1:
						break;
					}
			}

		});
		mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(arg0 -> {
			if(lastFile == null){
				File fl = chooseFile(f, "Save As...");
				if (fl != null) {
					lastFile = fl;
				}
			}
			if(lastFile != null){
				save(f, lastFile, list);
			}
		});
		
		mntmSaveAs = new JMenuItem("Save as...");
		mntmSaveAs.addActionListener(arg0 -> {
			File fl = chooseFile(f, "Save As...");
			if (fl != null) {
				lastFile = fl;
			}
			if(lastFile != null){
				save(f, lastFile, list);
			}
		});
		mntmAutoSave = new JCheckBoxMenuItem("Auto-Save");
		mntmAutoSave.setSelected(autosave);
		mntmAutoSave.addActionListener(e -> autosave = mntmAutoSave.isSelected());
		mntmLoad = new JMenuItem("Load");
		mntmLoad.addActionListener(arg0 -> {
			File fl = chooseFile(f, "Load...");
			if (fl != null) {
				lastFile = fl;
			}
			if(lastFile != null){
				load(f, lastFile);
			}
		});
		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(e -> {
			if(changesPending){
				Object[] options = {"Ok", "Cancel"};
				int n = JOptionPane.showOptionDialog(f, "You have unsaved changes. Do you really want to exit?", "Warning",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, options[1]);
				switch (n) {
				case 0:
					if( close == JFrame.DISPOSE_ON_CLOSE){
						f.dispose();
					}else{
						try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);;
					}
					break;
				case 1:
					break;
				}
			}else{
				if( close == JFrame.DISPOSE_ON_CLOSE){
					f.dispose();
				}else{
					try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);;
				}
			}
		});
		
		mnFile.add(mntmNew);
		mnFile.add(mntmSave);
		mnFile.add(mntmSaveAs);
		mnFile.add(mntmAutoSave);
		mnFile.add(mntmLoad);
		
		mntmImportFromCrowdin = new JMenuItem("Import from Crowdin XML");
		mntmImportFromCrowdin.addActionListener(e -> {
			if(!init){
				languageName = (String)JOptionPane.showInputDialog(
	                    f,
	                    "Enter Language Name",
	                    "Import Language",
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    null,
	                    "");
				try {

					calculateInital(defaultLanguage, defaultLanguage);
					handleImport(f);
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(f,
						    "Error loading default file. Check logs",
						    "error",
						    JOptionPane.ERROR_MESSAGE);
					e2.printStackTrace();
				}
				f.setTitle("StarMade Language Editor ("+languageName+")");
				EventQueue.invokeLater(() -> {
					try {
						setChanged();
						notifyObservers();
						translationList.setSelectedIndex(0);
					} catch(Exception e12) {
						e12.printStackTrace();
					}
				});
				init = true;;
			}else{
				Object[] options = {"Ok", "Cancel"};
				int n = JOptionPane.showOptionDialog(f, "Do you want to start a new translation?\nAll unsaved progress will be lost!", "Warning",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, options[1]);
				switch (n) {
					case 0:
						    languageName = (String)JOptionPane.showInputDialog(
			                    f,
			                    "Enter Language Name",
			                    "Import",
			                    JOptionPane.PLAIN_MESSAGE,
			                    null,
			                    null,
			                    "");
						    f.setTitle("StarMade Language Editor ("+languageName+")");
							lastFile = null;
							for(Translation l : list){
								l.translation = l.original;
								l.translator = "default";
							}

							handleImport(f);

							EventQueue.invokeLater(() -> {
								try {
									setChanged();
									notifyObservers();
									//									((TranslationListModel)translationList.getModel()).allChanged();
								} catch(Exception e1) {
									e1.printStackTrace();
								}
							});

						break;
					case 1:
						break;
					}
			}
		});
		mnFile.add(mntmImportFromCrowdin);
		mnFile.add(mntmExit);
	}
	private void handleImport(JFrame frame) {
		File f = chooseImportFile(frame, "choose import file");
		
		
		try {
			LanguageReader.importFileXML(f, list);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		missing = 0;
		translated = 0;
		total = 0;
		for(Translation a : list){
			if(a.translator.equals("default")){
				missing ++;
			}else{
				translated++;
			}
			total++;
		}
		EventQueue.invokeLater(() -> {
			try {
				((TranslationListModel)translationList.getModel()).allChanged();
				setChanged();
				notifyObservers();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	protected void openSearch() {
		if(searchDiag == null){
			searchDiag = new SearchDialog();
			searchDiag.editor = LanguageEditor.this;
		}
		searchDiag.setVisible(true);
	}
	private File chooseFile(JFrame frame, String title) {
		if (fc == null) {
			fc = new JFileChooser(new FileExt("./"));
			FileFilter fileFilter = new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					if (arg0.isDirectory()) {
						return true;
					}
					if (arg0.getName().endsWith(".xml")) {
						return true;
					}
					return false;
				}

				@Override
				public String getDescription() {
					return "StarMade Language (.xml)";
				}
			};
			fc.addChoosableFileFilter(fileFilter);
			fc.setFileFilter(fileFilter);
			fc.setAcceptAllFileFilterUsed(false);
		}
		//Show it.
		int returnVal = fc.showDialog(frame, title);

		//Process the results.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		} else {
			return null;
		}
	}
	private File chooseImportFile(JFrame frame, String title) {
		if (fc == null) {
			fc = new JFileChooser(new FileExt("./"));
			FileFilter fileFilter = new FileFilter() {
				
				@Override
				public boolean accept(File arg0) {
					if (arg0.isDirectory()) {
						return true;
					}
					if (arg0.getName().endsWith(".zip")) {
						return true;
					}
					return false;
				}
				
				@Override
				public String getDescription() {
					return "Crowdin(Android) archive (.zip)";
				}
			};
			FileFilter fileFilter2 = new FileFilter() {
				
				@Override
				public boolean accept(File arg0) {
					if (arg0.isDirectory()) {
						return true;
					}
					if (arg0.getName().endsWith(".xml")) {
						return true;
					}
					return false;
				}
				
				@Override
				public String getDescription() {
					return "Crowdin(Android) (.xml)";
				}
			};
			fc.addChoosableFileFilter(fileFilter);
			fc.addChoosableFileFilter(fileFilter2);
			fc.setFileFilter(fileFilter);
			fc.setAcceptAllFileFilterUsed(false);
		}
		//Show it.
		int returnVal = fc.showDialog(frame, title);
		
		//Process the results.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		} else {
			return null;
		}
	}
	private void calculateInital(File file, File defaultFile) throws IOException, SAXException, ParserConfigurationException {
		
		Object2ObjectAVLTreeMap<String, Translation> param = new Object2ObjectAVLTreeMap<String, Translation>();
		
		LanguageReader.loadLangFile(file, defaultFile, param);
		this.list = new ObjectArrayList<Translation>(param.size());
		for(Translation a : param.values()){
			list.add(a);
		}
		
		
		
		for(Translation a : list){
			a.dupl = new it.unimi.dsi.fastutil.objects.ObjectArrayList();
			for(Translation b : list){
				if(a != b && a.original.equals(b.original)){
					a.dupl.add(b);
				}
			}
		}
		missing = 0;
		translated = 0;
		total = 0;
		for(Translation a : list){
			if(a.translator.equals("default")){
				missing ++;
			}else{
				translated++;
			}
			total++;
		}
	}
	

	public static void main(String[] sdf){
		LanguageEditor e = new LanguageEditor();
		if(sdf.length > 0 && sdf[0].length() > 0){
			close = JFrame.DISPOSE_ON_CLOSE;
		}else{
			close = JFrame.EXIT_ON_CLOSE;
		}
		try {
			e.init();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
	}
	
	public void translationListSelectionChanged( final int i) {
		EventQueue.invokeLater(() -> {
			try {
				LanguageEditor.this.selectionIndex = i;
				setChanged();
				notifyObservers();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void fillDupl(Translation t) {
		for(final Translation d : t.dupl){
			if(!d.translation.equals(t.translation)){
				changesPending = true;
				translated++;
				missing--;
				d.translator = "modified";
				d.translation = new String(t.translation);
				EventQueue.invokeLater(() -> {
					if(selectionIndex >= 0 && selectionIndex < list.size()){
						((TranslationListModel)translationList.getModel()).changed(list.indexOf(d));
						setChanged();
						notifyObservers();
					}
				});
			}
		}
		
	}
	public void onChangeSelection(boolean fillDupl) {
		
		if(selectionIndex >= 0 && selectionIndex < list.size()){
			
			Translation lastSelected = list.get(selectionIndex);
			if(fillDupl){
				fillDupl(lastSelected);
			}
			if(!lastSelected.original.equals(lastSelected.translation)){
				System.err.println("TT\n\n\""+lastSelected.original+"\"\n\n\""+lastSelected.translation+"\n");
				if(lastSelected.translator.equals("default")){
					translated++;
					missing--;
				}
				changesPending = true;
				lastSelected.translator = "modified";
			}else{
//				if(!lastSelected.translator.equals("default")){
//					translated--;
//					missing++;
//				}
				changesPending = true;
//				lastSelected.translator = "default";
				
			}
			if(lastSelected.changed){
				if(autosave){
					autosave(f);
				}
				lastSelected.changed = false;
			}
		}
		EventQueue.invokeLater(() -> {
			if(selectionIndex >= 0 && selectionIndex < list.size()){
			((TranslationListModel)translationList.getModel()).changed(selectionIndex);
			setChanged();
			notifyObservers();
			}
		});
	}
	public void search(String text, boolean inTransaltion, boolean next) {
		if(list == null){
			return;
		}
		for(int i = 0; i < list.size(); i++){
			int index = (i+(selectionIndex+1))%list.size();
			Translation t = list.get(index);
			if(inTransaltion && t.translation.contains(text)){
				selectionIndex = index;
				onChangeSelection(autofillDupe);
				translationList.setSelectedIndex(selectionIndex);
				translationList.ensureIndexIsVisible(translationList.getSelectedIndex());
				return;
			}else if(!inTransaltion && t.original.contains(text)){
				selectionIndex = index;
				onChangeSelection(autofillDupe);
				translationList.setSelectedIndex(selectionIndex);
				translationList.ensureIndexIsVisible(translationList.getSelectedIndex());
				return;
			}
		}
	}
}
