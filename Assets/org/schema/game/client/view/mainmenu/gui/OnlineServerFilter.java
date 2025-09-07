package org.schema.game.client.view.mainmenu.gui;

import java.io.IOException;

import org.schema.game.common.version.VersionContainer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.AbstractServerInfo;

public class OnlineServerFilter extends GUIObservable{

	public boolean isCompatible() {
		return EngineSettings.SERVERLIST_COMPATIBLE.isOn();
	}
	public void setCompatible(boolean b) {
		EngineSettings.SERVERLIST_COMPATIBLE.setOn(b);
		try {
			EngineSettings.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
		notifyObservers();
	}
	public boolean isResponsive() {
		return EngineSettings.SERVERLIST_RESPONSIVE.isOn();
	}
	public void setResponsive(boolean b) {
		EngineSettings.SERVERLIST_RESPONSIVE.setOn(b);
		try {
			EngineSettings.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
		notifyObservers();
	}
	public boolean isFavorites() {
		return EngineSettings.SERVERLIST_FAVORITES.isOn();
	}
	public boolean isCustoms() {
		return EngineSettings.SERVERLIST_CUSTOMS.isOn();
	}
	public void setCustoms(boolean b) {
		EngineSettings.SERVERLIST_CUSTOMS.setOn(b);
		try {
			EngineSettings.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
		notifyObservers();
	}
	public void setFavorites(boolean b) {
		EngineSettings.SERVERLIST_FAVORITES.setOn(b);
		try {
			EngineSettings.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
		notifyObservers();
	}
	public boolean isFiltered(AbstractServerInfo info) {
		if(isFavorites() && !info.isFavorite()){
			return true;
		}
		if(isCustoms() && !info.isCustom()){
			return true;
		}
		if(isResponsive() && !info.isResponsive()){
			return true;
		}
		if(isCompatible() && VersionContainer.compareVersion(info.getVersion()) != 0){
			return true;
		}
		return false;
	}

}
