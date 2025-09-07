package org.schema.game.client.data;

import api.common.GameClient;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.player.SavedCoordinate;
import org.schema.game.common.data.world.RemoteSector;

public class ClientGameData {
	private final GameClientState state;
	private Vector3i waypoint;

	private Vector3i nearestToWayPoint;
	private Vector3i tmp = new Vector3i();
	private Vector3i ltmp = new Vector3i();

	public ClientGameData(GameClientState state) {
		this.state = state;
	}

	/**
	 * @return the nearestToWayPoint
	 */
	public Vector3i getNearestToWayPoint() {
		return nearestToWayPoint;
	}

	/**
	 * @return the waypoint
	 */
	public Vector3i getWaypoint() {
		return waypoint;
	}

	/**
	 * @param waypoint the waypoint to set
	 */
	public void setWaypoint(Vector3i waypoint) {
		System.err.println("SETTING WAYPOINT: " + waypoint);
		this.waypoint = waypoint;
		nearestToWayPoint = null;
		updateNearest(state.getCurrentSectorId());
	}

	public void updateNearest(int currentSectorId) {
		if (waypoint != null) {
			RemoteSector newSector = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(currentSectorId);
			if (newSector == null) {
				state.getController().flagWaypointUpdate = true;
				return;
			}
			Vector3i clientPos = newSector.clientPos();

			if (nearestToWayPoint == null) {
				nearestToWayPoint = new Vector3i();
			}

			if (clientPos.equals(waypoint)) {
				setWaypoint(null);
				return;
			} else {
				ltmp.set(0, 0, 0);
				for (int i = 0; i < Element.DIRECTIONSi.length; i++) {

					tmp.add(clientPos, Element.DIRECTIONSi[i]);

					if (tmp.equals(waypoint)) {
						nearestToWayPoint.add(clientPos, Element.DIRECTIONSi[i]);
						break;
					}

					//					System.err.println("CHECKING WAYPOINT: "+clientPos+": "+tmp);
					tmp.sub(waypoint);
					if (ltmp.length() == 0 || tmp.length() < ltmp.length()) {
						nearestToWayPoint.add(clientPos, Element.DIRECTIONSi[i]);
						ltmp.set(tmp);
					} else {
						System.err.println("NOT TAKING: " + tmp.length() + " / " + ltmp.length());
					}
				}
				System.err.println("NEAREST WAYPOINT " + nearestToWayPoint);
			}

		}

	}

	public ObjectArrayList<SavedCoordinate> getSavedCoordinates() {
		return GameClient.getClientPlayerState().getSavedCoordinates();
	}

	public void addSavedCoordinate(SavedCoordinate savedCoordinate) {
		state.getController().getClientChannel().sendSavedCoordinateToServer(savedCoordinate);
		getSavedCoordinates().add(savedCoordinate);
	}

	public void removeSavedCoordinate(SavedCoordinate savedCoordinate) {
		state.getController().getClientChannel().removeSavedCoordinateToServer(savedCoordinate);
		getSavedCoordinates().remove(savedCoordinate);
	}

	public boolean hasWaypointAt(Vector3i entryPos) {
		for(SavedCoordinate savedCoordinate : getSavedCoordinates()) {
			if(savedCoordinate.getSector().equals(entryPos)) return true;
		}
		return false;
	}
}
