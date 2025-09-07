package org.schema.schine.sound.controller.assignment;

import java.io.IOException;
import java.util.Locale;

import org.schema.schine.sound.controller.AudioPlaySettings;
import org.schema.schine.sound.controller.asset.AudioAsset;
import org.schema.schine.sound.controller.asset.AudioAssetManager;
import org.schema.schine.sound.controller.mixer.AudioMixer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class AudioAssignmentImpl implements AudioAssignment{
	private final AudioPlaySettings settings = new AudioPlaySettings();
	public AudioAsset assetPrimary;
	public AudioAsset assetSecondary;
	public AudioMixer mixer = AudioMixer.GUI;

	private String primLoad;
	private String secLoad;
	
	public static final String PRIMARY = "Primary";
	public static final String SECONDARY = "Secondary";

	@Override
	public AudioPlaySettings getSettings() {
		return settings;
	}
	
	public void resolveLoadedAsset(AudioAssetManager man) {
		if(primLoad != null) {
			AudioAsset audioAsset = man.assetsByPathLowerCase.get(primLoad.toLowerCase(Locale.ENGLISH));
			if(audioAsset == null) {
				try {
					throw new IOException("Asset not found "+primLoad);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			assetPrimary = audioAsset;
		}
		if(secLoad != null) {
			AudioAsset audioAsset = man.assetsByPathLowerCase.get(secLoad.toLowerCase(Locale.ENGLISH));
			if(audioAsset == null) {
				try {
					throw new IOException("Asset not found "+secLoad);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			assetPrimary = audioAsset;
		}
	}
	protected void parseAsset(Node item, int m) {
		
		if(m == 0) {
			primLoad = item.getTextContent();
		}else if(m == 1) {
			secLoad = item.getTextContent();
		}else {
			throw new RuntimeException("Invalid asset number "+m);
		}
	}
	@Override
	public boolean hasSound() {
		return assetPrimary != null;
	}

	@Override
	public AudioAsset getAssetPrimary() {
		return assetPrimary;
	}

	@Override
	public AudioAsset getAssetSecondary() {
		return assetSecondary;
	}

	@Override
	public void setPrimaryAsset(AudioAsset a) {
		assetPrimary = a;
	}

	@Override
	public void setSecondaryAsset(AudioAsset a) {
		assetSecondary = a;
	}

	@Override
	public AudioMixer getMixer() {
		return mixer;
	}

	public void writeAssets(Document doc, Node parent) {
		if(assetPrimary != null) {
			Element e = doc.createElement(PRIMARY);
			e.setTextContent(assetPrimary.getRelativePath());
			parent.appendChild(e);
		}
		if(assetSecondary != null) {
			Element e = doc.createElement(SECONDARY);
			e.setTextContent(assetSecondary.getRelativePath());
			parent.appendChild(e);
		}
	}
	
	@Override
	public void setAudioMixer(AudioMixer mixer) {
		this.mixer = mixer;
	}
	public void parseMixer(String textContent) {
		for(AudioMixer m : AudioMixer.mixersExposed) {
			if(m.getName().toLowerCase(Locale.ENGLISH).equals(textContent.toLowerCase(Locale.ENGLISH))) {
				mixer = m;
				return;
			}
		}
		throw new RuntimeException("Couldn't parse mixer: "+textContent+"; Available: "+AudioMixer.mixersExposed);
	}
}
