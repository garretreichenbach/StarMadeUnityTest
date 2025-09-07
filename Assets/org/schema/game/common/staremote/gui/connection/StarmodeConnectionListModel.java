package org.schema.game.common.staremote.gui.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractListModel;

import org.schema.game.common.staremote.Staremote;
import org.schema.schine.resource.FileExt;

public class StarmodeConnectionListModel extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	

	public final ArrayList<StarmoteConnection> list = new ArrayList<StarmoteConnection>();

	public StarmodeConnectionListModel() {
		try {
			load();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void add(StarmoteConnection c) {
		list.add(c);
		save();
		try {
			load();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public Object getElementAt(int i) {
		return list.get(i);
	}

	public void load() throws NumberFormatException, IOException {
		list.clear();
		File f = new FileExt(Staremote.getConnectionFilePath());
		if (!f.exists()) {
			throw new FileNotFoundException();
		} else {
			BufferedReader fr = new BufferedReader(new FileReader(f));
			String readLine;
			;
			while ((readLine = fr.readLine()) != null) {
				String[] nameHostPort = readLine.split(",", 21);
				String loginName = nameHostPort[0];
				String[] hostPort = nameHostPort[1].split(":", 2);
				String host = hostPort[0];
				int port = Integer.parseInt(hostPort[1]);

				StarmoteConnection con = new StarmoteConnection(host, port, loginName);

				list.add(con);

			}
			fr.close();
		}
		fireContentsChanged(this, 0, list.size() - 1);
	}

	public void remove(StarmoteConnection c) {
		list.remove(c);
		save();
		try {
			load();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save() {

		try {
			File f = new FileExt(Staremote.getConnectionFilePath());
			if (!f.exists()) {
				f.createNewFile();
			}
			BufferedWriter fw = new BufferedWriter(new FileWriter(f));
			for (StarmoteConnection p : list) {
				fw.append(p.username + "," + p.url + ":" + p.port + "\n");
			}
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
