package org.schema.game.common.data.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.FileExt;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PlayerSkin {

	public static final String EXTENSION = ".smskin";
	public static final String mainPatt = "_main_diff.png";
	public static final String mainEMPatt = "_main_em.png";
	public static final String helmetPatt = "_helmet_diff.png";
	public static final String helmetEMPatt = "_helmet_em.png";
	public String mainName;
	public String helmetName;
	public int mainTexId;
	public int mainEMId;
	public int helmetTexId;
	public int helmetEMId;

	public static PlayerSkin create(File tmp, String playerName) throws IOException {

		PlayerSkin s = new PlayerSkin();

		File[] listFiles = tmp.listFiles();

		s.mainTexId = load(listFiles, playerName, mainPatt);
		s.mainEMId = load(listFiles, playerName, mainEMPatt);
		s.helmetTexId = load(listFiles, playerName, helmetPatt);
		s.helmetEMId = load(listFiles, playerName, helmetEMPatt);

		return s;
	}

	private static int load(File[] listFiles, String playerName, String pattern) throws IOException {
		for (int i = 0; i < listFiles.length; i++) {
			if (listFiles[i].getName().endsWith(pattern)) {
				Controller.getResLoader().getImageLoader().loadImage(listFiles[i].getAbsolutePath(), playerName + pattern, true);
				try {
					return (Controller.getResLoader().getSprite(playerName + pattern).getMaterial().getTexture().getTextureId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		throw new FileNotFoundException("Cannot find vital texture of skin ending with: " + pattern);
	}

	public static File createSkinFile(File to, String main, String mainEM, String helmet,
	                                  String helmetEM) throws IOException {
		File mainFile = new FileExt(main);
		File mainEmissionFile = new FileExt(mainEM);
		File helmetFile = new FileExt(helmet);
		File helmetEmissionFile = new FileExt(helmetEM);

		String personal = to.getAbsolutePath();

		byte[] buffer = new byte[1024];

		FileOutputStream fos = new FileOutputStream(personal);
		ZipOutputStream zos = new ZipOutputStream(fos);

		System.out.println("Output to Zip : " + personal);

		add(zos, mainFile, "skin" + mainPatt);
		add(zos, mainEmissionFile, "skin" + mainEMPatt);
		add(zos, helmetFile, "skin" + helmetPatt);
		add(zos, helmetEmissionFile, "skin" + helmetEMPatt);

		//remember close it
		zos.close();

		System.out.println("Done");
		return new FileExt(personal);

	}

	private static void add(ZipOutputStream zos, File toAdd, String inZipName) throws IOException {
		byte[] buffer = new byte[1024];
		System.out.println("File Added : " + inZipName);
		ZipEntry ze = new ZipEntry(inZipName);
		zos.putNextEntry(ze);
		FileInputStream in = new FileInputStream(toAdd);
		int len;
		while ((len = in.read(buffer)) > 0) {
			zos.write(buffer, 0, len);
		}
		in.close();
		zos.closeEntry();
	}

	public boolean containsMesh(Mesh mesh) {
		return mesh.getName().equals("PlayerMdl") || mesh.getName().equals("AccHelmet");
	}

	public int getDiffuseIdFor(Mesh mesh) {
		if (mesh.getName().equals("PlayerMdl")) {
			return mainTexId;
		} else {
			return helmetTexId;
		}
	}

	public int getEmissiveIdFor(Mesh mesh) {
		if (mesh.getName().equals("PlayerMdl")) {
			return mainEMId;
		} else {
			return helmetEMId;
		}
	}
}
