package org.schema.schine.resource.tag;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class TagTools {

	public byte[] inflateBuffer = new byte[5 * 1024 * 1024];
	public byte[] inputBuffer = new byte[5 * 1024 * 1024];
	public FastByteArrayInputStream input = new FastByteArrayInputStream(inputBuffer);

	public byte[] deflateBuffer = new byte[5 * 1024 * 1024];
	public byte[] outputBuffer = new byte[5 * 1024 * 1024];
	public FastByteArrayOutputStream output = new FastByteArrayOutputStream(outputBuffer);
	public Deflater deflater = new Deflater();
	public Inflater inflater = new Inflater();
}
