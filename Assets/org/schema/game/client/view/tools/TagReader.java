package org.schema.game.client.view.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.commons.io.FileUtils;
import org.schema.game.common.Starter;
import org.schema.schine.resource.tag.Tag;

public class TagReader {

	public static void main(String[] args) {
		JFrame f = new JFrame("Tag Reader");
		Starter.registerSerializableFactories();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(1024, 800);
		JFileChooser chooser = new JFileChooser("./server-database");
		int returnVal = chooser.showDialog(f, "Tag");

		try {
//			File[] ds = chooser.getSelectedFile().getParentFile().listFiles();
//			for(File file : ds) {
//				if(file.getName().contains("FUD_Shuttle_Hangar_Done_1556723037123")) {
//
//
//					File newFile = new File(chooser.getSelectedFile().getParentFile(), file.getName().replaceAll("FUD_Shuttle_Hangar_Done_1556723037123", "Forward Unto Dawn"));
//					FileUtils.copyFile(file, newFile);
//					System.err.println("RENAMED: "+file.getName()+" -> "+newFile.getCanonicalPath());
//				}
//			}
			Starter.initialize(true);
			Tag readFrom = Tag.readFrom(new BufferedInputStream(new FileInputStream(chooser.getSelectedFile())), true, true);
			f.setContentPane(new TagExplorerPanel(readFrom));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		f.setVisible(true);
	}
}
