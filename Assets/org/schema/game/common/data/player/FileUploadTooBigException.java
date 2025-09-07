package org.schema.game.common.data.player;

import java.io.File;

public class FileUploadTooBigException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public FileUploadTooBigException(File f) {
		super("cant upload " + f.getAbsolutePath() + ": file is too big in size");
	}

}
