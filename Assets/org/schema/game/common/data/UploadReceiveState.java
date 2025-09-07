package org.schema.game.common.data;

import java.io.DataOutputStream;
import java.io.File;

public class UploadReceiveState {
	public DataOutputStream uploadOutputStream;
	public boolean ok = true;
	public boolean finished;
	public File file;
}
