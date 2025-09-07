package org.schema.game.common.controller;

import it.unimi.dsi.fastutil.longs.Long2ShortOpenHashMap;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer;
import org.schema.game.client.view.gui.weapon.WeaponRowElementInterface;
import org.schema.game.common.controller.elements.ManagerActivityInterface;
import org.schema.game.common.controller.elements.ManagerReloadInterface;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;

public interface PlayerUsableInterface extends HandleControlInterface {
	long MIN_USABLE = Long.MIN_VALUE + 1000L;
	long USABLE_ID_STEALTH = Long.MIN_VALUE + 1L;
	long USABLE_ID_STRUCTURE_SCAN = Long.MIN_VALUE + 2L;
	long USABLE_ID_POWER_BATTERY = Long.MIN_VALUE + 3L;
	long USABLE_ID_ACTIVATION_BEAM = Long.MIN_VALUE + 4L;
	long USABLE_ID_THRUSTER = Long.MIN_VALUE + 5L;
	long USABLE_ID_JUMP = Long.MIN_VALUE + 6L;
	long USABLE_ID_SPACE_SCAN = Long.MIN_VALUE + 7L;
	@Deprecated
	long USABLE_ID_STEALTH_REACTOR = Long.MIN_VALUE + 8L;
	long USABLE_ID_REPULSOR = Long.MIN_VALUE + 9L;
	long USABLE_ID_NULL_EFFECT = Long.MIN_VALUE + 10L;
	long USABLE_ID_CONTROLLESS = Long.MIN_VALUE + 11L;
	long USABLE_ID_THRUSTER_OUTAGE = Long.MIN_VALUE + 12L;
	long USABLE_ID_NO_POWER_RECHARGE = Long.MIN_VALUE + 13L;
	long USABLE_ID_NO_SHIELD_RECHARGE = Long.MIN_VALUE + 14L;
	long USABLE_ID_PUSH = Long.MIN_VALUE + 15L;
	long USABLE_ID_PULL = Long.MIN_VALUE + 16L;
	long USABLE_ID_STOP = Long.MIN_VALUE + 17L;
	long USABLE_ID_STATUS_ARMOR_HARDEN = Long.MIN_VALUE + 18L;
	long USABLE_ID_STATUS_PIERCING_PROTECTION = Long.MIN_VALUE + 19L;
	long USABLE_ID_STATUS_POWER_SHIELD = Long.MIN_VALUE + 20L;
	long USABLE_ID_STATUS_SHIELD_HARDEN = Long.MIN_VALUE + 21L;
	long USABLE_ID_STATUS_ARMOR_HP_HARDENING_BONUS = Long.MIN_VALUE + 22L;
	long USABLE_ID_STATUS_ARMOR_HP_ABSORPTION_BONUS = Long.MIN_VALUE + 23L;
	long USABLE_ID_STATUS_STATUS_TOP_SPEED = Long.MIN_VALUE + 24L;
	long USABLE_ID_STATUS_STATUS_ANTI_GRAVITY = Long.MIN_VALUE + 25L;
	long USABLE_ID_STATUS_GRAVITY_EFFECT_IGNORANCE = Long.MIN_VALUE + 26L;
	long USABLE_ID_TAKE_OFF = Long.MIN_VALUE + 27L;
	long USABLE_ID_EVADE = Long.MIN_VALUE + 28L;
	long USABLE_ID_SHOOT_TURRETS = Long.MIN_VALUE + 29L;
	long USABLE_ID_REACTOR_BOOST = Long.MIN_VALUE + 30L;
	long USABLE_ID_INTERDICTION = Long.MIN_VALUE + 31L;
	long USABLE_ID_MINE_SHOOTER = Long.MIN_VALUE + 32L;
	long USABLE_ID_UNDOCK = Long.MIN_VALUE + 33L;
	long USABLE_ID_STATUS_ARMOR_HP_REGEN_BONUS = Long.MIN_VALUE + 34L;

	long[] ALWAYS_SELECTED = {
        USABLE_ID_THRUSTER
	};
	Long2ShortOpenHashMap ICONS = new Long2ShortOpenHashMap(
		new long[] {
			USABLE_ID_STEALTH,
			USABLE_ID_STRUCTURE_SCAN,
			USABLE_ID_POWER_BATTERY,
			USABLE_ID_ACTIVATION_BEAM,
			USABLE_ID_JUMP,
			USABLE_ID_SPACE_SCAN,
			USABLE_ID_STEALTH_REACTOR,
			USABLE_ID_REPULSOR,
			USABLE_ID_SHOOT_TURRETS,
			USABLE_ID_REACTOR_BOOST,
			USABLE_ID_INTERDICTION,
			USABLE_ID_TAKE_OFF,
			USABLE_ID_EVADE,
			USABLE_ID_UNDOCK,
            USABLE_ID_STATUS_ARMOR_HP_REGEN_BONUS
		},
		new short[] {
			ElementKeyMap.STEALTH_COMPUTER,
			ElementKeyMap.INTELL_COMPUTER,
			ElementKeyMap.POWER_BATTERY,
			ElementKeyMap.ACTIVAION_BLOCK_ID,
			ElementKeyMap.JUMP_DRIVE_CONTROLLER,
			ElementKeyMap.SCANNER_COMPUTER,
			ElementKeyMap.HULL_COLOR_RED_ID, //corresponds to deprecated reactor stealth
			ElementKeyMap.REPULSE_MODULE,
			ElementKeyMap.RAIL_BLOCK_TURRET_Y_AXIS,
			ElementKeyMap.REACTOR_MAIN,
			ElementKeyMap.JUMP_INHIBITOR_COMPUTER,
			ElementKeyMap.THRUSTER_ID,
			ElementKeyMap.EFFECT_PUSH_MODULE,
			ElementKeyMap.RAIL_BLOCK_DOCKER,
            263 //Grey Advanced Armor
		}
	);

	WeaponRowElementInterface getWeaponRow();

	boolean isControllerConnectedTo(long index, short type);

	boolean isPlayerUsable();

	long getUsableId();

	ManagerReloadInterface getReloadInterface();

	ManagerActivityInterface getActivityInterface();

	String getName();

	/**
	 * used to preemptively filter collections from being added for performance.
	 * For example not all activation element managers have to be added. only innner remotes.
	 *
	 * @return
	 */
	boolean isAddToPlayerUsable();

	void onPlayerDetachedFromThisOrADock(ManagedUsableSegmentController<?> originalCaller, PlayerState pState,
	                                     PlayerControllable newAttached);

	/**
	 * executed during input update
	 */
	void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer);

	void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, HudContextHelperContainer.Hos hos);

	/**
	 * info about weapon speed. gonna be 0 if no a weapon or hitscan
	 **/
	float getWeaponSpeed();

	/**
	 * info about weapon distance. gonna be 0 if no a weapon
	 **/
	float getWeaponDistance();

	/**
	 * player switched to (on) or from (!on) this usable
	 *
	 * @param on
	 */
	void onSwitched(boolean on);

	void onLogicActivate(SegmentPiece selfBlock, boolean oldActive, Timer timer);
}
