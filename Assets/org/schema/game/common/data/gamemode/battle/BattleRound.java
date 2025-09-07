package org.schema.game.common.data.gamemode.battle;

import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FactionChange;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.gamemode.battle.BattleMode.BattleSector;
import org.schema.game.common.data.gamemode.battle.BattleMode.FactionPreset;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.SectorImportRequest;
import org.schema.game.server.data.admin.AdminCommandQueueElement;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BattleRound {

	private static final int MIN_FFA = EngineSettings.MIN_FFA.getInt();
	private final BattleMode battleMode;
	public ObjectArrayList<PreparedFaction> preparedFactions = new ObjectArrayList<PreparedFaction>();
	public ObjectArrayList<BattleCondition> conditions = new ObjectArrayList<BattleCondition>();
	Vector3f halfSize = new Vector3f();
	private boolean alive = true;
	private long started;
	private BattleSector battleSector;
	private long warpedInTime;
	private ObjectArrayList<PlayerState> winners = new ObjectArrayList<PlayerState>();
	private ObjectArrayList<Faction> winnerFactions = new ObjectArrayList<Faction>();
	private String output = "";
	private int activeFactions;
	private long battleOverTime;
	private long resetTime;
	private int teamTotal;
	private int ffaTotal;
	private int teamTotalMass;
	private int ffaTotalMass;
	private long lastSentParticipationMessage = 0;
	private long lastSentFacMessage = 0;
	private String battleInfo = "";
	private ObjectArrayList<PlayerState> activePlayers = new ObjectArrayList<PlayerState>();

	public BattleRound(BattleMode battleMode) {
		this.battleMode = battleMode;
	}

	public void initialize() {
		System.err.println("[BATTLEMODE] Round " + this + " has been reset");
		conditions.clear();
		preparedFactions.clear();
		output = "";
		battleSector = null;
		winners.clear();
		winnerFactions.clear();
		warpedInTime = 0;
		resetTime = 0;
		battleOverTime = 0;
		battleInfo = "";
		started = System.currentTimeMillis();
		setupFactions();
		initBattleSector();
		initConditions();
	}

	private int fightingConditionOk() {
		boolean teamModeOk = false;
		boolean ffaModeOk = false;
		teamTotal = 0;
		ffaTotal = 0;
		teamTotalMass = 0;
		ffaTotalMass = 0;
		if (battleMode.battleFactions.size() > 1) {
			int factionsWithMoreThenOne = 0;

			for (Faction f : battleMode.battleFactions) {

				PlayerState biggestShipPlayer = null;
				float biggest = 0;

				int factionMembers = f.getMembersUID().size();
				if (factionMembers > 0) {
					boolean ok = true;
					for (PlayerState p : battleMode.state.getPlayerStatesByName().values()) {
						if (p.getFactionId() == f.getIdFaction()) {
							SimpleTransformableSendableObject firstControlledTransformableWOExc = p.getFirstControlledTransformableWOExc();
							if (firstControlledTransformableWOExc != null && firstControlledTransformableWOExc instanceof SegmentController) {

								if (!isOkToFight(((SegmentController) firstControlledTransformableWOExc), p)) {
									p.sendServerMessage(new ServerMessage(Lng.astr("Your ship doesn't meet\nmass or dimension requirements\n\nYou are currently\nexcluded from battle!"), ServerMessage.MESSAGE_TYPE_ERROR, p.getId()));
									factionMembers--;
								} else {
									float mass = ((SegmentController) firstControlledTransformableWOExc).getMass();
									if (mass > biggest) {
										biggestShipPlayer = p;
										biggest = mass;
									}

									teamTotalMass += mass;
								}
							}

						}
					}
				}

				if (factionMembers > 0 && battleMode.maxMassPerFaction > 0 && teamTotalMass > battleMode.maxMassPerFaction) {
					if (System.currentTimeMillis() - lastSentFacMessage > 4000) {
						String b = (biggestShipPlayer != null ? biggestShipPlayer.getName() : "n/a") + " (" + (int) biggest + " mass)";
						f.broadcastMessage(Lng.astr("Your faction exceeds max mass!\nYou are not participating\nin the next battle.\nPlayer in biggest ship:\n%s",  b), ServerMessage.MESSAGE_TYPE_ERROR, battleMode.state);
						lastSentFacMessage = System.currentTimeMillis();
					}
				} else {
					if (factionMembers > 0) {
						teamTotal += f.getMembersUID().size();
						factionsWithMoreThenOne++;
					}
				}

			}
			if (factionsWithMoreThenOne > 1) {
				//there are at least 2 battle factions with at least one member each
				teamModeOk = true;
			}
		}
		if (battleMode.ffaFactions.size() > 0) {
			for (Faction f : battleMode.ffaFactions) {

				PlayerState biggestShipPlayer = null;
				float biggest = 0;

				int factionMembers = f.getMembersUID().size();
				if (factionMembers >= MIN_FFA) {
					ffaTotal = Math.max(ffaTotal, f.getMembersUID().size());

					for (PlayerState p : battleMode.state.getPlayerStatesByName().values()) {
						if (p.getFactionId() == f.getIdFaction()) {
							SimpleTransformableSendableObject firstControlledTransformableWOExc = p.getFirstControlledTransformableWOExc();
							if (firstControlledTransformableWOExc != null && firstControlledTransformableWOExc instanceof SegmentController) {
								if (!isOkToFight((SegmentController) firstControlledTransformableWOExc, p)) {

									factionMembers--;
								} else {
									float mass = ((SegmentController) firstControlledTransformableWOExc).getMass();
									if (mass > biggest) {
										biggestShipPlayer = p;
										biggest = mass;
									}
									ffaTotalMass += mass;
								}
							}
						}
					}
				}
				if (factionMembers >= MIN_FFA && battleMode.maxMassPerFaction > 0 && ffaTotalMass > battleMode.maxMassPerFaction) {
					if (System.currentTimeMillis() - lastSentFacMessage > 4000) {
						String b = (biggestShipPlayer != null ? biggestShipPlayer.getName() : "n/a") + " (" + (int) biggest + " mass)";
						f.broadcastMessage(Lng.astr("FFA faction exceeds max mass!\nYou are not participating\nin the next battle.\nPlayer in biggest ship:\n",  b), ServerMessage.MESSAGE_TYPE_ERROR, battleMode.state);
						lastSentFacMessage = System.currentTimeMillis();
					}
				} else {
					if (factionMembers >= MIN_FFA) {
						//at least one ffa faction has more then one member
						ffaModeOk = true;
					}
				}
			}
		}
		//		System.err.println("FIGHTING CONDITIONS: TEAM: "+teamModeOk+"; "+teamTotal+", "+battleMode.battleFactions.size()+"; FFA: "+ffaModeOk+", "+ffaTotal+", "+battleMode.ffaFactions.size());
		if (teamModeOk && teamTotal >= ffaTotal) {
			return Faction.MODE_FIGHTERS_TEAM;
		} else if (ffaModeOk && ffaTotal > teamTotal) {
			return Faction.MODE_FIGHTERS_FFA;
		} else {
			//round isnt playable
			return 0;
		}
	}

	private boolean isOkToFight(SegmentController segmentController, PlayerState p) {
		if (battleMode.maxMass > 0 && segmentController.getMass() > battleMode.maxMass) {

			if (System.currentTimeMillis() - p.lastSectorProtectedMsgSent > 4000) {
				p.sendServerMessage(new ServerMessage(Lng.astr("Your ship doesn't meet\nmass requirements!\n\nYou are currently\nexcluded from battle."), ServerMessage.MESSAGE_TYPE_ERROR, p.getId()));
				p.lastSectorProtectedMsgSent = System.currentTimeMillis();
			}
			return false;

		}
		segmentController.getBoundingBox().calculateHalfSize(halfSize);
		if (battleMode.maxDim > 0 && (halfSize.x * 2 > battleMode.maxDim || halfSize.y * 2 > battleMode.maxDim || halfSize.z * 2 > battleMode.maxDim)) {
			if (System.currentTimeMillis() - p.lastSectorProtectedMsgSent > 4000) {
				p.sendServerMessage(new ServerMessage(Lng.astr("Your ship doesn't meet\ndimension requirements!\n\nYou are currently\nexcluded from battle."), ServerMessage.MESSAGE_TYPE_ERROR, p.getId()));
				p.lastSectorProtectedMsgSent = System.currentTimeMillis();
			}

			return false;
		}
		return true;
	}

	private String currentBattleRound(int fightingConditionOk) {

		if (fightingConditionOk == Faction.MODE_FIGHTERS_TEAM) {
			return "factions set for team battle";
		} else if (fightingConditionOk == Faction.MODE_FIGHTERS_FFA) {
			return "factions set for FFA battle";
		} else {
			return "factions don't have enough players";
		}
	}

	private void sendParticipatingMessages() {
		if (System.currentTimeMillis() - lastSentParticipationMessage > 4000) {
			int fightingConditionOk = fightingConditionOk();
			if (fightingConditionOk == Faction.MODE_FIGHTERS_TEAM) {
				for (PlayerState p : battleMode.state.getPlayerStatesByName().values()) {
					Faction f = battleMode.state.getFactionManager().getFaction(p.getFactionId());
					if (f != null && f.isFactionMode(Faction.MODE_FIGHTERS_TEAM)) {
						p.sendServerMessage(new ServerMessage(Lng.astr("You are up for the\nnext team battle!\n\nGET READY!\n(or leave the faction)"), ServerMessage.MESSAGE_TYPE_ERROR, p.getId()));
					} else if (f != null && f.isFactionMode(Faction.MODE_SPECTATORS)) {
						p.sendServerMessage(new ServerMessage(Lng.astr("You are spectating\nthe next battle!\n\n(to cancel, leave the faction)"), ServerMessage.MESSAGE_TYPE_ERROR, p.getId()));
					} else if (f != null && f.isFactionMode(Faction.MODE_FIGHTERS_FFA)) {
					}
				}
			} else if (fightingConditionOk == Faction.MODE_FIGHTERS_FFA) {
				for (PlayerState p : battleMode.state.getPlayerStatesByName().values()) {
					Faction f = battleMode.state.getFactionManager().getFaction(p.getFactionId());
					if (f != null && f.isFactionMode(Faction.MODE_FIGHTERS_FFA)) {
						p.sendServerMessage(new ServerMessage(Lng.astr("You are up for the\nnext FFA battle!\n\nGET READY!\n(or leave the faction)"), ServerMessage.MESSAGE_TYPE_ERROR, p.getId()));
					} else if (f != null && f.isFactionMode(Faction.MODE_SPECTATORS)) {
						p.sendServerMessage(new ServerMessage(Lng.astr("You are spectating\nthe next battle!\n\n(to cancel, leave the faction)"), ServerMessage.MESSAGE_TYPE_ERROR, p.getId()));
					} else if (f != null && f.isFactionMode(Faction.MODE_FIGHTERS_TEAM)) {
					}
				}
			}

			lastSentParticipationMessage = System.currentTimeMillis();
		}
	}

	private void warpInPlayers() {
		ObjectArrayList<SimpleTransformableSendableObject> avoidList = new ObjectArrayList<SimpleTransformableSendableObject>();
		for (PreparedFaction f : preparedFactions) {
			warpInPlayers(f, avoidList);
		}
	}

	private void warpInPlayer(String playerName, PreparedFaction f, ObjectArrayList<SimpleTransformableSendableObject> avoidList) {
		try {
			PlayerState playerFromName = battleMode.state.getPlayerFromName(playerName);

			if (!playerFromName.getCurrentSector().equals(battleSector.pos) && playerFromName.getFirstControlledTransformableWOExc() != null) {

				Vector3f spawn = new Vector3f(f.preset.spawnPos);
				if (f.faction.isFactionMode(Faction.MODE_FIGHTERS_FFA)) {

					spawn.set(0, 0, 0);

					float sectorSize = battleMode.state.getSectorSize() - 200;

					spawn.set((float) Math.random() * sectorSize - (sectorSize / 2), (float) Math.random() * sectorSize - (sectorSize / 2), (float) Math.random() * sectorSize - (sectorSize / 2));

					System.err.println("[BATTLEMODE] FFA JUMP POS: " + spawn);
				} else {
					System.err.println("[BATTLEMODE] TEAM/SPEC JUMP POS: " + spawn);
				}

				for (ControllerStateUnit u : playerFromName
						.getControllerState().getUnits()) {

					SimpleTransformableSendableObject controllerRoot = AdminCommandQueueElement.getControllerRoot((SimpleTransformableSendableObject) u.playerControllable);

					SectorSwitch ss = battleMode.state.getController()
							.queueSectorSwitch(controllerRoot, battleSector.pos, SectorSwitch.TRANS_JUMP, false);

					if (ss != null) {
						avoidList.add((SimpleTransformableSendableObject) u.playerControllable);

						if (controllerRoot instanceof SegmentController) {
							((SegmentController) controllerRoot).getDockingController().setFactionAll(f.faction.getIdFaction());
						}
						//only make copy if enqueue worked
						ss.makeCopy = true;
						ss.avoidOverlapping = avoidList;
						ss.jumpSpawnPos = spawn;
					}
				}

				//			//DEBUG warp in all ships in the sector
				if (MIN_FFA == 1) {
					Sector sec = battleMode.state.getUniverse().getSector(playerFromName.getCurrentSectorId());
					ObjectArrayList<SimpleTransformableSendableObject> updateEntities = sec.updateEntities();
					for (SimpleTransformableSendableObject s : updateEntities) {
						spawn = new Vector3f(f.preset.spawnPos);
						if (f.faction.isFactionMode(Faction.MODE_FIGHTERS_FFA)) {

							spawn.set(0, 0, 0);

							float sectorSize = battleMode.state.getSectorSize() - 200;

							spawn.set((float) Math.random() * sectorSize - (sectorSize / 2), (float) Math.random() * sectorSize - (sectorSize / 2), (float) Math.random() * sectorSize - (sectorSize / 2));

							System.err.println("[BATTLEMODE] FFA JUMP POS: " + spawn);
						} else {
							System.err.println("[BATTLEMODE] TEAM/SPEC JUMP POS: " + spawn);
						}
						if (s instanceof Ship && !((Ship) s).getDockingController().isDocked()) {

							SectorSwitch ss = battleMode.state.getController()
									.queueSectorSwitch(
											s,
											battleSector.pos, SectorSwitch.TRANS_JUMP, true);
							if (ss != null) {
								avoidList.add(s);
								ss.avoidOverlapping = avoidList;
								ss.jumpSpawnPos = spawn;
							}
						}
					}
				}
			}
		} catch (PlayerNotFountException e) {
			e.printStackTrace();
		}
	}

	private void warpInPlayers(PreparedFaction f, ObjectArrayList<SimpleTransformableSendableObject> avoidList) {
		for (String playerName : f.faction.getMembersUID().keySet()) {
			warpInPlayer(playerName, f, avoidList);
		}
	}

	private void setupFactions() {
		for (Faction f : battleMode.battleFactions) {
			f.setServerOpenForJoin(battleMode.state, true);
		}
		for (Faction f : battleMode.ffaFactions) {
			f.setServerOpenForJoin(battleMode.state, true);
		}
		for (Faction f : battleMode.spectators) {
			f.setServerOpenForJoin(battleMode.state, true);
		}
	}

	private boolean prepareFactions() {
		int fightingConditionOk = fightingConditionOk();
		if (fightingConditionOk == Faction.MODE_FIGHTERS_TEAM) {
			for (Faction f : battleMode.battleFactions) {
				if (f.getMembersUID().size() > 0) {
					FactionPreset preset = null;
					for (FactionPreset pre : battleMode.battleFactionNames) {
						if (pre.name.equals(f.getName())) {
							preset = pre;
						}
					}
					if (preset == null) {
						battleMode.state.getController().broadcastMessage(Lng.astr("Not enough players to battle!\nPreset not found...\nResetting round!"), ServerMessage.MESSAGE_TYPE_ERROR);
						initConditions();
						return false;
					} else {
						preparedFactions.add(new PreparedFaction(preset, f));
					}
				}
			}
			if (preparedFactions.size() < 2) {
				battleMode.state.getController().broadcastMessage(Lng.astr("Not enough players to battle!\nFaction count invalid...\nResetting round!"), ServerMessage.MESSAGE_TYPE_ERROR);
				initConditions();
				return false;
			}
			addSpectators();

			return true;

		} else if (fightingConditionOk == Faction.MODE_FIGHTERS_FFA) {
			Faction biggest = battleMode.ffaFactions.get(0);

			for (Faction f : battleMode.ffaFactions) {
				if (f.getMembersUID().size() >= MIN_FFA && biggest.getMembersUID().size() < f.getMembersUID().size()) {
					biggest = f;
				}
			}
			FactionPreset preset = null;
			for (FactionPreset pre : battleMode.ffaFactionNames) {
				if (pre.name.equals(biggest.getName())) {
					preset = pre;
				}
			}
			if (preset == null) {
				battleMode.state.getController().broadcastMessage(Lng.astr("Not enough players to battle!\nPreset not found...\nResetting round!"), ServerMessage.MESSAGE_TYPE_ERROR);
				initConditions();
				return false;
			} else {
				preparedFactions.add(new PreparedFaction(preset, biggest));
			}
			addSpectators();

			return true;
		} else {
			battleMode.state.getController().broadcastMessage(Lng.astr("Not enough players to battle!\nFighting condition not met...\nResetting round!"), ServerMessage.MESSAGE_TYPE_ERROR);
			initConditions();
			return false;
		}
	}

	private String battleSplash(int fightingConditionOk) {
		StringBuffer splash = new StringBuffer();
		splash.append("Map: " + battleSector.sectorImport + "\n");
		splash.append("Max Mass/Ship: " + (battleMode.maxMass > 0 ? battleMode.maxMass : "unlimited") + "\n");
		splash.append("Max Mass/Faction: " + (battleMode.maxMassPerFaction > 0 ? battleMode.maxMassPerFaction : "unlimited") + "\n");
		splash.append("Max Dim/Ship: " + (battleMode.maxDim > 0 ? battleMode.maxDim : "unlimited") + "\n");

		if (fightingConditionOk == Faction.MODE_FIGHTERS_TEAM) {
			for (Faction f : battleMode.battleFactions) {
				if (f.getMembersUID().size() > 0) {
					splash.append("   " + f.getName() + "; Players: " + f.getMembersUID().size() + "; total mass: " + teamTotalMass + "\n");
				}
			}
		} else if (fightingConditionOk == Faction.MODE_FIGHTERS_FFA) {
			splash.append("Total players in FFA: " + ffaTotal + "; total mass: " + ffaTotalMass + "\n");
		}
		if (warpedInTime > 0) {
			splash.append("Players In current battle: ");
			for (PlayerState p : activePlayers) {
				splash.append(p.getName());
			}
		}

		battleInfo = splash.toString();
		return splash.toString();
	}

	private void initConditions() {
		BattleCondition countDown = new BattleCondition() {
			@Override
			protected boolean isMetCondition() {
				return ((System.currentTimeMillis() - started) >= battleMode.countdownRound * 1000);
			}

			@Override
			protected String getDescription() {

				sendParticipatingMessages();
				int fightingConditionOk = fightingConditionOk();
				battleSplash(fightingConditionOk);
				return "Round starting in T" + ((System.currentTimeMillis() - started) - battleMode.countdownRound * 1000) / 1000 + " secs [press '" + KeyboardMappings.LEADERBOARD_PANEL.getKeyChar() + "' for info]\n(" + currentBattleRound(fightingConditionOk) + ")\n";
			}

		};
		conditions.add(countDown);

		BattleCondition factionsOk = new BattleCondition() {
			@Override
			protected boolean isMetCondition() {
				if (fightingConditionOk() != 0) {
					//prepare factions
					if (!prepareFactions()) {
						return false;
					}
					return true;
				}
				battleMode.state.getController().broadcastMessage(Lng.astr("Not enough players to battle!\nFighting condition not met...\nResetting round!"), ServerMessage.MESSAGE_TYPE_ERROR);
				return false;
			}

			/* (non-Javadoc)
			 * @see org.schema.game.common.data.gamemode.battle.BattleCondition#onNotMetCondition()
			 */
			@Override
			protected void onNotMetCondition() {
				//reset everything
				battleMode.state.getController().broadcastMessage(Lng.astr("Not enough players to battle!\nFactions preparation failed...\nResetting round!"), ServerMessage.MESSAGE_TYPE_ERROR);
				initialize();
			}

			@Override
			protected String getDescription() {
				return "Checking faction conditions";
			}
		};
		conditions.add(factionsOk);

		BattleCondition replaceSector = new BattleCondition() {
			@Override
			protected boolean isMetCondition() {
				return battleMode.state.getUniverse().getSectorWithoutLoading(battleSector.pos) == null;
			}

			/* (non-Javadoc)
			 * @see org.schema.game.common.data.gamemode.battle.BattleCondition#onNotMetCondition()
			 */
			@Override
			protected void onNotMetCondition() {
				//reset everything
				battleMode.state.getController().broadcastMessage(Lng.astr("Cannot setup battle sector\nas it's currently loaded...\nResetting round!"), ServerMessage.MESSAGE_TYPE_ERROR);
				initialize();
				;
			}

			@Override
			protected String getDescription() {
				return "Checking battle sector";
			}
		};
		conditions.add(replaceSector);

		BattleCondition importSector = new BattleCondition() {
			@Override
			protected boolean isMetCondition() {

				if (battleMode.state.getUniverse().getSectorWithoutLoading(battleSector.pos) != null) {
					battleMode.state.getController().broadcastMessage(Lng.astr("Sector still active!\nRemoving players in sector!"), ServerMessage.MESSAGE_TYPE_ERROR);

					for (PlayerState player : battleMode.state.getPlayerStatesByName().values()) {
						if (player.getCurrentSector().equals(battleSector.pos)) {
							player.suicideOnServer();
						}
					}

					return false;
				}

				Vector3i sec = new Vector3i(battleSector.pos);
				String name = battleSector.sectorImport.replace(".smsec", "");

				SectorImportRequest r = new SectorImportRequest(sec, null, name);

				return (new FileExt("./sector-export/" + battleSector.sectorImport)).exists() && r.execute(battleMode.state);
			}

			/* (non-Javadoc)
			 * @see org.schema.game.common.data.gamemode.battle.BattleCondition#onNotMetCondition()
			 */
			@Override
			protected void onNotMetCondition() {
				//reset everything
				battleMode.state.getController().broadcastMessage(Lng.astr("Cannot setup battle sector!\nImport failed...\nResetting round!"), ServerMessage.MESSAGE_TYPE_ERROR);
				initialize();
				;
			}

			@Override
			protected String getDescription() {
				return "Resetting battle sector";
			}
		};
		conditions.add(importSector);

		BattleCondition loadBattleSector = new BattleCondition() {
			@Override
			protected boolean isMetCondition() {

				try {
					Sector s = battleMode.state.getUniverse().getSector(battleSector.pos, true);
					s.noEnter(true);
					s.noExit(true);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}

			/* (non-Javadoc)
			 * @see org.schema.game.common.data.gamemode.battle.BattleCondition#onNotMetCondition()
			 */
			@Override
			protected void onNotMetCondition() {
				//reset everything
				battleMode.state.getController().broadcastMessage(Lng.astr("Cannot load battle sectors...\nResetting round!"), ServerMessage.MESSAGE_TYPE_ERROR);
				initialize();
			}

			@Override
			protected void onMetCondition() {
				for (PlayerState player : battleMode.state.getPlayerStatesByName().values()) {
					Faction faction = battleMode.state.getFactionManager().getFaction(player.getFactionId());
					if (faction != null && faction.isFactionMode(Faction.MODE_SPECTATORS)) {
						player.getControllerState().forcePlayerOutOfSegmentControllers();
					}
				}
			}

			@Override
			protected String getDescription() {
				return "Loading in Battle Sector";
			}
		};
		conditions.add(loadBattleSector);

		BattleCondition warpPlayers = new BattleCondition() {

			@Override
			protected boolean isMetCondition() {
				warpInPlayers();
				return true;
			}

			@Override
			protected void onMetCondition() {
				warpedInTime = System.currentTimeMillis();
			}

			/* (non-Javadoc)
			 * @see org.schema.game.common.data.gamemode.battle.BattleCondition#onNotMetCondition()
			 */
			@Override
			protected void onNotMetCondition() {
				//reset everything
				battleMode.state.getController().broadcastMessage(Lng.astr("Cannot warp in players...\nResetting round!"), ServerMessage.MESSAGE_TYPE_ERROR);
				initialize();
				;
			}

			@Override
			protected String getDescription() {
				return "Warping in fighters";
			}
		};
		conditions.add(warpPlayers);

		BattleCondition doAutosave = new BattleCondition() {

			@Override
			protected boolean isMetCondition() {
				battleMode.state.getController().triggerForcedSave();
				return true;
			}

			@Override
			protected void onMetCondition() {
				warpedInTime = System.currentTimeMillis();
			}

			/* (non-Javadoc)
			 * @see org.schema.game.common.data.gamemode.battle.BattleCondition#onNotMetCondition()
			 */
			@Override
			protected void onNotMetCondition() {
				//reset everything
				battleMode.state.getController().broadcastMessage(Lng.astr("Cannot warp in players...\nResetting round!"), ServerMessage.MESSAGE_TYPE_ERROR);
				initialize();
				;
			}

			@Override
			protected String getDescription() {
				return "Autosaving to update sectors";
			}
		};
		conditions.add(doAutosave);

		BattleCondition freezeFighters = new BattleCondition() {
			@Override
			protected boolean isMetCondition() {

				try {
					battleMode.state.getGameState().serverRequestFrosenSector(battleMode.state.getUniverse().getSector(battleSector.pos, true).getId(), true);
				} catch (IOException e) {
					e.printStackTrace();
				}

				return ((System.currentTimeMillis() - warpedInTime) >= battleMode.countdownStart * 1000);
			}

			@Override
			protected void onMetCondition() {
				try {
					battleMode.state.getGameState().serverRequestFrosenSector(battleMode.state.getUniverse().getSector(battleSector.pos, true).getId(), false);
				} catch (IOException e) {
					e.printStackTrace();
				}
				//reset everything
				battleMode.state.getController().broadcastMessage(Lng.astr("BATTLE START!"), ServerMessage.MESSAGE_TYPE_INFO);
			}

			@Override
			protected String getDescription() {
				return "Warm up time. Battle starts in " + Math.abs(((System.currentTimeMillis() - warpedInTime) - battleMode.countdownStart * 1000) / 1000) + " secs (" + getBattleStatus() + ")";
			}

		};
		conditions.add(freezeFighters);

		BattleCondition checkBattle = new BattleCondition() {
			@Override
			protected boolean isMetCondition() {
				Sector sec = battleMode.state.getUniverse().getSectorWithoutLoading(battleSector.pos);
				if (sec == null) {
					battleMode.state.getController().broadcastMessage(Lng.astr("Round ended!\n(Sector unloaded)"), ServerMessage.MESSAGE_TYPE_ERROR);
					return true;
				}

				for (PlayerState player : battleMode.state.getPlayerStatesByName().values()) {
					Faction faction = battleMode.state.getFactionManager().getFaction(player.getFactionId());
					if (player.getCurrentSectorId() == sec.getId() && (faction == null || !(faction.isFactionMode(Faction.MODE_FIGHTERS_TEAM) || faction.isFactionMode(Faction.MODE_FIGHTERS_FFA)))) {
						player.getControllerState().forcePlayerOutOfShips();
					}

					if (player.getCurrentSectorId() != sec.getId() && faction != null && faction.isFactionMode(Faction.MODE_SPECTATORS)) {

						//warp spectator into battle
						ObjectArrayList<SimpleTransformableSendableObject> avoidList = new ObjectArrayList<SimpleTransformableSendableObject>();

						for (PreparedFaction f : preparedFactions) {

							if (f.faction.isFactionMode(Faction.MODE_SPECTATORS)) {

								warpInPlayer(player.getName(), f, avoidList);
							}
						}
					}
				}

				checkFaction();
				boolean team = false;
				for (PreparedFaction f : preparedFactions) {
					if (f.faction.getFactionMode() != Faction.MODE_SPECTATORS) {
						if (f.faction.getFactionMode() == Faction.MODE_FIGHTERS_TEAM) {
							team = true;

						} else if (f.faction.getFactionMode() == Faction.MODE_FIGHTERS_FFA) {
							//FFA
							if (f.activePlayersInSector < MIN_FFA) {
								battleMode.state.getController().broadcastMessage(Lng.astr("Round ended!\nFFA has ended!"), ServerMessage.MESSAGE_TYPE_ERROR);
								return true;
							}
						} else {
							throw new NullPointerException("invalid faction in prepared factions");
						}
					}
				}
				if (team && activeFactions < 2) {
					battleMode.state.getController().broadcastMessage(Lng.astr("Round ended!\nOnly %s faction remains.",  activeFactions), ServerMessage.MESSAGE_TYPE_ERROR);
					return true;
				}
				return false;
			}

			@Override
			protected String getDescription() {
				return "Battle is commencing! " + getBattleStatus();
			}

		};
		conditions.add(checkBattle);

		BattleCondition checkWinners = new BattleCondition() {

			@Override
			protected boolean isMetCondition() {
				return true;
			}

			@Override
			protected void onMetCondition() {
				for (PlayerState p : battleMode.state.getPlayerStatesByName().values()) {
					if (p.getControllerState().getUnits().size() > 0 && p.getCurrentSector().equals(battleSector.pos)) {
						Faction f = battleMode.state.getFactionManager().getFaction(p.getFactionId());
						if (f != null && (f.isFactionMode(Faction.MODE_FIGHTERS_TEAM) || f.isFactionMode(Faction.MODE_FIGHTERS_FFA))) {
							winners.add(p);
							if (f.isFactionMode(Faction.MODE_FIGHTERS_TEAM)) {
								if (!winnerFactions.contains(f)) {
									winnerFactions.add(f);
								}
							}
						}
					}
				}
				battleOverTime = System.currentTimeMillis();
			}

			@Override
			protected String getDescription() {
				return "Round ended! \nWinners: \n" + getWinnersString() + ";";
			}

		};
		conditions.add(checkWinners);

		BattleCondition killRemaining = new BattleCondition() {

			@Override
			protected boolean isMetCondition() {
				return ((System.currentTimeMillis() - battleOverTime) >= 5000);
			}

			@Override
			protected void onMetCondition() {
				for (PlayerState p : battleMode.state.getPlayerStatesByName().values()) {
					if (p.getControllerState().getUnits().size() > 0 && p.getCurrentSector().equals(battleSector.pos)) {
						p.suicideOnServer();
					}
				}
				resetTime = System.currentTimeMillis();
			}

			@Override
			protected String getDescription() {
				return "Round ended! Reset in " + Math.abs(((System.currentTimeMillis() - battleOverTime) - 5 * 1000) / 1000) + "! \nWinners: \n" + getWinnersString() + ";";
			}

		};
		conditions.add(killRemaining);

		BattleCondition showWinner = new BattleCondition() {
			@Override
			protected boolean isMetCondition() {
				return ((System.currentTimeMillis() - resetTime) >= 5000);
			}

			@Override
			protected void onMetCondition() {
			}

			@Override
			protected String getDescription() {
				return "Round over! \nWinners: \n" + getWinnersString();
			}

		};
		conditions.add(showWinner);
	}

	private String getBattleStatus() {
		StringBuffer s = new StringBuffer();
		for (PreparedFaction f : preparedFactions) {
			if (f.faction.getFactionMode() != Faction.MODE_SPECTATORS) {
				if (f.faction.getFactionMode() == Faction.MODE_FIGHTERS_TEAM) {
					s.append(f.faction.getName() + " (" + f.activePlayersInSector + "/" + f.faction.getMembersUID().size() + ")  ");
				} else if (f.faction.getFactionMode() == Faction.MODE_FIGHTERS_FFA) {
					//FFA
					s.append(f.faction.getName() + " (" + f.activePlayersInSector + "/" + f.faction.getMembersUID().size() + ")");
				} else {
					s.append("invalid faction in prepared factions");
				}
			}

		}
		return s.toString();
	}

	private String getWinnersString() {
		if (winners.isEmpty()) {
			return "nobody";
		}
		String winnerstr = "";
		for (Faction f : winnerFactions) {
			winnerstr += "Team '" + f.getName() + "':\n";
		}
		for (PlayerState f : winners) {
			winnerstr += "      " + f.getName() + "\n";
		}
		return winnerstr;
	}

	public void checkFaction() {
		activeFactions = 0;
		activePlayers.clear();
		for (PreparedFaction f : preparedFactions) {
			f.activePlayersInSector = 0;
			if (f.faction.getFactionMode() != Faction.MODE_SPECTATORS) {
				boolean active = false;
				for (PlayerState p : battleMode.state.getPlayerStatesByName().values()) {
					if (p.getFactionId() == f.faction.getIdFaction()) {
						if (p.getControllerState().getUnits().size() > 0 && p.getCurrentSector().equals(battleSector.pos)) {
							f.activePlayersInSector++;
							activePlayers.add(p);
							active = true;
						} else {
						}
					}
				}
				if (active) {
					activeFactions++;
				}
			}
		}
	}

	private void addSpectators() {
		//add spectators
		for (Faction f : battleMode.spectators) {
			FactionPreset preset = null;
			for (FactionPreset pre : battleMode.spectatorFactionNames) {
				if (pre.name.equals(f.getName())) {
					preset = pre;
				}
			}
			if (preset == null) {
			} else {
				preparedFactions.add(new PreparedFaction(preset, f));
			}
		}
	}

	private void initBattleSector() {
		this.battleSector = battleMode.battleSectors.get(0);
	}

	public void update(Timer timer) {
		if (conditions.size() > 0) {
			if (conditions.get(0).isMet()) {
				conditions.get(0).onMetCondition();
				conditions.remove(0);
			} else {
				conditions.get(0).onNotMetCondition();
			}
			if (conditions.size() > 0) {
				output = conditions.get(0).getDescription();
			}
		} else {
			output = "Round Over!";
			alive = false;
		}

	}

	public boolean isAlive() {
		return alive;
	}

	public String getCurrentOutput() {
		return output;
	}

	public boolean allowedToSpawnBBShips(PlayerState playerState, Faction f) {
		if (f != null && f.getFactionMode() != 0 && battleSector != null && playerState.getCurrentSector().equals(battleSector.pos)) {
			playerState.sendServerMessage(new ServerMessage(Lng.astr("You are not allowed to\nspawn ships in battle!"), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
			//cannot spawn ships while battle in battle sector
			return false;
		}
		return true;
	}

	public boolean isFighter(PlayerState playerState) {
		Faction faction = battleMode.state.getFactionManager().getFaction(playerState.getFactionId());
		if (battleSector != null && playerState.getCurrentSector().equals(battleSector.pos) && faction != null && (faction.isFactionMode(Faction.MODE_FIGHTERS_FFA) || faction.isFactionMode(Faction.MODE_FIGHTERS_TEAM))) {
			return true;
		}
		return false;
	}

	public String getBattleInfo() {
		return battleInfo;
	}

	public void onFactionChanged(PlayerState playerState, FactionChange change) {
		if (warpedInTime > 0) {
			assert (playerState.getFactionId() != change.from) : playerState.getFactionId() + "; " + change;
			if (playerState.getCurrentSector().equals(battleSector.pos)) {
				playerState.sendServerMessage(new ServerMessage(Lng.astr("Changing faction\nin battle costs\nyour life!"), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
				playerState.suicideOnServer();
			}
		}
	}

	private class PreparedFaction {
		public int activePlayersInSector;
		FactionPreset preset;
		Faction faction;

		public PreparedFaction(FactionPreset preset, Faction faction) {
			super();
			this.preset = preset;
			this.faction = faction;
		}

	}

}
