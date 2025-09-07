package org.schema.schine.resource;

import java.io.File;
import java.net.URI;

import org.schema.schine.network.server.ServerController;

public class FileExt extends File{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileExt(File parent, String child) {
		super(parent, child);
	}

	public FileExt(String parent, String child) {
		super(parent, child);
	}

	public FileExt(String pathname) {
		super(pathname);
	}

	public FileExt(URI uri) {
		super(uri);
	}

	@Override
	public boolean delete() {
		if(ServerController.debugLogoutOnShutdown){
			try {
				throw new Exception("##DEBUG## DELETING FILE: "+getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.delete();
	}

	@Override
	public void deleteOnExit() {
		if(ServerController.debugLogoutOnShutdown){
			try {
				throw new Exception("##DEBUG## DELETING FILE ON EXIT: "+getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.deleteOnExit();
	}

	@Override
	public boolean renameTo(File dest) {
		if(ServerController.debugLogoutOnShutdown){
			try {
				throw new Exception("##DEBUG## RENAME TO: "+getAbsolutePath()+" -> "+dest.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.renameTo(dest);
	}

}
